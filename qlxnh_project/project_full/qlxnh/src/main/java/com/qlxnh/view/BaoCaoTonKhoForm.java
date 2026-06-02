package com.qlxnh.view;

import com.qlxnh.controller.BaoCaoController;
import com.qlxnh.entity.TonKhoReportItem;
import com.qlxnh.util.Session;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Giao diện Báo cáo tồn kho tại một thời điểm (UC-06).
 *
 * Cho phép xem tồn kho hiện tại hoặc tồn kho tại một ngày trong quá khứ,
 * lọc tìm kiếm nhanh theo tên/mã, sắp xếp, và xuất báo cáo ra Excel (CSV).
 */
public class BaoCaoTonKhoForm extends JFrame {

    private final BaoCaoController controller = new BaoCaoController();
    private List<TonKhoReportItem> reportData = new ArrayList<>();
    private boolean isHienTaiMode = false; // Mặc định hiển thị Tồn tại ngày tham chiếu như hình mockup

    // ── UI Colors (Đồng bộ với MainFrame & Mockup) ─────────
    private static final Color PRIMARY       = new Color(0x18, 0x5F, 0xA5);
    private static final Color PRIMARY_DARK  = new Color(0x0C, 0x44, 0x7C);
    private static final Color PRIMARY_LIGHT = new Color(0xE6, 0xF1, 0xFB);
    private static final Color BORDER_C      = new Color(0xD6, 0xDC, 0xE4);
    private static final Color BG_WHITE      = Color.WHITE;
    private static final Color BG_GRAY_LIGHT = new Color(0xF8, 0xFA, 0xFC);
    private static final Color TEXT_DARK     = new Color(0x1E, 0x29, 0x3B);
    private static final Color TEXT_GRAY     = new Color(0x64, 0x74, 0x8B);

    // Màu cảnh báo/biến động
    private static final Color WARNING_BG      = new Color(0xFE, 0xF3, 0xC7); // Vàng/cam nhạt cho tồn thấp
    private static final Color WARNING_BG_SEL  = new Color(0xFD, 0xE6, 0x8A); // Vàng sậm hơn khi chọn dòng
    private static final Color DECREASE_COLOR  = new Color(0xDC, 0x26, 0x26); // Đỏ cho giảm
    private static final Color INCREASE_COLOR  = new Color(0x16, 0xA3, 0x4A); // Xanh lá cho tăng

    // ── UI Components ──────────────────────────────────────
    private JRadioButton rbTonHienTai;
    private JRadioButton rbTonTaiNgay;
    private JTextField txtNgay;
    private JButton btnXem;
    private JButton btnExcel;
    private JLabel lblInfoText;
    private JPanel pnlInfoBanner;
    
    private JTextField txtSearch;
    private JComboBox<String> cbSort;
    
    private JTable tblReport;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JLabel lblLegendLowStock;
    private JLabel lblSummary;

    public BaoCaoTonKhoForm() {
        initComponents();
        setupEvents();
        
        // Mặc định nạp dữ liệu tại ngày 31/03/2026 như mockup
        txtNgay.setText("31/03/2026");
        loadData();
    }

