package com.qlxnh.dao;

import com.qlxnh.entity.CTPhieuNhap;
import com.qlxnh.entity.PhieuNhap;
import java.sql.*;
import java.sql.Date;

/**
 * DAO cho phiếu nhập.
 *
 * Phương thức quan trọng nhất: luuPhieu(...) — thực hiện 3 thao tác trong
 * một transaction duy nhất:
 *   1. INSERT vào tblPhieuNhap, lấy id sinh tự động
 *   2. INSERT nhiều dòng vào tblCTPhieuNhap với phieuNhapId vừa lấy
 *   3. UPDATE soLuongTon cho mỗi mặt hàng trong chi tiết
 *
 * Nếu một bước fail, rollback toàn bộ. Nếu thành công, commit.
 * Đây là lý do giáo trình nhấn mạnh "DAO chứa logic giao dịch chứ không
 * chỉ là CRUD đơn lẻ".
 */
public class PhieuNhapDAO extends DAO {

    private final HangHoaDAO hangHoaDAO = new HangHoaDAO();

    /**
     * Lưu một phiếu nhập kèm toàn bộ chi tiết và cập nhật tồn kho.
     *
     * @param phieu đối tượng PhieuNhap chứa ngày, người lập, và danh sách chi tiết
     * @return id mới của phiếu vừa tạo
     * @throws SQLException khi có lỗi DB (đã rollback)
     */
    public int luuPhieu(PhieuNhap phieu) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            // Tắt auto-commit để chủ động kiểm soát transaction
            conn.setAutoCommit(false);

            // ===== Bước 1: INSERT phiếu nhập, lấy id =====
            int phieuNhapId = insertPhieuNhap(conn, phieu);

            // ===== Bước 2: INSERT các dòng chi tiết =====
            for (CTPhieuNhap ct : phieu.getChiTietList()) {
                insertChiTiet(conn, phieuNhapId, ct);
            }

            // ===== Bước 3: Cập nhật tồn kho cho từng dòng =====
            for (CTPhieuNhap ct : phieu.getChiTietList()) {
                hangHoaDAO.capNhatTonKho(conn, ct.getHangHoa().getId(), ct.getSoLuong());
            }

            // Mọi thứ OK -> commit
            conn.commit();
            return phieuNhapId;

        } catch (SQLException e) {
            // Có lỗi -> rollback và ném tiếp lên Controller
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // khôi phục default trước khi đóng
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }

    /** Chèn 1 dòng vào tblPhieuNhap, trả id sinh tự động. */
    private int insertPhieuNhap(Connection conn, PhieuNhap phieu) throws SQLException {
        String sql = "INSERT INTO tblPhieuNhap (ngayNhap, nguoiDungId) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(phieu.getNgayNhap()));
            ps.setInt(2, phieu.getNguoiDungId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Khong lay duoc id phieu nhap moi");
            }
        }
    }

    /** Chèn 1 dòng vào tblCTPhieuNhap. */
    private void insertChiTiet(Connection conn, int phieuNhapId, CTPhieuNhap ct) throws SQLException {
        String sql = "INSERT INTO tblCTPhieuNhap "
                   + "(phieuNhapId, hangHoaId, nhaCungCapId, soLuong, donGia) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phieuNhapId);
            ps.setInt(2, ct.getHangHoa().getId());
            ps.setInt(3, ct.getNhaCungCap().getId());
            ps.setInt(4, ct.getSoLuong());
            ps.setBigDecimal(5, ct.getDonGia());
            ps.executeUpdate();
        }
    }
}
