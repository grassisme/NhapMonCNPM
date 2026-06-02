package com.qlxnh.view;

import com.qlxnh.entity.NguoiDung;
import com.qlxnh.util.Session;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Cửa sổ chính của hệ thống Quản lý Xuất Nhập Hàng.
 *
 * Bố cục:
 *   - WEST:   Sidebar xanh đậm với menu điều hướng + thông tin user
 *   - CENTER: Content area (mặc định hiện Welcome panel)
 *
 * Menu hiển thị tuỳ theo vai trò:
 *   - Quản lý kho:        Phiếu nhập, Phiếu xuất, Đăng xuất
 *   - Nhân viên nhập liệu: Phiếu nhập, Phiếu xuất, Đăng xuất
 *   - Người xem:           Đăng xuất
 *
 * Khi bấm menu → mở form tương ứng (JFrame riêng).
 * Khi đăng xuất → ẩn MainFrame → xóa Session → hiện LoginDialog.
 */
public class MainFrame extends JFrame {

    // ── Màu chủ đạo ────────────────────────────────────────
    private static final Color SIDEBAR_BG     = new Color(0x0F, 0x17, 0x2A);
    private static final Color SIDEBAR_HOVER  = new Color(0x1E, 0x29, 0x3B);
    private static final Color SIDEBAR_ACTIVE = new Color(0x18, 0x5F, 0xA5);
    private static final Color PRIMARY        = new Color(0x18, 0x5F, 0xA5);
    private static final Color PRIMARY_LIGHT  = new Color(0xE6, 0xF1, 0xFB);
    private static final Color TEXT_WHITE      = Color.WHITE;
    private static final Color TEXT_GRAY_LIGHT = new Color(0x94, 0xA3, 0xB8);
    private static final Color CONTENT_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color BORDER_C       = new Color(0xE2, 0xE8, 0xF0);

    private static final int SIDEBAR_WIDTH = 250;

    // ── UI components ──────────────────────────────────────
    private JPanel pnlSidebar;
    private JPanel pnlContent;
    private JButton activeButton = null; // nút menu đang được chọn

    public MainFrame() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("QLXNH — Quản lý Xuất Nhập Hàng");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setLayout(new BorderLayout(0, 0));

