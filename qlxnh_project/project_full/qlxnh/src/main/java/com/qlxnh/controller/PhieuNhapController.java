package com.qlxnh.controller;

import com.qlxnh.dao.HangHoaDAO;
import com.qlxnh.dao.NhaCungCapDAO;
import com.qlxnh.dao.PhieuNhapDAO;
import com.qlxnh.entity.CTPhieuNhap;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.NhaCungCap;
import com.qlxnh.entity.PhieuNhap;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller cho module Lập phiếu nhập.
 *
 * Đảm nhiệm việc trung gian giữa View và DAO:
 *   - layDanhSachHangHoa() / layDanhSachNCC(): View gọi để đổ combobox
 *   - tinhThanhTien() / tinhTongTien(): tính toán cho View
 *   - luuPhieu(): kiểm tra hợp lệ rồi gọi DAO lưu xuống DB
 *
 * View KHÔNG gọi trực tiếp DAO. Mọi yêu cầu nghiệp vụ đi qua Controller.
 */
public class PhieuNhapController {

    private final HangHoaDAO hangHoaDAO = new HangHoaDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();

    public List<HangHoa> layDanhSachHangHoa() throws SQLException {
        return hangHoaDAO.getAll();
    }

    public List<NhaCungCap> layDanhSachNCC() throws SQLException {
        return nhaCungCapDAO.getAll();
    }

    /**
     * Lưu một phiếu nhập.
     * @return id của phiếu vừa lưu
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws SQLException nếu có lỗi CSDL
     */
    public int luuPhieu(PhieuNhap phieu) throws SQLException {
        // ===== Kiểm tra hợp lệ tối thiểu =====
        if (phieu.getNgayNhap() == null) {
            throw new IllegalArgumentException("Phai chon ngay nhap.");
        }
        if (phieu.getChiTietList() == null || phieu.getChiTietList().isEmpty()) {
            throw new IllegalArgumentException("Phieu nhap phai co it nhat 1 dong chi tiet.");
        }
        for (int i = 0; i < phieu.getChiTietList().size(); i++) {
            CTPhieuNhap ct = phieu.getChiTietList().get(i);
            int dong = i + 1;
            if (ct.getHangHoa() == null) {
                throw new IllegalArgumentException("Dong " + dong + ": chua chon hang hoa.");
            }
            if (ct.getNhaCungCap() == null) {
                throw new IllegalArgumentException("Dong " + dong + ": chua chon nha cung cap.");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Dong " + dong + ": so luong phai > 0.");
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Dong " + dong + ": don gia phai >= 0.");
            }
        }

        // ===== Đẩy xuống DAO (đã có transaction trong DAO) =====
        return phieuNhapDAO.luuPhieu(phieu);
    }
}