    private void initComponents() {
        setTitle("Báo cáo tồn kho hàng hóa");
        setSize(980, 640);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(BG_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(mainPanel);

        // =================================================================
        // PHÍA BẮC (NORTH): Panel chế độ xem và Banner thông tin
        // =================================================================
        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.setBackground(BG_WHITE);

        // Panel bộ lọc chế độ xem
        JPanel pnlFilter = new JPanel(new GridBagLayout());
        pnlFilter.setBackground(BG_GRAY_LIGHT);
        pnlFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);

        // Label Chế độ xem:
        JLabel lblCheDo = new JLabel("Chế độ xem:");
        lblCheDo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCheDo.setForeground(TEXT_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlFilter.add(lblCheDo, gbc);

        // Radio Tồn kho hiện tại
        rbTonHienTai = new JRadioButton("Tồn kho hiện tại");
        rbTonHienTai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbTonHienTai.setBackground(BG_GRAY_LIGHT);
        rbTonHienTai.setForeground(TEXT_DARK);
        gbc.gridx = 1; gbc.gridy = 0;
        pnlFilter.add(rbTonHienTai, gbc);

        // Radio Tồn tại ngày
        rbTonTaiNgay = new JRadioButton("Tồn tại ngày...");
        rbTonTaiNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbTonTaiNgay.setBackground(BG_GRAY_LIGHT);
        rbTonTaiNgay.setForeground(TEXT_DARK);
        rbTonTaiNgay.setSelected(true); // Mặc định chọn
        gbc.gridx = 2; gbc.gridy = 0;
        pnlFilter.add(rbTonTaiNgay, gbc);

        ButtonGroup bgCheDo = new ButtonGroup();
        bgCheDo.add(rbTonHienTai);
        bgCheDo.add(rbTonTaiNgay);

        // Textfield Ngày
        txtNgay = new JTextField(10);
        txtNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNgay.setPreferredSize(new Dimension(100, 28));
        txtNgay.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        gbc.gridx = 3; gbc.gridy = 0;
        pnlFilter.add(txtNgay, gbc);

        // Nút Xem
        btnXem = new JButton("🔍 Xem");
        btnXem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXem.setBackground(PRIMARY);
        btnXem.setForeground(Color.WHITE);
        btnXem.setOpaque(true);
        btnXem.setBorderPainted(false);
        btnXem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXem.setPreferredSize(new Dimension(80, 28));
        gbc.gridx = 4; gbc.gridy = 0;
        pnlFilter.add(btnXem, gbc);

        // Spacer đẩy nút Excel sang phải
        JPanel spacer = new JPanel();
        spacer.setBackground(BG_GRAY_LIGHT);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 1.0;
        pnlFilter.add(spacer, gbc);

        // Nút Xuất Excel
        btnExcel = new JButton("📊 Xuất Excel");
        btnExcel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExcel.setBackground(BG_WHITE);
        btnExcel.setForeground(PRIMARY);
        btnExcel.setBorder(BorderFactory.createLineBorder(PRIMARY));
        btnExcel.setFocusPainted(false);
        btnExcel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExcel.setPreferredSize(new Dimension(110, 28));
        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0;
        pnlFilter.add(btnExcel, gbc);

        pnlNorth.add(pnlFilter);
        pnlNorth.add(Box.createVerticalStrut(8));

        // Banner thông báo/công thức
        pnlInfoBanner = new JPanel(new BorderLayout(8, 0));
        pnlInfoBanner.setBackground(PRIMARY_LIGHT);
        pnlInfoBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblInfoIcon = new JLabel("🛈");
        lblInfoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblInfoIcon.setForeground(PRIMARY_DARK);
        pnlInfoBanner.add(lblInfoIcon, BorderLayout.WEST);

        lblInfoText = new JLabel("Hiển thị tồn kho tại ngày 31/03/2026 — tính bằng tồn hiện tại + tổng xuất sau ngày - tổng nhập sau ngày.");
        lblInfoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfoText.setForeground(PRIMARY_DARK);
        pnlInfoBanner.add(lblInfoText, BorderLayout.CENTER);

        pnlNorth.add(pnlInfoBanner);
        mainPanel.add(pnlNorth, BorderLayout.NORTH);

        // =================================================================
        // PHẦN TRUNG TÂM (CENTER): Tìm kiếm + Sắp xếp + Bảng dữ liệu
        // =================================================================
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 8));
        pnlCenter.setBackground(BG_WHITE);

        // Thanh công cụ lọc & sắp xếp trên đầu bảng
        JPanel pnlTableTools = new JPanel(new BorderLayout(10, 0));
        pnlTableTools.setBackground(BG_WHITE);

