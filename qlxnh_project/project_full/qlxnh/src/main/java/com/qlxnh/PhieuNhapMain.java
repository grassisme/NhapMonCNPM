package com.qlxnh;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlxnh.view.KetNoiDBDialog;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Launcher để dev/test module Lập phiếu nhập độc lập.
 *
 * Luồng chạy:
 *   1. Cài FlatLightLaf + màu sắc tùy chỉnh
 *   2. Mở KetNoiDBDialog — test kết nối SQL Server
 *   3. Nếu OK → KetNoiDBDialog tự mở PhieuNhapForm
 *
 * Khi tích hợp vào hệ thống nhóm: bỏ file này,
 * gọi new PhieuNhapForm(userId, userName).setVisible(true)
 * từ MainFrame sau khi đăng nhập.
 */
public class PhieuNhapMain {

    public static void main(String[] args) {
        // Cài FlatLaf TRƯỚC khi tạo bất kỳ component Swing nào
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Tùy chỉnh màu cho khớp với mockup xanh dương
            UIManager.put("Button.arc",                  8);
            UIManager.put("Component.arc",               6);
            UIManager.put("Component.focusWidth",        1);
            UIManager.put("TextField.margin",            new java.awt.Insets(2,6,2,6));
            UIManager.put("Table.selectionBackground",   new Color(0xE6, 0xF1, 0xFB));
            UIManager.put("Table.selectionForeground",   new Color(0x0C, 0x44, 0x7C));
            UIManager.put("TableHeader.background",      new Color(0x18, 0x5F, 0xA5));
            UIManager.put("TableHeader.foreground",      Color.WHITE);
            UIManager.put("ScrollBar.thumbArc",          999);
            UIManager.put("ScrollBar.width",             8);

        } catch (Exception ex) {
            System.err.println("Không cài được FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() ->
            new KetNoiDBDialog(null, true).setVisible(true)
        );
    }
}
