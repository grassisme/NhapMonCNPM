package com.qlxnh.dao;

import com.qlxnh.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp cha cho mọi DAO trong dự án (HangHoaDAO, NhaCungCapDAO,
 * PhieuNhapDAO, PhieuXuatDAO, NguoiDungDAO...).
 *
 * Mục tiêu: cung cấp 3 tiện ích chung mà DAO con nào cũng cần,
 * tránh viết lặp 5 lần cho 5 module:
 *
 *   1. getConnection() — mở Connection mới
 *   2. close(...)      — đóng tài nguyên an toàn (null-safe)
 *   3. countRows(sql)  — đếm số dòng kết quả của một truy vấn
 *
 * DAO con kế thừa và dùng trực tiếp các method này.
 *
 * GHI CHÚ CHO NHÓM:
 *   - KHÔNG đặt logic nghiệp vụ vào đây. Lớp này chỉ chứa
 *     tiện ích thuần kỹ thuật.
 *   - KHÔNG cache Connection (mỗi DAO con tự mở/đóng theo session).
 *   - Mọi DAO con đều phải `extends DAO`.
 */
public abstract class DAO {

    /**
     * Mở Connection mới. DAO con dùng trong try-with-resources:
     *   try (Connection conn = getConnection()) { ... }
     */
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Đóng tài nguyên JDBC an toàn — kể cả khi đối tượng null.
     * Tiện cho các method DAO cũ không dùng try-with-resources.
     */
    protected void close(AutoCloseable... resources) {
        if (resources == null) return;
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignore) { /* nuốt lỗi đóng */ }
            }
        }
    }

    /**
     * Đếm số dòng kết quả của một câu SELECT bất kỳ.
     * Hữu ích cho các module cần phân trang (UC-05 Tra cứu phiếu) hoặc
     * kiểm tra tồn tại bản ghi.
     *
     * @param sql    câu truy vấn SELECT (KHÔNG cần wrap SELECT COUNT(*))
     * @param params tham số PreparedStatement (truyền vào ?, ?, ?... trong sql)
     * @return số dòng kết quả, hoặc 0 nếu không có
     */
    protected int countRows(String sql, Object... params) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS sub";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
