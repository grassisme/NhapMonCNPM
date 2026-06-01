package com.qlxnh.view;

import com.qlxnh.controller.PhieuXuatController;
import com.qlxnh.entity.CTPhieuXuat;
import com.qlxnh.entity.DaiLyCon;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.PhieuXuat;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Form Lập phiếu xuất — theo UML PhieuXuatFrm.
 *
 * Bố cục:
 *   - NORTH:  Header (Mã phiếu, Ngày xuất, Người lập)
 *   - CENTER: Bảng các dòng chi tiết (read-only, được nạp từ ChiTietXuatFrm)
 *   - SOUTH:  Footer (Thêm chi tiết, Xóa dòng, Tổng tiền, Hủy, Lưu)
 *
 * Khác với PhieuNhapForm (nhập trực tiếp trên JTable), PhieuXuatFrm dùng
 * ChiTietXuatFrm làm dialog riêng để THÊM TỪNG DÒNG. Lý do:
 *   - Cần hiển thị TỒN HIỆN TẠI cho người dùng trước khi xuất
 *   - Có chốt chặn kiểm tra tồn ngay khi thêm, không đợi tới khi Lưu
 *   - Khớp đúng quan hệ "PhieuXuatFrm --> ChiTietXuatFrm" trong UML
 */
public class PhieuXuatFrm extends JFrame {

    private final int currentUserId;
    private final String currentUserName;
    private final PhieuXuatController controller = new PhieuXuatController();

    // ===== UI =====
    private JTextField   txtMaPhieu;
    private JTextField   txtNgayXuat;
    private JTextField   txtNguoiLap;

    private JTable             tblChiTiet;
    private DefaultTableModel  modelChiTiet;

    private JButton      btnThemChiTiet;
    private JButton      btnXoaDong;
    private JLabel       lblTongTien;
    private JButton      btnHuy;
    private JButton      btnLuu;

    // ===== Dữ liệu nguồn =====
    private List<HangHoa>  listHangHoa;
    private List<DaiLyCon> listDaiLy;

    // Cột bảng
    private static final int COL_HANGHOA   = 0;
    private static final int COL_DAILY     = 1;
    private static final int COL_SOLUONG   = 2;
    private static final int COL_DONGIA    = 3;
    private static final int COL_THANHTIEN = 4;

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getInstance(Locale.of("vi", "VN"));

