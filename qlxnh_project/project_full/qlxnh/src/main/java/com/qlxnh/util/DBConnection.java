package com.qlxnh.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lớp tiện ích mở kết nối tới SQL Server.
 *
 * Cách dùng (mọi DAO trong nhóm):
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // thực hiện truy vấn
 *   }
 *
 * Cấu hình URL/user/password đọc từ file src/main/resources/db.properties.
 * Khi đổi máy, mỗi thành viên trong nhóm chỉ cần sửa db.properties,
 * KHÔNG sửa file này.
 */
public final class DBConnection {

    private static final String PROPERTIES_FILE = "/db.properties";
    private static String url;
    private static String user;
    private static String password;

    // Khối static: chạy MỘT LẦN khi class được nạp vào JVM.
    // Đọc file properties + nạp JDBC driver.
    static {
        try {
            // Nạp class JDBC driver -> DriverManager nhận diện được URL "jdbc:sqlserver://..."
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Đọc thông số kết nối từ classpath
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
                if (in == null) {
                    throw new RuntimeException("Không tìm thấy " + PROPERTIES_FILE
                            + " trong classpath. Kiểm tra src/main/resources/db.properties.");
                }
                props.load(in);
            }

            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");

            if (url == null || user == null) {
                throw new RuntimeException("File db.properties thiếu db.url hoặc db.user.");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Thiếu JDBC driver mssql-jdbc trong classpath. "
                    + "Kiểm tra dependency trong pom.xml.", e);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc db.properties.", e);
        }
    }

    private DBConnection() { /* không cho khởi tạo */ }

    /**
     * Mở một Connection mới. Caller PHẢI đóng (dùng try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
