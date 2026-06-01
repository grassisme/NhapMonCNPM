package com.qlxnh.view;

import com.qlxnh.controller.PhieuXuatController;
import com.qlxnh.entity.CTPhieuXuat;
import com.qlxnh.entity.DaiLyCon;
import com.qlxnh.entity.HangHoa;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

/**
 * Form chi tiết xuất (modal dialog) — theo UML ChiTietXuatFrm.
 *
 * Mục đích: cho người dùng chọn 1 mặt hàng + đại lý nhận + số lượng + đơn giá,
 * và HIỂN THỊ TỒN HIỆN TẠI để biết còn xuất được bao nhiêu.
 *
 * Khi bấm "Đồng ý":
 *   1. Validate dữ liệu form
 *   2. Gọi controller.kiemTraTon() để chắc chắn tồn đủ
 *   3. Đóng dialog, PhieuXuatFrm lấy kết quả qua getResult()
 *
 * Tách form thành dialog riêng (theo UML) thay vì sửa inline trong JTable
 * giúp kiểm tra tồn chặt hơn và UX rõ ràng hơn cho người dùng.
 */
public class ChiTietXuatFrm extends JDialog {

    private final PhieuXuatController controller;
    private final List<HangHoa> listHangHoa;
    private final List<DaiLyCon> listDaiLy;

    /** Kết quả trả về cho PhieuXuatFrm. null nếu người dùng Hủy. */
    private CTPhieuXuat result;

    // ===== UI components =====
    private JComboBox<HangHoa>  cbHangHoa;
    private JComboBox<DaiLyCon> cbDaiLy;
    private JLabel              lblTonHienTai;
    private JSpinner            spnSoLuong;
    private JTextField          txtDonGia;
    private JLabel              lblThanhTien;
    private JButton             btnDongY;
    private JButton             btnHuy;

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getInstance(Locale.of("vi", "VN"));

    public ChiTietXuatFrm(Window owner,
                          PhieuXuatController controller,
                          List<HangHoa> listHangHoa,
                          List<DaiLyCon> listDaiLy) {
        super(owner, "Thêm chi tiết phiếu xuất", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.listHangHoa = listHangHoa;
        this.listDaiLy = listDaiLy;
        initComponents();
        capNhatTonHienTai();   // hiện tồn của hàng đầu tiên
        capNhatThanhTien();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(460, 320);

        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        // ---- Hàng hóa ----
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        pnlMain.add(new JLabel("Hàng hóa:"), gc);

        cbHangHoa = new JComboBox<>(listHangHoa.toArray(new HangHoa[0]));
        cbHangHoa.addActionListener(e -> capNhatTonHienTai());
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(cbHangHoa, gc);

        // ---- Tồn hiện tại (read-only label) ----
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        pnlMain.add(new JLabel("Tồn hiện tại:"), gc);

        lblTonHienTai = new JLabel("0");
        lblTonHienTai.setFont(lblTonHienTai.getFont().deriveFont(Font.BOLD));
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(lblTonHienTai, gc);

        // ---- Đại lý con ----
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        pnlMain.add(new JLabel("Đại lý nhận:"), gc);

        cbDaiLy = new JComboBox<>(listDaiLy.toArray(new DaiLyCon[0]));
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(cbDaiLy, gc);

        // ---- Số lượng ----
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        pnlMain.add(new JLabel("Số lượng:"), gc);

        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        spnSoLuong.addChangeListener(e -> capNhatThanhTien());
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(spnSoLuong, gc);

        // ---- Đơn giá ----
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        pnlMain.add(new JLabel("Đơn giá:"), gc);

        txtDonGia = new JTextField("0");
        txtDonGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { capNhatThanhTien(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { capNhatThanhTien(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { capNhatThanhTien(); }
        });
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(txtDonGia, gc);

        // ---- Thành tiền (read-only) ----
        gc.gridx = 0; gc.gridy = 5; gc.weightx = 0;
        pnlMain.add(new JLabel("Thành tiền:"), gc);

        lblThanhTien = new JLabel("0 đ");
        lblThanhTien.setFont(lblThanhTien.getFont().deriveFont(Font.BOLD, 14f));
        gc.gridx = 1; gc.weightx = 1;
        pnlMain.add(lblThanhTien, gc);

        add(pnlMain, BorderLayout.CENTER);

        // ---- Footer buttons ----
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> {
            result = null;
            dispose();
        });
        btnDongY = new JButton("Đồng ý");
        btnDongY.addActionListener(e -> xacNhan());
        pnlBtn.add(btnHuy);
        pnlBtn.add(btnDongY);
        add(pnlBtn, BorderLayout.SOUTH);
    }

    /** Lấy số tồn của hàng đang chọn từ controller và update label. */
    private void capNhatTonHienTai() {
        HangHoa hh = (HangHoa) cbHangHoa.getSelectedItem();
        if (hh == null) return;
        try {
            int ton = controller.layTonKho(hh.getId());
            lblTonHienTai.setText(String.valueOf(ton));
            lblTonHienTai.setForeground(ton > 0 ? new Color(0x18, 0x6A, 0x3B) : Color.RED);
        } catch (SQLException ex) {
            lblTonHienTai.setText("?");
        }
    }

    private void capNhatThanhTien() {
        int sl = (Integer) spnSoLuong.getValue();
        BigDecimal dg = parseDonGia();
        BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));
        lblThanhTien.setText(MONEY_FORMAT.format(tt) + " đ");
    }

    private BigDecimal parseDonGia() {
        try {
            String raw = txtDonGia.getText().trim().replace(",", "").replace(".", "");
            if (raw.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private void xacNhan() {
        HangHoa hh = (HangHoa) cbHangHoa.getSelectedItem();
        DaiLyCon dl = (DaiLyCon) cbDaiLy.getSelectedItem();
        int sl = (Integer) spnSoLuong.getValue();
        BigDecimal dg = parseDonGia();

        if (hh == null) {
            JOptionPane.showMessageDialog(this, "Chua chon hang hoa.",
                "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dl == null) {
            JOptionPane.showMessageDialog(this, "Chua chon dai ly.",
                "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "So luong phai > 0.",
                "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dg.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Don gia phai >= 0.",
                "Loi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra tồn thật ngay tại đây (chống tình huống tồn vừa bị thay đổi
        // ở một phiên làm việc khác). Đây là chốt chặn sớm — DAO vẫn check lại
        // trong transaction lúc lưu.
        try {
            if (!controller.kiemTraTon(hh.getMaHang(), sl)) {
                JOptionPane.showMessageDialog(this,
                    "Ton kho khong du de xuat (con " + lblTonHienTai.getText()
                    + ", can " + sl + ").",
                    "Khong du ton", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Loi kiem tra ton kho:\n" + ex.getMessage(),
                "Loi CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }

        result = new CTPhieuXuat(hh, dl, sl, dg);
        dispose();
    }

    /** PhieuXuatFrm gọi sau khi dialog đóng để lấy dòng vừa nhập. null = Hủy. */
    public CTPhieuXuat getResult() {
        return result;
    }
}
