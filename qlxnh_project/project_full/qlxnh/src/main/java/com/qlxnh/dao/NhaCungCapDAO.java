package com.qlxnh.dao;

import com.qlxnh.entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho nhà cung cấp — dùng chung cho nhóm.
 *
 * Các module sử dụng:
 *   - UC-01 Quản lý danh mục (tab NCC): đầy đủ CRUD
 *   - UC-02 Lập phiếu nhập: getAll (đổ combobox)
 *   - UC-05 Tra cứu lịch sử phiếu nhập: getAll (lọc theo NCC)
 */
public class NhaCungCapDAO extends DAO {

    public List<NhaCungCap> getAll() throws SQLException {
        String sql = "SELECT id, maNCC, tenNCC, diaChi, soDT "
                   + "FROM tblNhaCungCap ORDER BY tenNCC";
        List<NhaCungCap> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public NhaCungCap getById(int id) throws SQLException {
        String sql = "SELECT id, maNCC, tenNCC, diaChi, soDT "
                   + "FROM tblNhaCungCap WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public NhaCungCap getByMa(String maNCC) throws SQLException {
        String sql = "SELECT id, maNCC, tenNCC, diaChi, soDT "
                   + "FROM tblNhaCungCap WHERE maNCC = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<NhaCungCap> search(String keyword) throws SQLException {
        String sql = "SELECT id, maNCC, tenNCC, diaChi, soDT "
                   + "FROM tblNhaCungCap "
                   + "WHERE maNCC LIKE ? OR tenNCC LIKE ? "
                   + "ORDER BY tenNCC";
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<NhaCungCap> list = new ArrayList<>();

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

    public int insert(NhaCungCap n) throws SQLException {
        String sql = "INSERT INTO tblNhaCungCap (maNCC, tenNCC, diaChi, soDT) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getMaNCC());
            ps.setString(2, n.getTenNCC());
            ps.setString(3, n.getDiaChi());
            ps.setString(4, n.getSoDT());
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

    public boolean update(NhaCungCap n) throws SQLException {
        String sql = "UPDATE tblNhaCungCap SET maNCC = ?, tenNCC = ?, diaChi = ?, soDT = ? "
                   + "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getMaNCC());
            ps.setString(2, n.getTenNCC());
            ps.setString(3, n.getDiaChi());
            ps.setString(4, n.getSoDT());
            ps.setInt(5, n.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay NCC id=" + n.getId());
            }
        }
        return false;
    }

    /** Xóa NCC. Thất bại nếu đang được tham chiếu trong tblCTPhieuNhap. */
    public void delete(int id) throws SQLException {
        int refs = countReference(id);
        if (refs > 0) {
            throw new IllegalStateException(
                "Khong the xoa: NCC dang duoc tham chieu trong " + refs + " phieu nhap.");
        }

        String sql = "DELETE FROM tblNhaCungCap WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay NCC id=" + id);
            }
        }
    }

    private int countReference(int nhaCungCapId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tblCTPhieuNhap WHERE nhaCungCapId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nhaCungCapId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private NhaCungCap map(ResultSet rs) throws SQLException {
        return new NhaCungCap(
            rs.getInt("id"),
            rs.getString("maNCC"),
            rs.getString("tenNCC"),
            rs.getString("diaChi"),
            rs.getString("soDT")
        );
    }
}
