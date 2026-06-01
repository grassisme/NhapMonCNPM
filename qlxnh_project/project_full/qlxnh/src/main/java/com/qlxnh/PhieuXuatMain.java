package com.qlxnh;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlxnh.view.PhieuXuatFrm;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Launcher để dev/test module Lập phiếu xuất độc lập.
 *
 * Khi tích hợp vào MainFrame của nhóm: bỏ file này, gọi
 *   new PhieuXuatFrm(userId, userName).setVisible(true)
 * từ MainFrame sau khi đăng nhập.
 */
public class PhieuXuatMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
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

        // Mock user — khi tích hợp lấy từ Session sau đăng nhập
        SwingUtilities.invokeLater(() ->
            new PhieuXuatFrm(1, "Nguyễn Văn A (Test)").setVisible(true)
        );
    }
}
