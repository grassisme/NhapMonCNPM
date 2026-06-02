package com.qlxnh.view;

import com.qlxnh.entity.LichSuPhieu;
import com.qlxnh.util.DBConnection;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog xem chi tiết một phiếu nhập/xuất (UC-04).
 *
 * Mở khi người dùng double-click một dòng trong LichSuPhieuForm.
 * Đọc các dòng chi tiết từ view tương ứng:
 *   - Phiếu nhập → v_CTPhieuNhap_FullInfo (kèm tên NCC)
 *   - Phiếu xuất → v_CTPhieuXuat_FullInfo (kèm tên đại lý)
 */
public class ChiTietPhieuDialog extends JDialog {

    private static final Color PRIMARY   = new Color(0x18, 0x5F, 0xA5);
    private static final Color BORDER_C  = new Color(0xD6, 0xDC, 0xE4);
    private static final NumberFormat MONEY = NumberFormat.getInstance(new Locale("vi", "VN"));

    public ChiTietPhieuDialog(Frame owner, LichSuPhieu phieu) {
        super(owner, "Chi tiết phiếu " + phieu.getMaPhieu(), true);
        boolean laNhap = "NHAP".equals(phieu.getLoai());

        setSize(720, 420);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        // ── NORTH: thông tin chung của phiếu ──────────────
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 8));
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(6, 12, 0, 12));
        pnlInfo.add(infoLabel("Mã phiếu: " + phieu.getMaPhieu()));
        pnlInfo.add(infoLabel("Loại: " + (laNhap ? "Phiếu nhập" : "Phiếu xuất")));
        pnlInfo.add(infoLabel("Ngày: " + phieu.getNgay().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        pnlInfo.add(infoLabel("Người lập: " + phieu.getNguoiLap()));
        add(pnlInfo, BorderLayout.NORTH);

        // ── CENTER: bảng dòng chi tiết ────────────────────
        String cotDoiTac = laNhap ? "Nhà cung cấp" : "Đại lý nhận";
        String[] cols = {"Mã hàng", "Tên hàng", cotDoiTac, "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(model);
        tbl.setRowHeight(26);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(PRIMARY);
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.setGridColor(BORDER_C);

        loadChiTiet(model, phieu.getId(), laNhap);

        // Căn phải cột Số lượng, Đơn giá, Thành tiền
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        tbl.getColumnModel().getColumn(3).setCellRenderer(right);
        tbl.getColumnModel().getColumn(4).setCellRenderer(right);
        tbl.getColumnModel().getColumn(5).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(scroll, BorderLayout.CENTER);

        // ── SOUTH: tổng tiền + nút Đóng ───────────────────
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        JLabel lblTong = new JLabel("Tổng tiền: " + MONEY.format(phieu.getTongTien()) + " đ");
        lblTong.setFont(lblTong.getFont().deriveFont(Font.BOLD, 15f));
        lblTong.setForeground(PRIMARY);
        pnlFooter.add(lblTong, BorderLayout.WEST);

        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        pnlFooter.add(btnDong, BorderLayout.EAST);

        add(pnlFooter, BorderLayout.SOUTH);
    }

    /** Đọc các dòng chi tiết của phiếu từ view tương ứng và đổ vào model. */
    private void loadChiTiet(DefaultTableModel model, int phieuId, boolean laNhap) {
        String sql = laNhap
            ? "SELECT maHang, tenHang, tenNCC, soLuong, donGia, thanhTien "
              + "FROM v_CTPhieuNhap_FullInfo WHERE phieuNhapId = ?"
            : "SELECT maHang, tenHang, tenDL, soLuong, donGia, thanhTien "
              + "FROM v_CTPhieuXuat_FullInfo WHERE phieuXuatId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phieuId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("maHang"),
                        rs.getString("tenHang"),
                        rs.getString(3), // tenNCC hoặc tenDL
                        rs.getInt("soLuong"),
                        MONEY.format(rs.getBigDecimal("donGia")) + " đ",
                        MONEY.format(rs.getBigDecimal("thanhTien")) + " đ"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi tải chi tiết phiếu:\n" + ex.getMessage(),
                "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }
}