        // Ô tìm kiếm nhanh
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(280, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Lọc theo tên hoặc mã mặt hàng...");
        pnlTableTools.add(txtSearch, BorderLayout.WEST);

        // Sắp xếp combobox
        JPanel pnlSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlSort.setBackground(BG_WHITE);
        JLabel lblSort = new JLabel("Sắp xếp:");
        lblSort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSort.setForeground(TEXT_DARK);
        pnlSort.add(lblSort);

        cbSort = new JComboBox<>(new String[]{"Tên mặt hàng", "Mã hàng", "Tồn hiện tại", "Biến động"});
        cbSort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbSort.setPreferredSize(new Dimension(130, 30));
        pnlSort.add(cbSort);
        pnlTableTools.add(pnlSort, BorderLayout.EAST);

        pnlCenter.add(pnlTableTools, BorderLayout.NORTH);

        // Bảng dữ liệu JTable
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Chỉ xem
            }
        };

        tblReport = new JTable(tableModel);
        tblReport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblReport.setRowHeight(28);
        tblReport.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblReport.getTableHeader().setBackground(PRIMARY);
        tblReport.getTableHeader().setForeground(Color.WHITE);
        tblReport.getTableHeader().setReorderingAllowed(false);
        tblReport.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblReport.setGridColor(BORDER_C);

        tableSorter = new TableRowSorter<>(tableModel);
        tblReport.setRowSorter(tableSorter);

        JScrollPane scrollPane = new JScrollPane(tblReport);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_C));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(pnlCenter, BorderLayout.CENTER);

        // =================================================================
        // PHẦN NAM (SOUTH): Chú thích & Trạng thái footer
        // =================================================================
        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setBackground(BG_WHITE);
        pnlSouth.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));

        // Legend panel
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        pnlLegend.setBackground(BG_WHITE);

        // Mẫu màu tồn thấp
        lblLegendLowStock = new JLabel("  Tồn hiện tại thấp (< 20)  ");
        lblLegendLowStock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLegendLowStock.setOpaque(true);
        lblLegendLowStock.setBackground(WARNING_BG);
        lblLegendLowStock.setBorder(BorderFactory.createLineBorder(new Color(0xF5, 0x9E, 0x0B)));
        pnlLegend.add(lblLegendLowStock);

        JLabel lblLegendDown = new JLabel("↓ Giảm so với ngày tham chiếu");
        lblLegendDown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLegendDown.setForeground(DECREASE_COLOR);
        pnlLegend.add(lblLegendDown);

        JLabel lblLegendUp = new JLabel("↑ Tăng so với ngày tham chiếu");
        lblLegendUp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLegendUp.setForeground(INCREASE_COLOR);
        pnlLegend.add(lblLegendUp);

        pnlSouth.add(pnlLegend, BorderLayout.WEST);

        // Summary footer
        lblSummary = new JLabel("0 mặt hàng | Tham chiếu: Hiện tại");
        lblSummary.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSummary.setForeground(TEXT_GRAY);
        lblSummary.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        pnlSouth.add(lblSummary, BorderLayout.EAST);

        mainPanel.add(pnlSouth, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        // Toggle chế độ xem và enable/disable txtNgay
        rbTonHienTai.addActionListener(e -> toggleMode(true));
        rbTonTaiNgay.addActionListener(e -> toggleMode(false));

        // Xem báo cáo
        btnXem.addActionListener(e -> loadData());
        txtNgay.addActionListener(e -> loadData());

        // Lọc tìm kiếm theo tên hoặc mã
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Sắp xếp qua combobox
        cbSort.addActionListener(e -> applySort());

        // Xuất Excel
        btnExcel.addActionListener(e -> xuatBaoCaoExcel());
        
        // Hover effect cho nút Xem
        btnXem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnXem.setBackground(PRIMARY_DARK); }
            @Override
            public void mouseExited(MouseEvent e) { btnXem.setBackground(PRIMARY); }
        });
    }

    private void toggleMode(boolean hienTai) {
        this.isHienTaiMode = hienTai;
        txtNgay.setEnabled(!hienTai);
        btnXem.setEnabled(true);
        
        if (hienTai) {
            lblInfoText.setText("Hiển thị tồn kho hiện tại — số lượng thực tế hiện đang lưu trữ tại các kho của hệ thống.");
            lblLegendLowStock.setVisible(true);
            cbSort.setModel(new DefaultComboBoxModel<>(new String[]{"Tên mặt hàng", "Mã hàng", "Tồn hiện tại"}));
        } else {
            String ngay = txtNgay.getText().trim();
            lblInfoText.setText("Hiển thị tồn kho tại ngày " + (ngay.isEmpty() ? "tham chiếu" : ngay) 
                + " — tính bằng tồn hiện tại + tổng xuất sau ngày - tổng nhập sau ngày.");
            cbSort.setModel(new DefaultComboBoxModel<>(new String[]{"Tên mặt hàng", "Mã hàng", "Tồn hiện tại", "Biến động"}));
        }
    }

    /** Lấy dữ liệu từ database thông qua controller và đưa lên bảng */
    private void loadData() {
        String ngayStr = txtNgay.getText().trim();
        
        // Cập nhật banner trước
        if (!isHienTaiMode) {
            lblInfoText.setText("Hiển thị tồn kho tại ngày " + (ngayStr.isEmpty() ? "tham chiếu" : ngayStr) 
                + " — tính bằng tồn hiện tại + tổng xuất sau ngày - tổng nhập sau ngày.");
        }

        btnXem.setEnabled(false);
        btnXem.setText("Đang tải...");

        new Thread(() -> {
            try {
                List<TonKhoReportItem> data = controller.getBaoCaoTonKho(isHienTaiMode, ngayStr);
                
                SwingUtilities.invokeLater(() -> {
                    this.reportData = data;
                    renderTable();
                    btnXem.setEnabled(true);
                    btnXem.setText("🔍 Xem");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi khi tải dữ liệu báo cáo:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    btnXem.setEnabled(true);
                    btnXem.setText("🔍 Xem");
                });
            }
        }).start();
    }

    /** Vẽ lại JTable dựa trên chế độ xem */
    private void renderTable() {
        tableSorter.setRowFilter(null); // Reset filter cũ
        txtSearch.setText("");

        // Khởi tạo các cột
        if (isHienTaiMode) {
            tableModel.setColumnIdentifiers(new Object[]{"Mã hàng", "Tên hàng", "Mô tả", "Tồn hiện tại"});
        } else {
            String ngayStr = txtNgay.getText().trim();
            tableModel.setColumnIdentifiers(new Object[]{
                "Mã hàng", "Tên hàng", "Mô tả", "Tồn tại ngày " + ngayStr, "Tồn hiện tại", "Biến động"
            });
        }

        tableModel.setRowCount(0);
        for (TonKhoReportItem item : reportData) {
            if (isHienTaiMode) {
                tableModel.addRow(new Object[]{
                    item.getMaHang(),
                    item.getTenHang(),
                    item.getMoTa(),
                    item.getTonHienTai()
                });
            } else {
                tableModel.addRow(new Object[]{
                    item.getMaHang(),
                    item.getTenHang(),
                    item.getMoTa(),
                    item.getTonTaiNgay(),
                    item.getTonHienTai(),
                    item.getBienDong()
                });
            }
        }

        // Định cấu hình custom rendering và căn lề
        setupTableRenderers();
        
        // Cập nhật footer status
        if (isHienTaiMode) {
            lblSummary.setText(reportData.size() + " mặt hàng | Tham chiếu: Hiện tại");
        } else {
            lblSummary.setText(reportData.size() + " mặt hàng | Tham chiếu: " + txtNgay.getText().trim());
        }

        // Sắp xếp mặc định theo tên hàng
        cbSort.setSelectedIndex(0);
        applySort();
    }

    /** Thiết lập căn lề, hiển thị biến động màu sắc, và cảnh báo tồn kho thấp */
    private void setupTableRenderers() {
        // Căn giữa cột Mã hàng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblReport.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblReport.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Độ rộng cột Tên hàng
        tblReport.getColumnModel().getColumn(1).setPreferredWidth(200);

        // Custom renderer cho cột Tồn hiện tại
        final int tonHienTaiColIndex = isHienTaiMode ? 3 : 4;
        DefaultTableCellRenderer tonHienTaiRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                
                try {
                    int val = Integer.parseInt(value.toString());
                    if (val < 20) {
                        // Tồn kho thấp (< 20) -> Tô màu cảnh báo
                        c.setBackground(isSelected ? WARNING_BG_SEL : WARNING_BG);
                        c.setForeground(TEXT_DARK);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    }
                } catch (Exception e) {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                return c;
            }
        };
        tblReport.getColumnModel().getColumn(tonHienTaiColIndex).setCellRenderer(tonHienTaiRenderer);

        if (!isHienTaiMode) {
            // Căn phải cột Tồn tại ngày tham chiếu
            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            tblReport.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

            // Custom renderer cho cột Biến động
            DefaultTableCellRenderer bienDongRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(getFont().deriveFont(Font.BOLD));

                    try {
                        int val = Integer.parseInt(value.toString());
                        if (val > 0) {
                            setText("↑ +" + val);
                            setForeground(INCREASE_COLOR);
                        } else if (val < 0) {
                            setText("↓ " + val);
                            setForeground(DECREASE_COLOR);
                        } else {
                            setText("-");
                            setForeground(TEXT_GRAY);
                        }
                    } catch (Exception e) {
                        setText(value != null ? value.toString() : "");
                        setForeground(TEXT_DARK);
                    }

                    if (isSelected) {
                        setBackground(table.getSelectionBackground());
                    } else {
                        setBackground(table.getBackground());
                    }

                    return c;
                }
            };
            tblReport.getColumnModel().getColumn(5).setCellRenderer(bienDongRenderer);
        }
    }

    /** Lọc dữ liệu hiển thị trên bảng theo tên hoặc mã hàng */
    private void filterTable() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            // Lọc không phân biệt chữ hoa/thường trên cột Mã hàng (0) và Tên hàng (1)
            tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
        }
    }

    /** Áp dụng sắp xếp khi thay đổi Combobox */
    private void applySort() {
        int selectIndex = cbSort.getSelectedIndex();
        if (selectIndex < 0) return;

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        if (selectIndex == 0) { // Tên mặt hàng
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        } else if (selectIndex == 1) { // Mã hàng
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        } else if (selectIndex == 2) { // Tồn hiện tại
            int colIndex = isHienTaiMode ? 3 : 4;
            sortKeys.add(new RowSorter.SortKey(colIndex, SortOrder.DESCENDING));
        } else if (selectIndex == 3 && !isHienTaiMode) { // Biến động
            sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING));
        }

        tableSorter.setSortKeys(sortKeys);
    }

    /** Thực hiện xuất dữ liệu báo cáo ra file Excel */
    private void xuatBaoCaoExcel() {
        if (reportData == null || reportData.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Không có dữ liệu để xuất báo cáo.", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo Excel");
        
        String defaultName = "BaoCaoTonKho_" + (isHienTaiMode ? "HienTai" : txtNgay.getText().trim().replace("/", "")) + ".csv";
        fileChooser.setSelectedFile(new File(defaultName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Đảm bảo định dạng file là csv
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                controller.xuatExcel(reportData, fileToSave, isHienTaiMode, txtNgay.getText().trim());
                JOptionPane.showMessageDialog(this,
                    "Xuất báo cáo thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất file:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
