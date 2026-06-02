package com.qlxnh.view;

import com.qlxnh.dao.NguoiDungDAO;
import com.qlxnh.entity.NguoiDung;
import com.qlxnh.util.Session;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Màn hình đăng nhập hệ thống Quản lý Xuất Nhập Hàng.
 *
 * Luồng:
 *   1. Người dùng nhập tên đăng nhập + mật khẩu
 *   2. Nhấn "Đăng nhập" hoặc Enter
 *   3. Gọi NguoiDungDAO.checkLogin() để xác thực
 *   4. Nếu OK → Session.setCurrentUser(user) → đóng dialog
 *   5. Nếu sai → hiển thị lỗi inline (không popup)
 *
 * Sau khi dialog đóng, caller kiểm tra isLoginSuccess() để biết kết quả.
 */
public class LoginDialog extends JDialog {

    // ── UI components ──────────────────────────────────────
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnExit;
    private JLabel         lblError;

    // ── Kết quả đăng nhập ──────────────────────────────────
    private boolean loginSuccess = false;

    // ── Màu chủ đạo (khớp với KetNoiDBDialog) ─────────────
    private static final Color PRIMARY       = new Color(0x18, 0x5F, 0xA5);
    private static final Color PRIMARY_DARK  = new Color(0x0C, 0x44, 0x7C);
    private static final Color PRIMARY_LIGHT = new Color(0xE6, 0xF1, 0xFB);
    private static final Color BG_WHITE      = new Color(0xFF, 0xFF, 0xFF);
    private static final Color BORDER_C      = new Color(0xD6, 0xDC, 0xE4);
    private static final Color TEXT_GRAY     = new Color(0x6B, 0x70, 0x7B);
    private static final Color ERROR_RED     = new Color(0xDC, 0x26, 0x26);

    public LoginDialog(Frame parent) {
        super(parent, "Đăng nhập hệ thống", true);
        initComponents();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_WHITE);

        // ── Header banner xanh dương ──────────────────────
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(PRIMARY);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));

        JLabel lblSystemIcon = new JLabel("📦");
        lblSystemIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblSystemIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblSystemIcon);

        pnlHeader.add(Box.createVerticalStrut(8));

        JLabel lblTitle = new JLabel("Quản lý Xuất Nhập Hàng");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Đăng nhập để tiếp tục");
        lblSubtitle.setForeground(new Color(0xBB, 0xD5, 0xF0));
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(Box.createVerticalStrut(4));
        pnlHeader.add(lblSubtitle);

        add(pnlHeader, BorderLayout.NORTH);

        // ── Form đăng nhập ────────────────────────────────
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBackground(BG_WHITE);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(24, 36, 8, 36));

        // Label + Field: Tên đăng nhập
        JLabel lblUsername = new JLabel("Tên đăng nhập");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(PRIMARY_DARK);
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblUsername);
        pnlForm.add(Box.createVerticalStrut(6));

        txtUsername = new JTextField(20);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập...");
        pnlForm.add(txtUsername);
        pnlForm.add(Box.createVerticalStrut(16));

        // Label + Field: Mật khẩu
        JLabel lblPassword = new JLabel("Mật khẩu");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(PRIMARY_DARK);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblPassword);
        pnlForm.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField(20);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        // Enter trên field mật khẩu → đăng nhập
        txtPassword.addActionListener(e -> xuLyDangNhap());
        pnlForm.add(txtPassword);
        pnlForm.add(Box.createVerticalStrut(10));

        // Label lỗi (ẩn mặc định)
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ERROR_RED);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblError);
        pnlForm.add(Box.createVerticalStrut(10));

        // Nút Đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> xuLyDangNhap());
        // Hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(PRIMARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(PRIMARY);
            }
        });
        pnlForm.add(btnLogin);
        pnlForm.add(Box.createVerticalStrut(10));

        // Nút Thoát
        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setBackground(BG_WHITE);
        btnExit.setForeground(TEXT_GRAY);
        btnExit.setOpaque(true);
        btnExit.setBorder(BorderFactory.createLineBorder(BORDER_C));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> {
            loginSuccess = false;
            dispose();
        });
        pnlForm.add(btnExit);
        pnlForm.add(Box.createVerticalStrut(16));

        // Footer info
        JLabel lblInfo = new JLabel("Mật khẩu mặc định: 123456");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(TEXT_GRAY);
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblInfo);

        add(pnlForm, BorderLayout.CENTER);

        // ── Kích thước cửa sổ ─────────────────────────────
        setPreferredSize(new Dimension(380, 520));
        pack();
    }

    // ── Xử lý đăng nhập ──────────────────────────────────
    private void xuLyDangNhap() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Validate rỗng
        if (username.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên đăng nhập.");
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            hienThiLoi("Vui lòng nhập mật khẩu.");
            txtPassword.requestFocus();
            return;
        }

        // Disable nút khi đang xử lý
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
        lblError.setText(" ");

        // Chạy trên background thread để không block UI
        new Thread(() -> {
            try {
                NguoiDungDAO dao = new NguoiDungDAO();
                NguoiDung user = dao.checkLogin(username, password);

                SwingUtilities.invokeLater(() -> {
                    if (user != null) {
                        // Đăng nhập thành công
                        Session.setCurrentUser(user);
                        loginSuccess = true;
                        dispose();
                    } else {
                        // Sai thông tin hoặc tài khoản bị khóa
                        hienThiLoi("Sai tên đăng nhập hoặc mật khẩu.");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Đăng nhập");
                    }
                });

            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> {
                    hienThiLoi("Lỗi kết nối CSDL: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                });
            }
        }).start();
    }

    private void hienThiLoi(String msg) {
        lblError.setText("⚠ " + msg);
    }

    /** Caller gọi sau khi dialog đóng để kiểm tra kết quả. */
    public boolean isLoginSuccess() {
        return loginSuccess;
    }
}