        // ── Sidebar ──────────────────────────────────────
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setBackground(SIDEBAR_BG);
        pnlSidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        pnlSidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ── Logo area ──
        JPanel pnlLogo = new JPanel();
        pnlLogo.setLayout(new BoxLayout(pnlLogo, BoxLayout.Y_AXIS));
        pnlLogo.setBackground(SIDEBAR_BG);
        pnlLogo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x1E, 0x29, 0x3B)),
            BorderFactory.createEmptyBorder(20, 20, 16, 20)
        ));
        pnlLogo.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 90));

        JLabel lblLogo = new JLabel("📦  QLXNH");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(TEXT_WHITE);
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlLogo.add(lblLogo);

        JLabel lblVersion = new JLabel("Quản lý Xuất Nhập Hàng v1.0");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(TEXT_GRAY_LIGHT);
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlLogo.add(Box.createVerticalStrut(4));
        pnlLogo.add(lblVersion);

        pnlSidebar.add(pnlLogo);

        // ── User info area ──
        NguoiDung currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            JPanel pnlUser = new JPanel();
            pnlUser.setLayout(new BoxLayout(pnlUser, BoxLayout.Y_AXIS));
            pnlUser.setBackground(new Color(0x14, 0x1E, 0x30));
            pnlUser.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
            pnlUser.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 70));

            // Avatar + Tên
            JPanel pnlUserRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pnlUserRow.setBackground(new Color(0x14, 0x1E, 0x30));
            pnlUserRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblAvatar = new JLabel("👤 ");
            lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            pnlUserRow.add(lblAvatar);

            JLabel lblUserName = new JLabel(currentUser.getHoTen());
            lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblUserName.setForeground(TEXT_WHITE);
            pnlUserRow.add(lblUserName);

            pnlUser.add(pnlUserRow);

            // Vai trò
            String tenVaiTro = currentUser.getVaiTro() != null
                ? currentUser.getVaiTro().getTenVaiTro()
                : "Người dùng";
            JLabel lblRole = new JLabel("    " + tenVaiTro);
            lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblRole.setForeground(new Color(0x6B, 0xB5, 0xF0));
            lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlUser.add(Box.createVerticalStrut(2));
            pnlUser.add(lblRole);

            pnlSidebar.add(pnlUser);
        }

        // ── Menu section label ──
        pnlSidebar.add(Box.createVerticalStrut(16));
        JLabel lblMenuSection = new JLabel("   CHỨC NĂNG");
        lblMenuSection.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenuSection.setForeground(TEXT_GRAY_LIGHT);
        lblMenuSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenuSection.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 20));
        pnlSidebar.add(lblMenuSection);
        pnlSidebar.add(Box.createVerticalStrut(6));

        // ── Menu buttons (tuỳ theo vai trò) ──
        boolean canLapPhieu = Session.isQuanLyKho() || Session.isNhanVien();

        if (canLapPhieu) {
            JButton btnPhieuNhap = createMenuButton("📋  Lập phiếu nhập");
            btnPhieuNhap.addActionListener(e -> {
                setActiveButton(btnPhieuNhap);
                moPhieuNhap();
            });
            pnlSidebar.add(btnPhieuNhap);

            JButton btnPhieuXuat = createMenuButton("📦  Lập phiếu xuất");
            btnPhieuXuat.addActionListener(e -> {
                setActiveButton(btnPhieuXuat);
                moPhieuXuat();
            });
            pnlSidebar.add(btnPhieuXuat);
        }

        // ── Báo cáo tồn kho (Tất cả vai trò đều xem được) ──
        JButton btnBaoCao = createMenuButton("📊  Báo cáo tồn kho");
        btnBaoCao.addActionListener(e -> {
            setActiveButton(btnBaoCao);
            moBaoCaoTonKho();
        });
        pnlSidebar.add(btnBaoCao);

        // ── Spacer (đẩy nút Đăng xuất xuống cuối) ──
        pnlSidebar.add(Box.createVerticalGlue());

        // ── Separator trước Đăng xuất ──
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(0x1E, 0x29, 0x3B));
        sep.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 1));
        pnlSidebar.add(sep);

        // ── Nút Đăng xuất ──
        JButton btnLogout = createMenuButton("🚪  Đăng xuất");
        btnLogout.addActionListener(e -> dangXuat());
        pnlSidebar.add(btnLogout);
        pnlSidebar.add(Box.createVerticalStrut(10));

        add(pnlSidebar, BorderLayout.WEST);

        // ── Content area ─────────────────────────────────
        pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(CONTENT_BG);
        hienThiWelcome();
        add(pnlContent, BorderLayout.CENTER);
    }

    // ── Tạo nút menu sidebar ─────────────────────────────
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_GRAY_LIGHT);
        btn.setBackground(SIDEBAR_BG);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 42));
        btn.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_HOVER);
                    btn.setForeground(TEXT_WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(TEXT_GRAY_LIGHT);
                }
            }
        });

        return btn;
    }

    /** Đánh dấu nút menu đang active. */
    private void setActiveButton(JButton btn) {
        // Reset nút cũ
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setForeground(TEXT_GRAY_LIGHT);
        }
        // Highlight nút mới
        activeButton = btn;
        btn.setBackground(SIDEBAR_ACTIVE);
        btn.setForeground(TEXT_WHITE);
    }

    // ── Welcome panel (content mặc định) ─────────────────
    private void hienThiWelcome() {
        pnlContent.removeAll();

        JPanel pnlWelcome = new JPanel();
        pnlWelcome.setLayout(new BoxLayout(pnlWelcome, BoxLayout.Y_AXIS));
        pnlWelcome.setBackground(CONTENT_BG);
        pnlWelcome.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        // ── Welcome card ──
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(600, 400));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblIcon = new JLabel("🏬");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(16));

        NguoiDung user = Session.getCurrentUser();
        String hoTen = user != null ? user.getHoTen() : "bạn";
        JLabel lblWelcome = new JLabel("Chào mừng, " + hoTen + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(new Color(0x1E, 0x29, 0x3B));
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblWelcome);

        card.add(Box.createVerticalStrut(8));

        String tenVaiTro = "";
        if (user != null && user.getVaiTro() != null) {
            tenVaiTro = user.getVaiTro().getTenVaiTro();
        }
        JLabel lblRoleInfo = new JLabel("Vai trò: " + tenVaiTro);
        lblRoleInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRoleInfo.setForeground(PRIMARY);
        lblRoleInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblRoleInfo);

        card.add(Box.createVerticalStrut(24));

        // Separator
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(400, 1));
        sep.setForeground(BORDER_C);
        card.add(sep);

        card.add(Box.createVerticalStrut(20));

        // Hướng dẫn sử dụng
        JLabel lblGuide = new JLabel("Hướng dẫn sử dụng");
        lblGuide.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGuide.setForeground(new Color(0x33, 0x33, 0x33));
        lblGuide.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblGuide);
        card.add(Box.createVerticalStrut(12));

        boolean canLapPhieu = Session.isQuanLyKho() || Session.isNhanVien();
        if (canLapPhieu) {
            card.add(createGuideItem("📋", "Lập phiếu nhập",
                "Tạo phiếu nhập hàng từ nhà cung cấp"));
            card.add(Box.createVerticalStrut(8));
            card.add(createGuideItem("📦", "Lập phiếu xuất",
                "Tạo phiếu xuất hàng cho đại lý con"));
        } else {
            JLabel lblNoAccess = new JLabel("Bạn chỉ có quyền xem. Liên hệ Quản lý kho để được cấp quyền.");
            lblNoAccess.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblNoAccess.setForeground(new Color(0x6B, 0x70, 0x7B));
            lblNoAccess.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblNoAccess);
        }

        // ── Centering wrapper ──
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBackground(CONTENT_BG);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(card);
        wrapper.add(Box.createHorizontalGlue());

        pnlWelcome.add(wrapper);
        pnlContent.add(pnlWelcome, BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    /** Tạo 1 dòng hướng dẫn (icon + title + desc). */
    private JPanel createGuideItem(String icon, String title, String desc) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnl.setBackground(PRIMARY_LIGHT);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pnl.setMaximumSize(new Dimension(500, 50));
        pnl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        pnl.add(lblIcon);

        JLabel lblTitle = new JLabel(title + "  — ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(PRIMARY);
        pnl.add(lblTitle);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(0x33, 0x33, 0x33));
        pnl.add(lblDesc);

        return pnl;
    }

    // ── Mở các form ──────────────────────────────────────
    private void moPhieuNhap() {
        NguoiDung user = Session.getCurrentUser();
        PhieuNhapForm form = new PhieuNhapForm(user.getId(), user.getHoTen());
        form.setVisible(true);
    }

    private void moPhieuXuat() {
        NguoiDung user = Session.getCurrentUser();
        PhieuXuatFrm form = new PhieuXuatFrm(user.getId(), user.getHoTen());
        form.setVisible(true);
    }

    private void moBaoCaoTonKho() {
        BaoCaoTonKhoForm form = new BaoCaoTonKhoForm();
        form.setVisible(true);
    }

    // ── Đăng xuất ────────────────────────────────────────
    private void dangXuat() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            Session.clear();

            // Mở lại LoginDialog
            SwingUtilities.invokeLater(() -> {
                LoginDialog dlg = new LoginDialog(null);
                dlg.setVisible(true);

                if (dlg.isLoginSuccess()) {
                    new MainFrame().setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }
}