    public PhieuXuatFrm(int currentUserId, String currentUserName) {
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Lập phiếu xuất");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(840, 500);
        setLayout(new BorderLayout(8, 8));

        // ---------- HEADER ----------
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));

        pnlHeader.add(new JLabel("Mã phiếu:"));
        txtMaPhieu = new JTextField("(tự sinh khi lưu)", 12);
        txtMaPhieu.setEditable(false);
        pnlHeader.add(txtMaPhieu);

        pnlHeader.add(new JLabel("Ngày xuất:"));
        txtNgayXuat = new JTextField(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 10);
        pnlHeader.add(txtNgayXuat);

        pnlHeader.add(new JLabel("Người lập:"));
        txtNguoiLap = new JTextField(currentUserName, 18);
        txtNguoiLap.setEditable(false);
        pnlHeader.add(txtNguoiLap);

        add(pnlHeader, BorderLayout.NORTH);

        // ---------- TABLE ----------
        String[] columns = {"Hàng hóa", "Đại lý nhận", "Số lượng", "Đơn giá", "Thành tiền"};
        modelChiTiet = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == COL_SOLUONG)   return Integer.class;
                if (col == COL_DONGIA)    return BigDecimal.class;
                if (col == COL_THANHTIEN) return BigDecimal.class;
                return Object.class;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(26);

        JScrollPane scrollPane = new JScrollPane(tblChiTiet);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(scrollPane, BorderLayout.CENTER);

        // ---------- FOOTER ----------
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnThemChiTiet = new JButton("+ Thêm chi tiết");
        btnThemChiTiet.addActionListener(e -> moDialogThemChiTiet());
        btnXoaDong = new JButton("− Xóa dòng");
        btnXoaDong.addActionListener(e -> xoaDongDangChon());
        pnlLeft.add(btnThemChiTiet);
        pnlLeft.add(btnXoaDong);

        JPanel pnlCenter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlCenter.add(new JLabel("Tổng tiền:"));
        lblTongTien = new JLabel("0 đ");
        lblTongTien.setFont(lblTongTien.getFont().deriveFont(Font.BOLD, 16f));
        pnlCenter.add(lblTongTien);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());
        btnLuu = new JButton("Lưu phiếu");
        btnLuu.addActionListener(e -> luuPhieu());
        pnlRight.add(btnHuy);
        pnlRight.add(btnLuu);

        pnlFooter.add(pnlLeft,   BorderLayout.WEST);
        pnlFooter.add(pnlCenter, BorderLayout.CENTER);
        pnlFooter.add(pnlRight,  BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    /** Nạp combobox nguồn: hàng hóa + đại lý. */
    private void loadData() {
        try {
            listHangHoa = controller.layDanhSachHangHoa();
            listDaiLy   = controller.layDanhSachDaiLy();

            if (listHangHoa.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Chua co hang hoa nao trong he thong.",
                    "Canh bao", JOptionPane.WARNING_MESSAGE);
            }
            if (listDaiLy.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Chua co dai ly con nao trong he thong.",
                    "Canh bao", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Khong lay duoc danh sach hang hoa/dai ly:\n" + ex.getMessage(),
                "Loi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Mở ChiTietXuatFrm để thêm một dòng. Khi đóng, lấy result. */
    private void moDialogThemChiTiet() {
        if (listHangHoa == null || listDaiLy == null
                || listHangHoa.isEmpty() || listDaiLy.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Chua co du du lieu (hang hoa hoac dai ly) de tao phieu.",
                "Khong the them", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChiTietXuatFrm dlg = new ChiTietXuatFrm(this, controller, listHangHoa, listDaiLy);
        dlg.setVisible(true);   // modal — chặn ở đây tới khi đóng

        CTPhieuXuat ct = dlg.getResult();
        if (ct == null) return; // người dùng hủy

        modelChiTiet.addRow(new Object[]{
            ct.getHangHoa(),
            ct.getDaiLyCon(),
            ct.getSoLuong(),
            ct.getDonGia(),
            ct.tinhThanhTien()
        });
        capNhatTongTien();
    }

    private void xoaDongDangChon() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Hay chon mot dong de xoa.",
                "Chua chon", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modelChiTiet.removeRow(row);
        capNhatTongTien();
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
            // 1. Parse ngày
            LocalDate ngay;
            try {
                ngay = LocalDate.parse(txtNgayXuat.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Ngay khong hop le. Dinh dang: dd/MM/yyyy",
                    "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Đóng gói PhieuXuat
            PhieuXuat phieu = new PhieuXuat();
            phieu.setNgayXuat(ngay);
            phieu.setNguoiDungId(currentUserId);

            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                CTPhieuXuat ct = new CTPhieuXuat();
                ct.setHangHoa((HangHoa) modelChiTiet.getValueAt(i, COL_HANGHOA));
                ct.setDaiLyCon((DaiLyCon) modelChiTiet.getValueAt(i, COL_DAILY));
                ct.setSoLuong((Integer) modelChiTiet.getValueAt(i, COL_SOLUONG));
                Object dg = modelChiTiet.getValueAt(i, COL_DONGIA);
                ct.setDonGia(dg instanceof BigDecimal ? (BigDecimal) dg : BigDecimal.ZERO);
                phieu.getChiTietList().add(ct);
            }

            // 3. Gọi controller (đã validate + transaction trong DAO)
            int idMoi = controller.luuPhieu(phieu);

            JOptionPane.showMessageDialog(this,
                "Luu phieu xuat thanh cong! Ma phieu: PX" + String.format("%04d", idMoi),
                "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Du lieu chua hop le", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException ex) {
            // ném từ DAO khi tồn không đủ
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Khong du ton kho", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Loi CSDL khi luu phieu:\n" + ex.getMessage(),
                "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
