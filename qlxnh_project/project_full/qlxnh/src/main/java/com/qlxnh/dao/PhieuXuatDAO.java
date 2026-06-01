package com.qlxnh.dao;

import com.qlxnh.entity.CTPhieuXuat;
import com.qlxnh.entity.PhieuXuat;
import java.sql.*;
import java.sql.Date;

/**
 * DAO cho phiếu xuất.
 *
 * Khác với phiếu nhập: phiếu xuất phải KIỂM TRA TỒN KHO trước khi xuất.
 * Nếu số lượng xuất > tồn hiện tại -> không cho lưu (rollback).
 *
 * Phương thức luuPhieu(...) thực hiện trong một transaction duy nhất:
 *   1. Với mỗi dòng chi tiết: kiểm tra tồn kho hiện tại (kiemTraTon)
 *   2. INSERT vào tblPhieuXuat, lấy id sinh tự động
 *   3. INSERT các dòng vào tblCTPhieuXuat
 *   4. TRỪ tồn kho cho từng dòng (truTonKho)
 * Nếu bất kỳ bước nào fail -> rollback toàn bộ.
 *
 * Theo UML DesignLop_PhieuXuat:
 *   + kiemTraTon(maHang, soLuong) : boolean
 *   + luuPhieu(phieu) : int
 *   + truTonKho(ct)
 */
public class PhieuXuatDAO extends DAO {

    private final HangHoaDAO hangHoaDAO = new HangHoaDAO();

    /**
     * Kiểm tra tồn kho cho 1 mặt hàng có đủ để xuất không.
     * Method public (theo UML) — GUI có thể gọi để kiểm tra realtime
     * khi người dùng vừa nhập số lượng ở ChiTietXuatFrm.
     *
     * @param maHang  mã hàng (vd "H001")
     * @param soLuong số lượng cần xuất
     * @return true nếu tồn >= soLuong, false nếu không đủ hoặc không tìm thấy hàng
     */
    public boolean kiemTraTon(String maHang, int soLuong) throws SQLException {
        String sql = "SELECT soLuongTon FROM tblHangHoa WHERE maHang = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return rs.getInt("soLuongTon") >= soLuong;
            }
        }
    }

    /**
     * Lưu một phiếu xuất kèm toàn bộ chi tiết và trừ tồn kho.
     * Toàn bộ thao tác nằm trong 1 transaction.
     *
     * @param phieu đối tượng PhieuXuat chứa ngày, người lập, danh sách chi tiết
     * @return id mới của phiếu vừa tạo
     * @throws SQLException khi có lỗi DB (đã rollback)
     * @throws IllegalStateException khi tồn kho không đủ (đã rollback)
     */
    public int luuPhieu(PhieuXuat phieu) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // ===== Bước 1: Kiểm tra tồn cho TỪNG dòng trước khi đụng vào DB =====
            // Dùng kiểm tra qua maHang (UML) — gộp số lượng nếu cùng 1 hàng nhiều dòng.
            for (CTPhieuXuat ct : phieu.getChiTietList()) {
                int tonHienTai = layTonTrongTransaction(conn, ct.getHangHoa().getId());
                if (tonHienTai < ct.getSoLuong()) {
                    throw new IllegalStateException(
                        "Hang '" + ct.getHangHoa().getMaHang()
                        + "' khong du ton (con " + tonHienTai
                        + ", can " + ct.getSoLuong() + ").");
                }
            }

            // ===== Bước 2: INSERT phiếu xuất =====
            int phieuXuatId = insertPhieuXuat(conn, phieu);

            // ===== Bước 3: INSERT các dòng chi tiết =====
            for (CTPhieuXuat ct : phieu.getChiTietList()) {
                insertChiTiet(conn, phieuXuatId, ct);
            }

            // ===== Bước 4: Trừ tồn kho cho từng dòng =====
            for (CTPhieuXuat ct : phieu.getChiTietList()) {
                truTonKho(conn, ct);
            }

            conn.commit();
            return phieuXuatId;

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Trừ tồn kho cho 1 dòng chi tiết.
     * Theo UML: PhieuXuatDAO.truTonKho(ct).
     * Dùng trong transaction của luuPhieu — nhận Connection để chia sẻ tx.
     */
    public void truTonKho(Connection conn, CTPhieuXuat ct) throws SQLException {
        // delta âm vì đây là xuất
        hangHoaDAO.capNhatTonKho(conn, ct.getHangHoa().getId(), -ct.getSoLuong());
    }

    // ===================================================================
    // PRIVATE HELPERS
    // ===================================================================

    /** Đọc tồn hiện tại của 1 hàng trong cùng connection/transaction. */
    private int layTonTrongTransaction(Connection conn, int hangHoaId) throws SQLException {
        String sql = "SELECT soLuongTon FROM tblHangHoa WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hangHoaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Khong tim thay hang hoa id=" + hangHoaId);
            }
        }
    }

    /** Chèn 1 dòng vào tblPhieuXuat, trả id sinh tự động. */
    private int insertPhieuXuat(Connection conn, PhieuXuat phieu) throws SQLException {
        String sql = "INSERT INTO tblPhieuXuat (ngayXuat, nguoiDungId) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(phieu.getNgayXuat()));
            ps.setInt(2, phieu.getNguoiDungId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Khong lay duoc id phieu xuat moi");
            }
        }
    }

    /** Chèn 1 dòng vào tblCTPhieuXuat. */
    private void insertChiTiet(Connection conn, int phieuXuatId, CTPhieuXuat ct) throws SQLException {
        String sql = "INSERT INTO tblCTPhieuXuat "
                   + "(phieuXuatId, hangHoaId, daiLyConId, soLuong, donGia) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phieuXuatId);
            ps.setInt(2, ct.getHangHoa().getId());
            ps.setInt(3, ct.getDaiLyCon().getId());
            ps.setInt(4, ct.getSoLuong());
            ps.setBigDecimal(5, ct.getDonGia());
            ps.executeUpdate();
        }
    }
}
