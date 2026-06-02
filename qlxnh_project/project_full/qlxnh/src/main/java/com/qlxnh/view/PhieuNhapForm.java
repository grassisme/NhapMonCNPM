package com.qlxnh.view;

import com.qlxnh.controller.PhieuNhapController;
import com.qlxnh.entity.CTPhieuNhap;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.NhaCungCap;
import com.qlxnh.entity.PhieuNhap;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Form Lập phiếu nhập.
 *
 * Bố cục (BorderLayout của JFrame):
 *   - NORTH:  Panel header (Mã phiếu, Ngày nhập, Người lập)
 *   - CENTER: JTable dòng chi tiết + JScrollPane
 *   - SOUTH:  Panel footer (Thêm dòng, Tổng tiền, Hủy, Lưu)
 *
 * JTable dùng DefaultTableModel có editable cells để người dùng nhập
 * trực tiếp số lượng và đơn giá. Cột Hàng hóa và NCC dùng JComboBox
 * trong cell editor — chuẩn cách làm form nhập liệu trong Swing.
 *
 * Lưu ý quan trọng:
 *   - Thành tiền tự tính khi sửa số lượng hoặc đơn giá (TableModelListener).
 *   - Tổng tiền cập nhật mỗi lần Thành tiền của một dòng đổi.
 */
public class PhieuNhapForm extends JFrame {

    /** Người đang đăng nhập (truyền vào từ Main hoặc cửa sổ chính). */
    private final int currentUserId;
    private final String currentUserName;

    private final PhieuNhapController controller = new PhieuNhapController();

    // ===== UI components =====
    private JLabel       lblMaPhieu;
    private JTextField   txtMaPhieu;
    private JLabel       lblNgayNhap;
    private JTextField   txtNgayNhap; // ngày dạng dd/MM/yyyy
    private JLabel       lblNguoiLap;
    private JTextField   txtNguoiLap;

    private JTable       tblChiTiet;
    private DefaultTableModel modelChiTiet;

    private JButton      btnThemDong;
    private JButton btnXoaDong;
    private JLabel       lblTongTienLabel;
    private JLabel       lblTongTien;
    private JButton      btnHuy;
    private JButton      btnLuu;

    // ===== Dữ liệu nguồn cho combobox =====
    private List<HangHoa>    listHangHoa;
    private List<NhaCungCap> listNCC;

    // Cột Index trong JTable — đặt hằng số cho dễ đọc
    private static final int COL_HANGHOA    = 0;
    private static final int COL_NCC        = 1;
    private static final int COL_SOLUONG    = 2;
    private static final int COL_DONGIA     = 3;
    private static final int COL_THANHTIEN  = 4;

    // Formatter định dạng tiền VND có dấu chấm phân nhóm
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    public PhieuNhapForm(int currentUserId, String currentUserName) {
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }

