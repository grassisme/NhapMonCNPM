package com.qlxnh.view;

import com.qlxnh.dao.LichSuPhieuDAO;
import com.qlxnh.entity.LichSuPhieu;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

public class LichSuPhieuForm extends JFrame {

    // ── Màu (đồng bộ MainFrame) ────────────────────────────
    private static final Color PRIMARY      = new Color(0x18, 0x5F, 0xA5);
    private static final Color PRIMARY_DARK = new Color(0x0C, 0x44, 0x7C);
    private static final Color BORDER_C     = new Color(0xD6, 0xDC, 0xE4);
    private static final Color BG_GRAY      = new Color(0xF8, 0xFA, 0xFC);
    private static final Color NHAP_COLOR   = new Color(0x16, 0xA3, 0x4A);
    private static final Color XUAT_COLOR   = new Color(0xDC, 0x26, 0x26);

    private static final NumberFormat  MONEY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── UI ────────────────────────────────────────────────
    private JTextField      txtTuNgay, txtDenNgay;
    private JComboBox<String> cbLoai;
    private JButton         btnTimKiem, btnLamMoi;
    private JTable          tbl;
    private DefaultTableModel tableModel;
    private JLabel          lblTongKet;

    private final LichSuPhieuDAO dao = new LichSuPhieuDAO();

    public LichSuPhieuForm() {
        initComponents();
        loadTable();        // ← method từ JFrame.java
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Tra cứu lịch sử phiếu nhập / xuất");
        setSize(920, 580);
        setMinimumSize(new Dimension(780, 450));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(main);

        // ── NORTH: Bộ lọc ────────────────────────────────
        JPanel pnlFilter = new JPanel(new GridBagLayout());
        pnlFilter.setBackground(BG_GRAY);
        pnlFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Từ ngày
        gbc.gridx = 0; gbc.weightx = 0;
        pnlFilter.add(boldLabel("Từ ngày:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.25;
        txtTuNgay = new JTextField(10);
        txtTuNgay.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        pnlFilter.add(txtTuNgay, gbc);

        // Đến ngày
        gbc.gridx = 2; gbc.weightx = 0;
        pnlFilter.add(boldLabel("Đến ngày:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.25;
        txtDenNgay = new JTextField(10);
        txtDenNgay.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        pnlFilter.add(txtDenNgay, gbc);

        // Loại phiếu
        gbc.gridx = 4; gbc.weightx = 0;
        pnlFilter.add(boldLabel("Loại:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.2;
        cbLoai = new JComboBox<>(new String[]{"Tất cả", "Phiếu nhập", "Phiếu xuất"});
        pnlFilter.add(cbLoai, gbc);

        // Nút Tìm kiếm
        gbc.gridx = 6; gbc.weightx = 0;
        btnTimKiem = new JButton("🔍 Tìm kiếm");
        btnTimKiem.setBackground(PRIMARY);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setOpaque(true);
        btnTimKiem.setBorderPainted(false);
        btnTimKiem.setFocusPainted(false);
        btnTimKiem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTimKiem.addActionListener(e -> timKiem());
        btnTimKiem.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnTimKiem.setBackground(PRIMARY_DARK); }
            @Override public void mouseExited (MouseEvent e) { btnTimKiem.setBackground(PRIMARY); }
        });
        pnlFilter.add(btnTimKiem, gbc);

        // Nút Làm mới
        gbc.gridx = 7;
        btnLamMoi = new JButton("↺ Làm mới");
        btnLamMoi.setBackground(Color.WHITE);
        btnLamMoi.setForeground(PRIMARY);
        btnLamMoi.setBorder(BorderFactory.createLineBorder(PRIMARY));
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLamMoi.addActionListener(e -> {
            txtTuNgay.setText("");
            txtDenNgay.setText("");
            cbLoai.setSelectedIndex(0);
            loadTable();
        });
        pnlFilter.add(btnLamMoi, gbc);

        main.add(pnlFilter, BorderLayout.NORTH);

        // ── CENTER: Bảng dữ liệu ─────────────────────────
        String[] cols = {"Mã phiếu", "Loại", "Ngày", "Người lập", "Số dòng", "Tổng tiền"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tbl = new JTable(tableModel);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(28);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(PRIMARY);
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setGridColor(BORDER_C);
        tbl.setRowSorter(new TableRowSorter<>(tableModel));

        // Căn giữa cột Mã phiếu, Ngày, Số dòng
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);
        tbl.getColumnModel().getColumn(2).setCellRenderer(center);
        tbl.getColumnModel().getColumn(4).setCellRenderer(center);

        // Màu sắc cột Loại
        tbl.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) setForeground("NHẬP".equals(v) ? NHAP_COLOR : XUAT_COLOR);
                return this;
            }
        });

