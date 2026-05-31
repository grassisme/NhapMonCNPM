package com.qlxnh.dao;

import com.qlxnh.entity.DaiLyCon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho đại lý con — dùng chung cho nhóm.
 *
 * Các module sử dụng:
 *   - UC-01 Quản lý danh mục (tab Đại lý con): đầy đủ CRUD
 *   - UC-03 Lập phiếu xuất: getAll (đổ combobox)
 *   - UC-05 Tra cứu lịch sử phiếu xuất: getAll (lọc theo đại lý)
 */
public class DaiLyConDAO extends DAO {

    public List<DaiLyCon> getAll() throws SQLException {
        String sql = "SELECT id, maDL, tenDL, diaChi, soDT "
                   + "FROM tblDaiLyCon ORDER BY tenDL";
        List<DaiLyCon> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public DaiLyCon getById(int id) throws SQLException {
        String sql = "SELECT id, maDL, tenDL, diaChi, soDT "
                   + "FROM tblDaiLyCon WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public DaiLyCon getByMa(String maDL) throws SQLException {
        String sql = "SELECT id, maDL, tenDL, diaChi, soDT "
                   + "FROM tblDaiLyCon WHERE maDL = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDL);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<DaiLyCon> search(String keyword) throws SQLException {
        String sql = "SELECT id, maDL, tenDL, diaChi, soDT "
                   + "FROM tblDaiLyCon "
                   + "WHERE maDL LIKE ? OR tenDL LIKE ? "
                   + "ORDER BY tenDL";
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<DaiLyCon> list = new ArrayList<>();

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

    public int insert(DaiLyCon d) throws SQLException {
        String sql = "INSERT INTO tblDaiLyCon (maDL, tenDL, diaChi, soDT) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getMaDL());
            ps.setString(2, d.getTenDL());
            ps.setString(3, d.getDiaChi());
            ps.setString(4, d.getSoDT());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    d.setId(id);
                    return id;
                }
                throw new SQLException("Khong lay duoc id moi");
            }
        }
    }

    public void update(DaiLyCon d) throws SQLException {
        String sql = "UPDATE tblDaiLyCon SET maDL = ?, tenDL = ?, diaChi = ?, soDT = ? "
                   + "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getMaDL());
            ps.setString(2, d.getTenDL());
            ps.setString(3, d.getDiaChi());
            ps.setString(4, d.getSoDT());
            ps.setInt(5, d.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay dai ly id=" + d.getId());
            }
        }
    }

    /** Xóa đại lý. Thất bại nếu đang được tham chiếu trong tblCTPhieuXuat. */
    public void delete(int id) throws SQLException {
        int refs = countReference(id);
        if (refs > 0) {
            throw new IllegalStateException(
                "Khong the xoa: dai ly dang duoc tham chieu trong " + refs + " phieu xuat.");
        }

        String sql = "DELETE FROM tblDaiLyCon WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Khong tim thay dai ly id=" + id);
            }
        }
    }

    private int countReference(int daiLyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tblCTPhieuXuat WHERE daiLyConId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, daiLyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private DaiLyCon map(ResultSet rs) throws SQLException {
        return new DaiLyCon(
            rs.getInt("id"),
            rs.getString("maDL"),
            rs.getString("tenDL"),
            rs.getString("diaChi"),
            rs.getString("soDT")
        );
    }
}