    /** Dựng toàn bộ UI. Nếu sau này dùng GUI Builder, đoạn này sẽ thay bằng
     *  vùng "Generated Code" do NetBeans sinh. */
    private void initComponents() {
        setTitle("Lập phiếu nhập");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                xacNhanDong();
            }
        });
        setSize(820, 480);
        setLayout(new BorderLayout(8, 8));

        // ---------- HEADER ----------
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));

        lblMaPhieu = new JLabel("Mã phiếu:");
        txtMaPhieu = new JTextField("(tự sinh khi lưu)", 12);
        txtMaPhieu.setEditable(false);

        lblNgayNhap = new JLabel("Ngày nhập:");
        txtNgayNhap = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 10);

        lblNguoiLap = new JLabel("Người lập:");
        txtNguoiLap = new JTextField(currentUserName, 18);
        txtNguoiLap.setEditable(false);

        pnlHeader.add(lblMaPhieu);   pnlHeader.add(txtMaPhieu);
        pnlHeader.add(lblNgayNhap);  pnlHeader.add(txtNgayNhap);
        pnlHeader.add(lblNguoiLap);  pnlHeader.add(txtNguoiLap);

        add(pnlHeader, BorderLayout.NORTH);

        // ---------- TABLE CHI TIẾT ----------
        String[] columns = {"Hàng hóa", "Nhà cung cấp", "Số lượng", "Đơn giá", "Thành tiền"};
        modelChiTiet = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != COL_THANHTIEN; // Thành tiền chỉ đọc (auto tính)
            }
            @Override
            public Class<?> getColumnClass(int col) {
                // Để JTable hiển thị số lượng/đơn giá đúng kiểu
                if (col == COL_SOLUONG)    return Integer.class;
                if (col == COL_DONGIA)     return BigDecimal.class;
                if (col == COL_THANHTIEN)  return BigDecimal.class;
                return Object.class;
            }
        };

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(26);

        // Khi người dùng sửa SỐ LƯỢNG hoặc ĐƠN GIÁ -> tự tính lại thành tiền + tổng
        modelChiTiet.addTableModelListener(e -> {
            if (e.getColumn() == COL_SOLUONG || e.getColumn() == COL_DONGIA) {
                int row = e.getFirstRow();
                capNhatThanhTienDong(row);
                capNhatTongTien();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblChiTiet);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(scrollPane, BorderLayout.CENTER);

        // ---------- FOOTER ----------
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        // Trái: nút Thêm dòng & nút Xóa Dòng
        JPanel pnlFooterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnThemDong = new JButton("+ Thêm dòng");
        btnThemDong.addActionListener(e -> themDongMoi());
        pnlFooterLeft.add(btnThemDong);
        btnXoaDong = new JButton("− Xóa dòng");
        btnXoaDong.addActionListener(e -> xoaDongDangChon());
        pnlFooterLeft.add(btnXoaDong);

        // Giữa: tổng tiền
        JPanel pnlFooterCenter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        lblTongTienLabel = new JLabel("Tổng tiền:");
        lblTongTien = new JLabel("0 đ");
        lblTongTien.setFont(lblTongTien.getFont().deriveFont(Font.BOLD, 16f));
        pnlFooterCenter.add(lblTongTienLabel);
        pnlFooterCenter.add(lblTongTien);

        // Phải: Hủy / Lưu
        JPanel pnlFooterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> xacNhanDong());
        btnLuu = new JButton("Lưu phiếu");
        btnLuu.addActionListener(e -> luuPhieu());
        pnlFooterRight.add(btnHuy);
        pnlFooterRight.add(btnLuu);

        pnlFooter.add(pnlFooterLeft,   BorderLayout.WEST);
        pnlFooter.add(pnlFooterCenter, BorderLayout.CENTER);
        pnlFooter.add(pnlFooterRight,  BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }
    /** Xử lý chức năng xóa dòng */
    private void xoaDongDangChon() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một dòng để xóa.",
                    "Chưa chọn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modelChiTiet.removeRow(row);
        capNhatTongTien();
    }

    /** Xử lý chức năng xác nhận đóng */
    private void xacNhanDong() {
        // Nếu chưa nhập dòng nào thì đóng thẳng, không cần hỏi
        if (modelChiTiet.getRowCount() == 0) {
            dispose();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Phiếu chưa được lưu. Bạn có chắc muốn bỏ qua?",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    /** Tải dữ liệu nguồn (hàng hóa, NCC) + gắn editor combobox cho 2 cột. */
    private void loadData() {
        try {
            listHangHoa = controller.layDanhSachHangHoa();
            listNCC     = controller.layDanhSachNCC();

            // Combobox hàng hóa
            JComboBox<HangHoa> cbHangHoa = new JComboBox<>(listHangHoa.toArray(new HangHoa[0]));
            tblChiTiet.getColumnModel().getColumn(COL_HANGHOA)
                      .setCellEditor(new DefaultCellEditor(cbHangHoa));

            // Combobox NCC
            JComboBox<NhaCungCap> cbNCC = new JComboBox<>(listNCC.toArray(new NhaCungCap[0]));
            tblChiTiet.getColumnModel().getColumn(COL_NCC)
                      .setCellEditor(new DefaultCellEditor(cbNCC));

            // Mặc định thêm sẵn 1 dòng trống để người dùng nhập ngay
            themDongMoi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Không lấy được danh sách hàng hóa/NCC:\n" + ex.getMessage(),
                "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themDongMoi() {
        if (listHangHoa == null || listNCC == null) return;
        modelChiTiet.addRow(new Object[]{
            listHangHoa.get(0),
            listNCC.get(0),
            1,                  // số lượng mặc định
            BigDecimal.ZERO,    // đơn giá mặc định
            BigDecimal.ZERO     // thành tiền (sẽ tính lại)
        });
        int newRow = modelChiTiet.getRowCount() - 1;
        capNhatThanhTienDong(newRow);
        capNhatTongTien();
    }

    private void capNhatThanhTienDong(int row) {
        Object sl  = modelChiTiet.getValueAt(row, COL_SOLUONG);
        Object dg  = modelChiTiet.getValueAt(row, COL_DONGIA);
        int soLuong = (sl instanceof Integer) ? (Integer) sl : 0;
        BigDecimal donGia = (dg instanceof BigDecimal) ? (BigDecimal) dg : BigDecimal.ZERO;
        BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

        // Tạm gỡ listener để tránh đệ quy khi setValueAt
        modelChiTiet.setValueAt(thanhTien, row, COL_THANHTIEN);
    }

    private void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            Object tt = modelChiTiet.getValueAt(i, COL_THANHTIEN);
            if (tt instanceof BigDecimal) {
                tong = tong.add((BigDecimal) tt);
            }
        }
        lblTongTien.setText(MONEY_FORMAT.format(tong) + " đ");
    }

    private void luuPhieu() {
        try {
            // 1. Parse ngày từ textbox
            LocalDate ngay;
            try {
                ngay = LocalDate.parse(txtNgayNhap.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Ngày không hợp lệ. Định dạng đúng: dd/MM/yyyy",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Dừng cell editing nếu người dùng đang sửa dở
            if (tblChiTiet.isEditing()) {
                tblChiTiet.getCellEditor().stopCellEditing();
            }

            // 3. Đóng gói PhieuNhap từ form
            PhieuNhap phieu = new PhieuNhap();
            phieu.setNgayNhap(ngay);
            phieu.setNguoiDungId(currentUserId);

            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                CTPhieuNhap ct = new CTPhieuNhap();
                ct.setHangHoa((HangHoa) modelChiTiet.getValueAt(i, COL_HANGHOA));
                ct.setNhaCungCap((NhaCungCap) modelChiTiet.getValueAt(i, COL_NCC));
                ct.setSoLuong((Integer) modelChiTiet.getValueAt(i, COL_SOLUONG));
                Object dg = modelChiTiet.getValueAt(i, COL_DONGIA);
                ct.setDonGia(dg instanceof BigDecimal ? (BigDecimal) dg : BigDecimal.ZERO);
                phieu.getChiTietList().add(ct);
            }

            // 4. Gọi Controller lưu
            int idMoi = controller.luuPhieu(phieu);

            JOptionPane.showMessageDialog(this,
                "Lưu phiếu nhập thành công! Mã phiếu: PN" + String.format("%04d", idMoi),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            // Lỗi validate -> hiện cho người dùng
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi CSDL khi lưu phiếu:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
