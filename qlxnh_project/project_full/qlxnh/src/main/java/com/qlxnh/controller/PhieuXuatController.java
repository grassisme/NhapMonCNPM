package com.qlxnh.controller;

import com.qlxnh.dao.DaiLyConDAO;
import com.qlxnh.dao.HangHoaDAO;
import com.qlxnh.dao.PhieuXuatDAO;
import com.qlxnh.entity.CTPhieuXuat;
import com.qlxnh.entity.DaiLyCon;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.PhieuXuat;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller cho module Lập phiếu xuất (UC-03).
 *
 * Trung gian giữa View (PhieuXuatFrm, ChiTietXuatFrm) và DAO:
 *   - layDanhSachHangHoa() / layDanhSachDaiLy(): đổ combobox
 *   - kiemTraTon(maHang, soLuong): View gọi khi người dùng nhập số lượng
 *   - layTonKho(hangHoaId): View hiển thị "Tồn hiện tại"
 *   - luuPhieu(phieu): validate + gọi DAO lưu trong transaction
 */
public class PhieuXuatController {

    private final HangHoaDAO hangHoaDAO = new HangHoaDAO();
    private final DaiLyConDAO daiLyConDAO = new DaiLyConDAO();
    private final PhieuXuatDAO phieuXuatDAO = new PhieuXuatDAO();

    public List<HangHoa> layDanhSachHangHoa() throws SQLException {
        return hangHoaDAO.getAll();
    }

    public List<DaiLyCon> layDanhSachDaiLy() throws SQLException {
        return daiLyConDAO.getAll();
    }

    /** Lấy tồn kho hiện tại để hiển thị trên ChiTietXuatFrm. */
    public int layTonKho(int hangHoaId) throws SQLException {
        return hangHoaDAO.getTonKho(hangHoaId);
    }

    /** Kiểm tra tồn — gọi từ ChiTietXuatFrm trước khi cho thêm dòng. */
    public boolean kiemTraTon(String maHang, int soLuong) throws SQLException {
        return phieuXuatDAO.kiemTraTon(maHang, soLuong);
    }

    /**
     * Lưu phiếu xuất.
     * @return id phiếu vừa lưu
     * @throws IllegalArgumentException nếu dữ liệu form không hợp lệ
     * @throws IllegalStateException    nếu tồn kho không đủ (do DAO ném)
     * @throws SQLException             lỗi CSDL
     */
    public int luuPhieu(PhieuXuat phieu) throws SQLException {
        if (phieu.getNgayXuat() == null) {
            throw new IllegalArgumentException("Phai chon ngay xuat.");
        }
        if (phieu.getChiTietList() == null || phieu.getChiTietList().isEmpty()) {
            throw new IllegalArgumentException("Phieu xuat phai co it nhat 1 dong chi tiet.");
        }
        for (int i = 0; i < phieu.getChiTietList().size(); i++) {
            CTPhieuXuat ct = phieu.getChiTietList().get(i);
            int dong = i + 1;
            if (ct.getHangHoa() == null) {
                throw new IllegalArgumentException("Dong " + dong + ": chua chon hang hoa.");
            }
            if (ct.getDaiLyCon() == null) {
                throw new IllegalArgumentException("Dong " + dong + ": chua chon dai ly con.");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Dong " + dong + ": so luong phai > 0.");
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Dong " + dong + ": don gia phai >= 0.");
            }
        }
        return phieuXuatDAO.luuPhieu(phieu);
    }
}
