package com.qlxnh.view;
import com.qlxnh.controller.DanhMucController;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.NhaCungCap;
import com.qlxnh.entity.DaiLyCon;
import com.qlxnh.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DanhMucView extends JFrame {
    private DanhMucController controller;
    private JTabbedPane tabbedPane;

    // Các thành phần giao diện Tab Hàng hóa
    private JTextField txtSearchHH, txtMaHH, txtTenHH, txtMoTaHH;
    private JTable tableHH;
    private DefaultTableModel modelHH;
    private int selectedIdHH = -1;

    // Các thành phần giao diện Tab Nhà cung cấp
    private JTextField txtSearchNCC, txtMaNCC, txtTenNCC, txtDiaChiNCC, txtSdtNCC;
    private JTable tableNCC;
    private DefaultTableModel modelNCC;
    private int selectedIdNCC = -1;

    // Các thành phần giao diện Tab Đại lý con
    private JTextField txtSearchDL, txtMaDL, txtTenDL, txtDiaChiDL, txtSdtDL;
    private JTable tableDL;
    private DefaultTableModel modelDL;
    private int selectedIdDL = -1;

    // Biến nút cần kiểm soát theo vai trò
    private JButton btnAddHH, btnEditHH, btnDeleteHH;
    private JButton btnAddNCC, btnEditNCC, btnDeleteNCC;
    private JButton btnAddDL, btnEditDL, btnDeleteDL;

    public DanhMucView() {
        controller = new DanhMucController();
        setTitle("HỆ THỐNG QUẢN LÝ XUẤT NHẬP HÀNG - QUẢN LÝ DANH MỤC");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Khởi tạo 3 phân hệ tab 
        initTabHangHoa();
        initTabNhaCungCap();
        initTabDaiLyCon();

        // Phân quyền sau khi tất cả tab đã khởi tạo xong
        boolean coQuyen = Session.isQuanLyKho() || Session.isNhanVien();
        boolean laQuanLy = Session.isQuanLyKho();

        // Tab Hàng hóa
        btnAddHH.setVisible(coQuyen);
        btnEditHH.setVisible(coQuyen);
        btnDeleteHH.setVisible(laQuanLy);

        // Tab Nhà cung cấp
        btnAddNCC.setVisible(coQuyen);
        btnEditNCC.setVisible(coQuyen);
        btnDeleteNCC.setVisible(laQuanLy);

        // Tab Đại lý con
        btnAddDL.setVisible(coQuyen);
        btnEditDL.setVisible(coQuyen);
        btnDeleteDL.setVisible(laQuanLy);

        add(tabbedPane);
        loadDataHangHoa("");
        loadDataNCC("");
        loadDataDaiLy("");
    }

    // ================= GIAO DIỆN PHÂN HỆ HÀNG HÓA =================
    private void initTabHangHoa() {
        JPanel panel = new JPanel(new BorderLayout());

        // Form nhập liệu phía trên
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Hàng hóa"));
        formPanel.add(new JLabel("  Mã hàng hóa (*):"));
        txtMaHH = new JTextField();
        formPanel.add(txtMaHH);
        formPanel.add(new JLabel("  Tên hàng hóa (*):"));
        txtTenHH = new JTextField();
        formPanel.add(txtTenHH);
        formPanel.add(new JLabel("  Mô tả:"));
        txtMoTaHH = new JTextField();
        formPanel.add(txtMoTaHH);
        formPanel.add(new JLabel("  Số lượng tồn:"));
        JTextField txtTon = new JTextField("Tự động (Mặc định = 0)");
        txtTon.setEditable(false);
        formPanel.add(txtTon); // Khóa quyền nhập thủ công [cite: 22, 67]

        // Thanh công cụ tìm kiếm và nút chức năng
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearchHH = new JTextField(15);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnAddHH = new JButton("Thêm mới");
        btnEditHH = new JButton("Cập nhật");
        btnDeleteHH = new JButton("Xóa");
        JButton btnClear = new JButton("Làm mới form");

        actionPanel.add(new JLabel("Từ khóa:"));
        actionPanel.add(txtSearchHH);
        actionPanel.add(btnSearch);
        actionPanel.add(Box.createHorizontalStrut(30));
        actionPanel.add(btnAddHH);
        actionPanel.add(btnEditHH);
        actionPanel.add(btnDeleteHH);
        actionPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);

        // Bảng danh sách hiển thị dữ liệu
        modelHH = new DefaultTableModel(new String[]{"ID VẬT LÝ", "MÃ HÀNG HÓA", "TÊN MẶT HÀNG", "MÔ TẢ CHI TIẾT", "SỐ LƯỢNG TỒN KHO"}, 0);
        tableHH = new JTable(modelHH);
        JScrollPane scroll = new JScrollPane(tableHH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        tabbedPane.addTab("Danh mục Hàng hóa", panel);

        // Xử lý Sự kiện Tab Hàng hóa
        btnSearch.addActionListener(e -> loadDataHangHoa(txtSearchHH.getText().trim()));

        btnAddHH.addActionListener(e -> {
            if (txtMaHH.getText().trim().isEmpty() || txtTenHH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng không để trống trường thông tin bắt buộc", "Cảnh báo", JOptionPane.WARNING_MESSAGE); // [cite: 18, 63]
                return;
            }
            HangHoa hh = new HangHoa();
            hh.setMaHang(txtMaHH.getText().trim());
            hh.setTenHang(txtTenHH.getText().trim());
            hh.setMoTa(txtMoTaHH.getText().trim());

            String res = controller.themHangHoa(hh);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Thêm mới thành công!"); // [cite: 16, 61]
                clearFormHH();
                loadDataHangHoa("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi đầu vào dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableHH.getSelectionModel().addListSelectionListener(e -> {
            int row = tableHH.getSelectedRow();
            if (row >= 0) {
                selectedIdHH = Integer.parseInt(modelHH.getValueAt(row, 0).toString());
                txtMaHH.setText(modelHH.getValueAt(row, 1).toString());
                txtMaHH.setEditable(false); // Khóa cứng trường Mã định danh khi chọn để sửa [cite: 27, 72]
                txtTenHH.setText(modelHH.getValueAt(row, 2).toString());
                txtMoTaHH.setText(modelHH.getValueAt(row, 3).toString());
            }
        });

        btnEditHH.addActionListener(e -> {
            if (selectedIdHH == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng cần chỉnh sửa từ danh sách kết quả!");
                return;
            }
            if (txtTenHH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng không để trống trường thông tin bắt buộc");
                return;
            }
            HangHoa hh = new HangHoa(selectedIdHH, txtMaHH.getText().trim(), txtTenHH.getText().trim(), txtMoTaHH.getText().trim(), 0);
            String res = controller.capNhatHangHoa(hh);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công"); // [cite: 29, 74]
                clearFormHH();
                loadDataHangHoa("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteHH.addActionListener(e -> {
            if (selectedIdHH == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng muốn xóa khỏi danh sách!");
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi danh mục này?", "Xác nhận hành động", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                String res = controller.xoaHangHoa(selectedIdHH);
                if (res.equals("Thành công")) {
                    JOptionPane.showMessageDialog(this, "Xóa đối tượng thành công"); // [cite: 38, 83]
                    clearFormHH();
                    loadDataHangHoa("");
                } else {
                    JOptionPane.showMessageDialog(this, res, "Cảnh báo nghiêm trọng", JOptionPane.WARNING_MESSAGE); // [cite: 39, 84]
                }
            }
        });

        btnClear.addActionListener(e -> clearFormHH());
    }

    private void loadDataHangHoa(String tuKhoa) {
        modelHH.setRowCount(0);
        try {
            List<HangHoa> list = controller.timKiemHangHoa(tuKhoa);
            if (list.isEmpty() && !tuKhoa.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy mặt hàng phù hợp", "Thông báo tra cứu", JOptionPane.INFORMATION_MESSAGE); // [cite: 30, 75]
            }
            for (HangHoa hh : list) {
                modelHH.addRow(new Object[]{hh.getId(), hh.getMaHang(), hh.getTenHang(), hh.getMoTa(), hh.getSoLuongTon()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFormHH() {
        selectedIdHH = -1;
        txtMaHH.setText("");
        txtMaHH.setEditable(true);
        txtTenHH.setText("");
        txtMoTaHH.setText("");
        tableHH.clearSelection();
    }

    // ================= GIAO DIỆN PHÂN HỆ NHÀ CUNG CẤP =================
    private void initTabNhaCungCap() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Nhà cung cấp đối tác"));
        formPanel.add(new JLabel("  Mã NCC (*):"));
        txtMaNCC = new JTextField();
        formPanel.add(txtMaNCC);
        formPanel.add(new JLabel("  Tên nhà cung cấp (*):"));
        txtTenNCC = new JTextField();
        formPanel.add(txtTenNCC);
        formPanel.add(new JLabel("  Địa chỉ trụ sở:"));
        txtDiaChiNCC = new JTextField();
        formPanel.add(txtDiaChiNCC);
        formPanel.add(new JLabel("  Số điện thoại (*):"));
        txtSdtNCC = new JTextField();
        formPanel.add(txtSdtNCC);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearchNCC = new JTextField(15);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnAddNCC = new JButton("Thêm mới");
        btnEditNCC = new JButton("Cập nhật");
        btnDeleteNCC = new JButton("Xóa");
        JButton btnClear = new JButton("Làm mới form");

        actionPanel.add(new JLabel("Từ khóa:"));
        actionPanel.add(txtSearchNCC);
        actionPanel.add(btnSearch);
        actionPanel.add(Box.createHorizontalStrut(30));
        actionPanel.add(btnAddNCC);
        actionPanel.add(btnEditNCC);
        actionPanel.add(btnDeleteNCC);
        actionPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);

        modelNCC = new DefaultTableModel(new String[]{"ID VẬT LÝ", "MÃ ĐỐI TÁC", "TÊN PHÁP NHÂN", "ĐỊA CHỈ TRỤ SỞ", "HOTLINE LIÊN HỆ"}, 0);
        tableNCC = new JTable(modelNCC);
        JScrollPane scroll = new JScrollPane(tableNCC);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        tabbedPane.addTab("Danh mục Nhà cung cấp", panel);

        btnSearch.addActionListener(e -> loadDataNCC(txtSearchNCC.getText().trim()));

        btnAddNCC.addActionListener(e -> {
            if (txtMaNCC.getText().trim().isEmpty() || txtTenNCC.getText().trim().isEmpty() || txtSdtNCC.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng không để trống trường thông tin bắt buộc", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            NhaCungCap ncc = new NhaCungCap(0, txtMaNCC.getText().trim(), txtTenNCC.getText().trim(), txtDiaChiNCC.getText().trim(), txtSdtNCC.getText().trim());
            String res = controller.themNCC(ncc);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                clearFormNCC();
                loadDataNCC("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableNCC.getSelectionModel().addListSelectionListener(e -> {
            int row = tableNCC.getSelectedRow();
            if (row >= 0) {
                selectedIdNCC = Integer.parseInt(modelNCC.getValueAt(row, 0).toString());
                txtMaNCC.setText(modelNCC.getValueAt(row, 1).toString());
                txtMaNCC.setEditable(false);
                txtTenNCC.setText(modelNCC.getValueAt(row, 2).toString());
                txtDiaChiNCC.setText(modelNCC.getValueAt(row, 3).toString());
                txtSdtNCC.setText(modelNCC.getValueAt(row, 4).toString());
            }
        });

        btnEditNCC.addActionListener(e -> {
            if (selectedIdNCC == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng cần chỉnh sửa từ danh sách!");
                return;
            }
            NhaCungCap ncc = new NhaCungCap(selectedIdNCC, txtMaNCC.getText().trim(), txtTenNCC.getText().trim(), txtDiaChiNCC.getText().trim(), txtSdtNCC.getText().trim());
            String res = controller.capNhatNCC(ncc);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                clearFormNCC();
                loadDataNCC("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteNCC.addActionListener(e -> {
            if (selectedIdNCC == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng muốn xóa!");
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa đối tác này?", "Xác nhận hành động", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                String res = controller.xoaNCC(selectedIdNCC);
                if (res.equals("Thành công")) {
                    JOptionPane.showMessageDialog(this, "Xóa đối tượng thành công");
                    clearFormNCC();
                    loadDataNCC("");
                } else {
                    JOptionPane.showMessageDialog(this, res, "Cảnh báo nghiêm trọng", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnClear.addActionListener(e -> clearFormNCC());
    }

    private void loadDataNCC(String tuKhoa) {
        modelNCC.setRowCount(0);
        try {
            List<NhaCungCap> list = controller.timKiemNCC(tuKhoa);
            if (list.isEmpty() && !tuKhoa.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhà cung cấp phù hợp", "Thông báo tra cứu", JOptionPane.INFORMATION_MESSAGE);
            }
            for (NhaCungCap ncc : list) {
                modelNCC.addRow(new Object[]{ncc.getId(), ncc.getMaNCC(), ncc.getTenNCC(), ncc.getDiaChi(), ncc.getSoDT()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFormNCC() {
        selectedIdNCC = -1;
        txtMaNCC.setText("");
        txtMaNCC.setEditable(true);
        txtTenNCC.setText("");
        txtDiaChiNCC.setText("");
        txtSdtNCC.setText("");
        tableNCC.clearSelection();
    }

    // ================= GIAO DIỆN PHÂN HỆ ĐẠI LÝ CON =================
    private void initTabDaiLyCon() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Đại lý cấp dưới"));
        formPanel.add(new JLabel("  Mã Đại lý (*):"));
        txtMaDL = new JTextField();
        formPanel.add(txtMaDL);
        formPanel.add(new JLabel("  Tên đại lý con (*):"));
        txtTenDL = new JTextField();
        formPanel.add(txtTenDL);
        formPanel.add(new JLabel("  Địa chỉ giao nhận:"));
        txtDiaChiDL = new JTextField();
        formPanel.add(txtDiaChiDL);
        formPanel.add(new JLabel("  Số điện thoại (*):"));
        txtSdtDL = new JTextField();
        formPanel.add(txtSdtDL);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearchDL = new JTextField(15);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnAddDL = new JButton("Thêm mới");
        btnEditDL = new JButton("Cập nhật");
        btnDeleteDL = new JButton("Xóa");
        JButton btnClear = new JButton("Làm mới form");

        actionPanel.add(new JLabel("Từ khóa:"));
        actionPanel.add(txtSearchDL);
        actionPanel.add(btnSearch);
        actionPanel.add(Box.createHorizontalStrut(30));
        actionPanel.add(btnAddDL);
        actionPanel.add(btnEditDL);
        actionPanel.add(btnDeleteDL);
        actionPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);

        modelDL = new DefaultTableModel(new String[]{"ID VẬT LÝ", "MÃ ĐẠI LÝ", "TÊN ĐƠN VỊ", "ĐỊA CHỈ TRỤ SỞ", "SỐ ĐIỆN THOẠI"}, 0);
        tableDL = new JTable(modelDL);
        JScrollPane scroll = new JScrollPane(tableDL);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        tabbedPane.addTab("Danh mục Đại lý con", panel);

        btnSearch.addActionListener(e -> loadDataDaiLy(txtSearchDL.getText().trim()));

        btnAddDL.addActionListener(e -> {
            if (txtMaDL.getText().trim().isEmpty() || txtTenDL.getText().trim().isEmpty() || txtSdtDL.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng không để trống trường thông tin bắt buộc", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DaiLyCon dl = new DaiLyCon(0, txtMaDL.getText().trim(), txtTenDL.getText().trim(), txtDiaChiDL.getText().trim(), txtSdtDL.getText().trim());
            String res = controller.themDaiLy(dl);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                clearFormDL();
                loadDataDaiLy("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableDL.getSelectionModel().addListSelectionListener(e -> {
            int row = tableDL.getSelectedRow();
            if (row >= 0) {
                selectedIdDL = Integer.parseInt(modelDL.getValueAt(row, 0).toString());
                txtMaDL.setText(modelDL.getValueAt(row, 1).toString());
                txtMaDL.setEditable(false);
                txtTenDL.setText(modelDL.getValueAt(row, 2).toString());
                txtDiaChiDL.setText(modelDL.getValueAt(row, 3).toString());
                txtSdtDL.setText(modelDL.getValueAt(row, 4).toString());
            }
        });

        btnEditDL.addActionListener(e -> {
            if (selectedIdDL == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng cần chỉnh sửa từ danh sách!");
                return;
            }
            DaiLyCon dl = new DaiLyCon(selectedIdDL, txtMaDL.getText().trim(), txtTenDL.getText().trim(), txtDiaChiDL.getText().trim(), txtSdtDL.getText().trim());
            String res = controller.capNhatDaiLy(dl);
            if (res.equals("Thành công")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                clearFormDL();
                loadDataDaiLy("");
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteDL.addActionListener(e -> {
            if (selectedIdDL == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đại lý cần xóa!");
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa đại lý con này?", "Xác nhận hành động", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                String res = controller.xoaDaiLy(selectedIdDL);
                if (res.equals("Thành công")) {
                    JOptionPane.showMessageDialog(this, "Xóa đối tượng thành công");
                    clearFormDL();
                    loadDataDaiLy("");
                } else {
                    JOptionPane.showMessageDialog(this, res, "Cảnh báo nghiêm trọng", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnClear.addActionListener(e -> clearFormDL());
    }

    private void loadDataDaiLy(String tuKhoa) {
        modelDL.setRowCount(0);
        try {
            List<DaiLyCon> list = controller.timKiemDaiLy(tuKhoa);
            if (list.isEmpty() && !tuKhoa.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đại lý phù hợp", "Thông báo tra cứu", JOptionPane.INFORMATION_MESSAGE);
            }
            for (DaiLyCon dl : list) {
                modelDL.addRow(new Object[]{dl.getId(), dl.getMaDL(), dl.getTenDL(), dl.getDiaChi(), dl.getSoDT()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFormDL() {
        selectedIdDL = -1;
        txtMaDL.setText("");
        txtMaDL.setEditable(true);
        txtTenDL.setText("");
        txtDiaChiDL.setText("");
        txtSdtDL.setText("");
        tableDL.clearSelection();
    }
}

