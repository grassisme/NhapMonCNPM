package com.qlxnh.dao;

import com.qlxnh.entity.TonKhoReportItem;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ UC-06: Báo cáo tồn kho tại một thời điểm.
 *
 * Thực hiện truy vấn view v_TonKhoHienTai và function dbo.fn_TonKhoTaiNgay.
 */
public class BaoCaoDAO extends DAO {

    /**
     * Lấy danh sách tồn kho hiện tại (chế độ 1).
     * Đọc từ view v_TonKhoHienTai.
     */
    public List<TonKhoReportItem> layTonKhoHienTai() throws SQLException {
        String sql = "SELECT hangHoaId, maHang, tenHang, moTa, soLuongTon FROM v_TonKhoHienTai ORDER BY tenHang";
        List<TonKhoReportItem> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TonKhoReportItem(
                    rs.getInt("hangHoaId"),
                    rs.getString("maHang"),
                    rs.getString("tenHang"),
                    rs.getString("moTa"),
                    rs.getInt("soLuongTon"),
                    -1 // Chế độ tồn hiện tại không so sánh ngày
                ));
            }
        }
        return list;
    }

    /**
     * Lấy danh sách tồn kho tại một ngày trong quá khứ (chế độ 2).
     * Gọi table-valued function dbo.fn_TonKhoTaiNgay(?).
     */
    public List<TonKhoReportItem> layTonKhoTaiNgay(LocalDate ngay) throws SQLException {
        String sql = "SELECT hangHoaId, maHang, tenHang, moTa, tonHienTai, tonTaiNgay "
                   + "FROM dbo.fn_TonKhoTaiNgay(?) ORDER BY tenHang";
        List<TonKhoReportItem> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TonKhoReportItem(
                        rs.getInt("hangHoaId"),
                        rs.getString("maHang"),
                        rs.getString("tenHang"),
                        rs.getString("moTa"),
                        rs.getInt("tonHienTai"),
                        rs.getInt("tonTaiNgay")
                    ));
                }
            }
        }
        return list;
    }
}
