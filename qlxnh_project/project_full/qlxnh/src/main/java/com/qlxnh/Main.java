package com.qlxnh;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlxnh.view.LoginDialog;
import com.qlxnh.view.MainFrame;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point chính của toàn bộ ứng dụng Quản lý Xuất Nhập Hàng.
 *
 * Luồng chạy:
 *   1. Cài FlatLightLaf + cấu hình màu sắc tùy chỉnh.
 *   2. Mở LoginDialog (modal).
 *   3. Nếu đăng nhập thành công (isLoginSuccess() == true), mở MainFrame.
 *   4. Nếu người dùng đóng/hủy đăng nhập, thoát ứng dụng.
 */
public class Main {

    public static void main(String[] args) {
        // Cài FlatLaf TRƯỚC khi tạo bất kỳ component Swing nào
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Tùy chỉnh màu sắc đồng bộ với giao diện
            UIManager.put("Button.arc",                  8);
            UIManager.put("Component.arc",               6);
            UIManager.put("Component.focusWidth",        1);
            UIManager.put("TextField.margin",            new java.awt.Insets(2, 6, 2, 6));
            UIManager.put("Table.selectionBackground",   new Color(0xE6, 0xF1, 0xFB));
            UIManager.put("Table.selectionForeground",   new Color(0x0C, 0x44, 0x7C));
            UIManager.put("TableHeader.background",      new Color(0x18, 0x5F, 0xA5));
            UIManager.put("TableHeader.foreground",      Color.WHITE);
            UIManager.put("ScrollBar.thumbArc",          999);
            UIManager.put("ScrollBar.width",             8);

        } catch (Exception ex) {
            System.err.println("Không cài được FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Mở màn hình đăng nhập
            LoginDialog loginDlg = new LoginDialog(null);
            loginDlg.setVisible(true);

            // Kiểm tra kết quả đăng nhập
            if (loginDlg.isLoginSuccess()) {
                // Đăng nhập thành công -> Mở MainFrame
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } else {
                // Hủy đăng nhập -> Thoát ứng dụng
                System.exit(0);
            }
        });
    }
}
