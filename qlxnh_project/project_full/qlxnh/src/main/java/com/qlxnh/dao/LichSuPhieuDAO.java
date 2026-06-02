package com.qlxnh.dao;

import com.qlxnh.entity.LichSuPhieu;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho chức năng tra cứu lịch sử phiếu nhập/xuất
 */
public class LichSuPhieuDAO extends DAO {

    /**
     * Lấy toàn bộ lịch sử phiếu (nhập + xuất)
     */
    public List<LichSuPhieu> getAll() throws SQLException {
        String sql =
            "SELECT id, maPhieu, ngayNhap AS ngay, nguoiLapHoTen, soDong, tongTien, 'NHAP' AS loai " +
            "FROM v_PhieuNhap_TomTat " +
            "UNION ALL " +
            "SELECT id, maPhieu, ngayXuat AS ngay, nguoiLapHoTen, soDong, tongTien, 'XUAT' AS loai " +
            "FROM v_PhieuXuat_TomTat " +
            "ORDER BY ngay DESC";

        return executeQuery(sql, null);
    }

    /**
     * Tìm theo khoảng ngày + loại
     */
    public List<LichSuPhieu> search(LocalDate from, LocalDate to, String loai) throws SQLException {

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM (" +
            "SELECT id, maPhieu, ngayNhap AS ngay, nguoiLapHoTen, soDong, tongTien, 'NHAP' AS loai FROM v_PhieuNhap_TomTat " +
            "UNION ALL " +
            "SELECT id, maPhieu, ngayXuat AS ngay, nguoiLapHoTen, soDong, tongTien, 'XUAT' AS loai FROM v_PhieuXuat_TomTat" +
            ") AS t WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (from != null) {
            sql.append(" AND ngay >= ?");
            params.add(Date.valueOf(from));
        }

        if (to != null) {
            sql.append(" AND ngay <= ?");
            params.add(Date.valueOf(to));
        }

        if (loai != null && !loai.equalsIgnoreCase("ALL")) {
            sql.append(" AND loai = ?");
            params.add(loai.toUpperCase());
        }

        sql.append(" ORDER BY ngay DESC");

        return executeQuery(sql.toString(), params);
    }

    // =========================================================
    // PRIVATE HELPER
    // =========================================================

    private List<LichSuPhieu> executeQuery(String sql, List<Object> params) throws SQLException {
        List<LichSuPhieu> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuPhieu p = new LichSuPhieu();
                    p.setId(rs.getInt("id"));
                    p.setMaPhieu(rs.getString("maPhieu"));
                    p.setNgay(rs.getDate("ngay").toLocalDate());
                    p.setLoai(rs.getString("loai"));
                    p.setNguoiLap(rs.getString("nguoiLapHoTen"));
                    p.setSoDong(rs.getInt("soDong"));
                    p.setTongTien(rs.getBigDecimal("tongTien"));

                    list.add(p);
                }
            }
        }

        return list;
    }
}