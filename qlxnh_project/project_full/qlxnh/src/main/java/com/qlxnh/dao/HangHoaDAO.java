package com.qlxnh.dao;

import com.qlxnh.entity.HangHoa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho hàng hóa — dùng chung cho cả nhóm.
 *
 * Các module sử dụng:
 *   - UC-01 Quản lý danh mục: getAll, search, insert, update, delete
 *   - UC-02 Lập phiếu nhập: getAll (đổ combobox), capNhatTonKho (cộng tồn)
 *   - UC-03 Lập phiếu xuất: getAll, getTonKho (kiểm tra trước khi xuất),
 *     capNhatTonKho (trừ tồn)
 *   - UC-06 Báo cáo tồn kho: getAll
 */
public class HangHoaDAO extends DAO {

    /** Lấy toàn bộ danh sách hàng hóa, sắp xếp theo tên. */
    public List<HangHoa> getAll() throws SQLException {
        String sql = "SELECT id, maHang, tenHang, moTa, soLuongTon "
                   + "FROM tblHangHoa ORDER BY tenHang";
        List<HangHoa> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Tìm theo id, trả null nếu không có. */
    public HangHoa getById(int id) throws SQLException {
        String sql = "SELECT id, maHang, tenHang, moTa, soLuongTon "
                   + "FROM tblHangHoa WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Tìm theo mã hàng (UNIQUE), trả null nếu không có.
     */
    public HangHoa getByMa(String maHang) throws SQLException {
        String sql = "SELECT id, maHang, tenHang, moTa, soLuongTon "
                   + "FROM tblHangHoa WHERE maHang = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Tìm theo từ khóa trong mã hoặc tên. */
    public List<HangHoa> search(String keyword) throws SQLException {
        String sql = "SELECT id, maHang, tenHang, moTa, soLuongTon "
                   + "FROM tblHangHoa "
                   + "WHERE maHang LIKE ? OR tenHang LIKE ? "
                   + "ORDER BY tenHang";
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<HangHoa> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Thêm hàng hóa mới. Mã hàng phải UNIQUE, trùng sẽ ném SQLException
     * (do constraint UNIQUE ở tblHangHoa).
     *
     * @return id mới sinh
     */
    public int insert(HangHoa hh) throws SQLException {
        String sql = "INSERT INTO tblHangHoa (maHang, tenHang, moTa, soLuongTon) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hh.getMaHang());
            ps.setString(2, hh.getTenHang());
            ps.setString(3, hh.getMoTa());
            ps.setInt(4, hh.getSoLuongTon());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    hh.setId(id);
                    return id;
                }
                throw new SQLException("Khong lay duoc id moi");
            }
        }
    }

    /**
     * Cập nhật thông tin hàng hóa. Không cho sửa soLuongTon ở đây — soLuongTon
     * chỉ đổi qua phiếu nhập/xuất (để đảm bảo toàn vẹn).
     * Nếu cần điều chỉnh tồn thủ công (kiểm kê), gọi capNhatTonKho() riêng.
     *
     * @return
     */
    public boolean update(HangHoa hh) throws SQLException {
        String sql = "UPDATE tblHangHoa SET maHang = ?, tenHang = ?, moTa = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hh.getMaHang());
            ps.setString(2, hh.getTenHang());
            ps.setString(3, hh.getMoTa());
            ps.setInt(4, hh.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay hang hoa id=" + hh.getId());
            }
        }
        return false;
    }

    /**
     * Xóa hàng hóa. Sẽ thất bại nếu hàng đang được tham chiếu trong
     * phiếu nhập/xuất (FK constraint). Method này kiểm tra trước, ném
     * IllegalStateException với thông báo rõ ràng thay vì SQL exception.
     */
    public void delete(int id) throws SQLException {
        // Kiểm tra ràng buộc trước
        int countNhap = countReference("tblCTPhieuNhap", id);
        int countXuat = countReference("tblCTPhieuXuat", id);
        if (countNhap + countXuat > 0) {
            throw new IllegalStateException(
                "Khong the xoa: hang hoa dang duoc tham chieu trong "
                + countNhap + " phieu nhap va " + countXuat + " phieu xuat.");
        }

        String sql = "DELETE FROM tblHangHoa WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay hang hoa id=" + id);
            }
        }
    }

    /** Đếm số tham chiếu của hàng hóa trong một bảng chi tiết. */
    private int countReference(String tableName, int hangHoaId) throws SQLException {
        // tableName được kiểm soát trong code (không từ user) -> an toàn
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE hangHoaId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hangHoaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ===================================================================
    // CÁC METHOD QUAN TRỌNG CHO MODULE PHIẾU NHẬP/XUẤT
    // ===================================================================

    /**
     * Lấy số lượng tồn kho hiện tại của một mặt hàng.
     * Module Lập phiếu xuất (UC-03) gọi để kiểm tra trước khi xuất.
     */
    public int getTonKho(int hangHoaId) throws SQLException {
        String sql = "SELECT soLuongTon FROM tblHangHoa WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hangHoaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Khong tim thay hang hoa id=" + hangHoaId);
            }
        }
    }

    /**
     * Cập nhật tồn kho (cộng dồn).
     *
     * @param conn  Connection truyền vào — để dùng chung trong transaction
     *              với việc lưu phiếu (PhieuNhapDAO/PhieuXuatDAO sẽ truyền
     *              connection đang mở của họ vào, không tự mở mới).
     * @param hangHoaId id hàng cần đổi tồn
     * @param delta giá trị cộng vào: dương khi nhập (cộng), âm khi xuất (trừ)
     */
    public void capNhatTonKho(Connection conn, int hangHoaId, int delta) throws SQLException {
        String sql = "UPDATE tblHangHoa SET soLuongTon = soLuongTon + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, hangHoaId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay hang hoa id=" + hangHoaId);
            }
        }
    }

    // ===================================================================
    // Helper: map ResultSet thành đối tượng HangHoa
    // ===================================================================
    private HangHoa map(ResultSet rs) throws SQLException {
        return new HangHoa(
            rs.getInt("id"),
            rs.getString("maHang"),
            rs.getString("tenHang"),
            rs.getString("moTa"),
            rs.getInt("soLuongTon")
        );
    }
}
