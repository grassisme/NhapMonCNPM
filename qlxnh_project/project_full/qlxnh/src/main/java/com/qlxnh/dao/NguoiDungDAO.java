package com.qlxnh.dao;

import com.qlxnh.entity.NguoiDung;
import com.qlxnh.entity.VaiTro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho người dùng — dùng chung cho nhóm.
 *
 * Các module sử dụng:
 *   - LoginDialog (cross-cutting): checkLogin, getByTenDangNhap
 *   - UC-07 Quản lý tài khoản: đầy đủ CRUD + đổi mật khẩu + khóa/kích hoạt
 *   - UC-05 Tra cứu lịch sử phiếu: getById (hiển thị tên người lập)
 *
 * Chú ý: mật khẩu hiện đang lưu thô (plain text). Khi nâng cấp lên
 * production thì băm bằng BCrypt — sửa ở 2 chỗ: checkLogin() và changePassword().
 * Khi đó cột matKhau (NVARCHAR(255)) đã đủ dài cho hash.
 */
public class NguoiDungDAO extends DAO {

    /**
     * Kiểm tra đăng nhập.
     * Cross-cutting concern: được gọi từ LoginDialog.
     *
     * @return NguoiDung (kèm đối tượng VaiTro) nếu đúng và tài khoản kích hoạt,
     *         null nếu sai thông tin hoặc tài khoản đã bị khóa.
     */
    public NguoiDung checkLogin(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT nd.id, nd.hoTen, nd.tenDangNhap, nd.matKhau, "
                   + "       nd.kichHoat, nd.vaiTroId, vt.tenVaiTro "
                   + "FROM tblNguoiDung nd "
                   + "JOIN tblVaiTro vt ON vt.id = nd.vaiTroId "
                   + "WHERE nd.tenDangNhap = ? AND nd.matKhau = ? AND nd.kichHoat = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithVaiTro(rs) : null;
            }
        }
    }

    /** Lấy danh sách toàn bộ người dùng kèm tên vai trò (cho màn quản lý). */
    public List<NguoiDung> getAll() throws SQLException {
        String sql = "SELECT nd.id, nd.hoTen, nd.tenDangNhap, nd.matKhau, "
                   + "       nd.kichHoat, nd.vaiTroId, vt.tenVaiTro "
                   + "FROM tblNguoiDung nd "
                   + "JOIN tblVaiTro vt ON vt.id = nd.vaiTroId "
                   + "ORDER BY nd.hoTen";
        List<NguoiDung> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapWithVaiTro(rs));
        }
        return list;
    }

    public NguoiDung getById(int id) throws SQLException {
        String sql = "SELECT nd.id, nd.hoTen, nd.tenDangNhap, nd.matKhau, "
                   + "       nd.kichHoat, nd.vaiTroId, vt.tenVaiTro "
                   + "FROM tblNguoiDung nd "
                   + "JOIN tblVaiTro vt ON vt.id = nd.vaiTroId "
                   + "WHERE nd.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithVaiTro(rs) : null;
            }
        }
    }

    public NguoiDung getByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT nd.id, nd.hoTen, nd.tenDangNhap, nd.matKhau, "
                   + "       nd.kichHoat, nd.vaiTroId, vt.tenVaiTro "
                   + "FROM tblNguoiDung nd "
                   + "JOIN tblVaiTro vt ON vt.id = nd.vaiTroId "
                   + "WHERE nd.tenDangNhap = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithVaiTro(rs) : null;
            }
        }
    }

    public List<NguoiDung> search(String keyword) throws SQLException {
        String sql = "SELECT nd.id, nd.hoTen, nd.tenDangNhap, nd.matKhau, "
                   + "       nd.kichHoat, nd.vaiTroId, vt.tenVaiTro "
                   + "FROM tblNguoiDung nd "
                   + "JOIN tblVaiTro vt ON vt.id = nd.vaiTroId "
                   + "WHERE nd.hoTen LIKE ? OR nd.tenDangNhap LIKE ? "
                   + "ORDER BY nd.hoTen";
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<NguoiDung> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapWithVaiTro(rs));
            }
        }
        return list;
    }

    /**
     * Thêm người dùng mới. tenDangNhap phải UNIQUE.
     * Mặc định kichHoat = true.
     */
    public int insert(NguoiDung n) throws SQLException {
        String sql = "INSERT INTO tblNguoiDung (hoTen, tenDangNhap, matKhau, kichHoat, vaiTroId) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getHoTen());
            ps.setString(2, n.getTenDangNhap());
            ps.setString(3, n.getMatKhau());
            ps.setBoolean(4, n.isKichHoat());
            ps.setInt(5, n.getVaiTroId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    n.setId(id);
                    return id;
                }
                throw new SQLException("Khong lay duoc id moi");
            }
        }
    }

    /** Cập nhật thông tin chung. KHÔNG đổi mật khẩu ở đây — dùng changePassword(). */
    public void update(NguoiDung n) throws SQLException {
        String sql = "UPDATE tblNguoiDung SET hoTen = ?, tenDangNhap = ?, "
                   + "kichHoat = ?, vaiTroId = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getHoTen());
            ps.setString(2, n.getTenDangNhap());
            ps.setBoolean(3, n.isKichHoat());
            ps.setInt(4, n.getVaiTroId());
            ps.setInt(5, n.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay nguoi dung id=" + n.getId());
            }
        }
    }

    /** Đổi mật khẩu — tách riêng vì là thao tác nhạy cảm. */
    public void changePassword(int id, String matKhauMoi) throws SQLException {
        String sql = "UPDATE tblNguoiDung SET matKhau = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay nguoi dung id=" + id);
            }
        }
    }

    /** Khóa/mở khóa tài khoản (không xóa, để giữ lịch sử phiếu đã lập). */
    public void setKichHoat(int id, boolean kichHoat) throws SQLException {
        String sql = "UPDATE tblNguoiDung SET kichHoat = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, kichHoat);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay nguoi dung id=" + id);
            }
        }
    }

    /**
     * Xóa người dùng. Thường KHÔNG nên xóa thật mà chỉ setKichHoat(false),
     * vì phiếu nhập/xuất đã lập tham chiếu nguoiDungId. Xóa sẽ thất bại
     * nếu người dùng đã lập phiếu nào.
     */
    public void delete(int id) throws SQLException {
        int refs = countReference(id);
        if (refs > 0) {
            throw new IllegalStateException(
                "Khong the xoa: nguoi dung da lap " + refs + " phieu nhap/xuat. "
                + "Hay khoa tai khoan thay vi xoa.");
        }

        String sql = "DELETE FROM tblNguoiDung WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay nguoi dung id=" + id);
            }
        }
    }

    /** Đếm số phiếu nhập + xuất đã lập bởi user. */
    private int countReference(int nguoiDungId) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM tblPhieuNhap WHERE nguoiDungId = ?) "
                   + "     + (SELECT COUNT(*) FROM tblPhieuXuat WHERE nguoiDungId = ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nguoiDungId);
            ps.setInt(2, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Lấy danh sách vai trò (để đổ combobox khi tạo/sửa user). */
    public List<VaiTro> getAllVaiTro() throws SQLException {
        String sql = "SELECT id, tenVaiTro FROM tblVaiTro ORDER BY id";
        List<VaiTro> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new VaiTro(rs.getInt("id"), rs.getString("tenVaiTro")));
            }
        }
        return list;
    }

    // ===================================================================
    // Helper: map ResultSet kèm VaiTro vào NguoiDung
    // ===================================================================
    private NguoiDung mapWithVaiTro(ResultSet rs) throws SQLException {
        NguoiDung n = new NguoiDung(
            rs.getInt("id"),
            rs.getString("hoTen"),
            rs.getString("tenDangNhap"),
            rs.getString("matKhau"),
            rs.getBoolean("kichHoat"),
            rs.getInt("vaiTroId")
        );
        n.setVaiTro(new VaiTro(rs.getInt("vaiTroId"), rs.getString("tenVaiTro")));
        return n;
    }
}
