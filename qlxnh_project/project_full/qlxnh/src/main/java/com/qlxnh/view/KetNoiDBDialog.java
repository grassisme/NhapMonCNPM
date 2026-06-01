package com.qlxnh.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlxnh.util.DBConnection;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Màn hình test kết nối CSDL trước khi mở PhieuNhapForm.
 * Người dùng nhập thông tin server/DB/password, nhấn Test,
 * nếu OK thì mở PhieuNhapForm.
 */
public class KetNoiDBDialog extends JDialog {

    // ── UI components ──────────────────────────────────────
    private JTextField     txtServer;
    private JTextField     txtDatabase;
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JTextArea      txtLog;
    private JButton        btnTest;
    private JButton        btnKetNoi;
    private JButton        btnThoat;

    // ── Màu chủ đạo ────────────────────────────────────────
    private static final Color PRIMARY   = new Color(0x18, 0x5F, 0xA5);
    private static final Color BG_LIGHT  = new Color(0xF4, 0xF7, 0xFB);
    private static final Color BORDER_C  = new Color(0xD6, 0xDC, 0xE4);

    public KetNoiDBDialog(Frame parent, boolean modal) {
        super(parent, "Kiểm tra kết nối CSDL", modal);
        initComponents();
        setLocationRelativeTo(null);
        setResizable(false);
        log("Nhập thông tin kết nối rồi nhấn \"Test kết nối\".");
    }

    // ── Dựng giao diện ─────────────────────────────────────
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // ── Header màu xanh ──
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        pnlHeader.setBackground(PRIMARY);
        JLabel lblTitle = new JLabel("Quản lý Xuất Nhập Hàng — Kết nối CSDL");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 13f));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // ── Form nhập thông tin ──
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(16, 20, 8, 20),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_C),
                "Thông tin kết nối SQL Server",
                TitledBorder.LEFT, TitledBorder.TOP,
                null, PRIMARY)
        ));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 10, 6, 6);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(6, 0, 6, 10);

        String[][] fields = {
            {"Server:", "localhost"},
            {"Database:", "QLXNH"},
            {"Username:", "sa"},
            {"Password:", ""}
        };

        JTextField[] textFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            lc.gridx = 0; lc.gridy = i;
            fc.gridx = 1; fc.gridy = i;

            JLabel lbl = new JLabel(fields[i][0]);
            lbl.setPreferredSize(new Dimension(90, 26));
            pnlForm.add(lbl, lc);

            if (i == 3) {
                txtPassword = new JPasswordField(fields[i][1], 20);
                txtPassword.setPreferredSize(new Dimension(260, 28));
                pnlForm.add(txtPassword, fc);
            } else {
                textFields[i] = new JTextField(fields[i][1], 20);
                textFields[i].setPreferredSize(new Dimension(260, 28));
                pnlForm.add(textFields[i], fc);
            }
        }
        txtServer   = textFields[0];
        txtDatabase = textFields[1];
        txtUsername = textFields[2];

        add(pnlForm, BorderLayout.CENTER);

        // ── Log area ──
        JPanel pnlLog = new JPanel(new BorderLayout());
        pnlLog.setBackground(Color.WHITE);
        pnlLog.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));

        txtLog = new JTextArea(6, 40);
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBackground(BG_LIGHT);
        txtLog.setForeground(new Color(0x2C, 0x2C, 0x2A));
        txtLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(txtLog);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_C));
        pnlLog.add(scroll, BorderLayout.CENTER);
        add(pnlLog, BorderLayout.SOUTH);

        // ── Buttons ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        pnlBtn.setBackground(new Color(0xF4, 0xF7, 0xFB));
        pnlBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));

        btnTest   = makeButton("Test kết nối",          PRIMARY, Color.WHITE);
        btnKetNoi = makeButton("Kết nối & Mở phiếu nhập", PRIMARY, Color.WHITE);
        btnThoat  = makeButton("Thoát",                 Color.WHITE, new Color(0x5F,0x5E,0x5A));

        btnKetNoi.setEnabled(false);
        btnThoat.setBorder(BorderFactory.createLineBorder(BORDER_C));

        pnlBtn.add(btnTest);
        pnlBtn.add(btnKetNoi);
        pnlBtn.add(btnThoat);

        // Thêm pnlBtn vào SOUTH của pnlLog (ghép lại)
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.add(pnlLog, BorderLayout.CENTER);
        pnlBottom.add(pnlBtn, BorderLayout.SOUTH);
        remove(pnlLog);
        add(pnlBottom, BorderLayout.SOUTH);

        // ── Events ──
        btnTest.addActionListener(e -> testKetNoi());
        btnKetNoi.addActionListener(e -> moPhieuNhap());
        btnThoat.addActionListener(e -> System.exit(0));

        pack();
    }

    // ── Logic kết nối ──────────────────────────────────────
    private void testKetNoi() {
        txtLog.setText("");
        String server = txtServer.getText().trim();
        String db     = txtDatabase.getText().trim();
        String user   = txtUsername.getText().trim();
        String pass   = new String(txtPassword.getPassword());

        log("Đang kết nối tới " + server + "/" + db + "...");
        btnTest.setEnabled(false);
        btnKetNoi.setEnabled(false);

        new Thread(() -> {
            String url = "jdbc:sqlserver://" + server
                       + ":1433;databaseName=" + db
                       + ";encrypt=true;trustServerCertificate=true";
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                var rs = conn.getMetaData()
                             .getTables(null, null, "tblHangHoa", new String[]{"TABLE"});
                boolean hasSchema = rs.next();
                rs.close();

                SwingUtilities.invokeLater(() -> {
                    log("✓ Kết nối thành công!");
                    if (hasSchema) {
                        log("✓ Schema QLXNH đã có sẵn (tìm thấy tblHangHoa).");
                        log("→ Nhấn \"Kết nối & Mở phiếu nhập\" để tiếp tục.");
                        btnKetNoi.setEnabled(true);
                    } else {
                        log("⚠ Kết nối OK nhưng CHƯA có schema QLXNH.");
                        log("→ Hãy chạy file qlxnh_database.sql trong SSMS trước.");
                    }
                    btnTest.setEnabled(true);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    log("✗ Kết nối thất bại: " + ex.getMessage());
                    log("Kiểm tra: SQL Server đang chạy? Port 1433? Đúng mật khẩu?");
                    btnTest.setEnabled(true);
                });
            }
        }).start();
    }

    private void moPhieuNhap() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            // Mock user để test — khi tích hợp lấy từ Session sau đăng nhập
            new PhieuNhapForm(1, "Nguyễn Văn A (Test)").setVisible(true);
        });
    }

    // ── Helpers ────────────────────────────────────────────
    private void log(String msg) {
        String time = LocalDateTime.now()
                                   .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        txtLog.append("[" + time + "] " + msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