        // Căn phải cột Tổng tiền
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        tbl.getColumnModel().getColumn(5).setCellRenderer(right);

        // Độ rộng cột
        int[] widths = {100, 80, 100, 180, 70, 150};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Double-click → xem chi tiết (Bước 3)
        tbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tbl.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });

        main.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // ── SOUTH: Tổng kết ───────────────────────────────
        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setBackground(Color.WHITE);
        pnlSouth.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        lblTongKet = new JLabel("0 phiếu");
        lblTongKet.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTongKet.setForeground(new Color(0x64, 0x74, 0x8B));
        lblTongKet.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        pnlSouth.add(lblTongKet, BorderLayout.EAST);
        main.add(pnlSouth, BorderLayout.SOUTH);
    }

    // ── LOAD TOÀN BỘ (từ JFrame.java) ────────────────────
    public void loadTable() {
        try {
            List<LichSuPhieu> list = dao.getAll();
            tableModel.setRowCount(0);
            for (LichSuPhieu p : list) {
                tableModel.addRow(new Object[]{
                        p.getMaPhieu(),
                        p.getLoai().equals("NHAP") ? "NHẬP" : "XUẤT",
                        p.getNgay().format(DATE_FMT),
                        p.getNguoiLap(),
                        p.getSoDong(),
                        MONEY_FORMAT.format(p.getTongTien()) + " đ"
                });
            }
            lblTongKet.setText(list.size() + " phiếu");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── TÌM KIẾM THEO BỘ LỌC ─────────────────────────────
    private void timKiem() {
        try {
            String tuStr  = txtTuNgay.getText().trim();
            String denStr = txtDenNgay.getText().trim();

            LocalDate tu  = tuStr.isEmpty()  ? null : LocalDate.parse(tuStr,  DATE_FMT);
            LocalDate den = denStr.isEmpty() ? null : LocalDate.parse(denStr, DATE_FMT);

            if (tu != null && den != null && tu.isAfter(den)) {
                JOptionPane.showMessageDialog(this,
                        "Ngày bắt đầu không được lớn hơn ngày kết thúc.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String loai = switch (cbLoai.getSelectedIndex()) {
                case 1  -> "NHAP";
                case 2  -> "XUAT";
                default -> "ALL";
            };

            List<LichSuPhieu> list = dao.search(tu, den, loai);
            tableModel.setRowCount(0);
            for (LichSuPhieu p : list) {
                tableModel.addRow(new Object[]{
                        p.getMaPhieu(),
                        p.getLoai().equals("NHAP") ? "NHẬP" : "XUẤT",
                        p.getNgay().format(DATE_FMT),
                        p.getNguoiLap(),
                        p.getSoDong(),
                        MONEY_FORMAT.format(p.getTongTien()) + " đ"
                });
            }
            lblTongKet.setText(list.size() + " phiếu");

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ. Nhập dd/MM/yyyy.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi CSDL:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── XEM CHI TIẾT (Bước 3 — tạm để trống, điền sau) ──
    private void xemChiTiet() {
        int row = tbl.getSelectedRow();
        // TODO: Bước 3 — mở ChiTietPhieuDialog
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }
}