package com.qlxnh.controller;
import com.qlxnh.dao.HangHoaDAO;
import com.qlxnh.dao.NhaCungCapDAO;
import com.qlxnh.dao.DaiLyConDAO;
import com.qlxnh.entity.HangHoa;
import com.qlxnh.entity.NhaCungCap;
import com.qlxnh.entity.DaiLyCon;
import java.sql.SQLException;
import java.util.List;

public class DanhMucController {
    private HangHoaDAO hangHoaDAO;
    private NhaCungCapDAO nhaCungCapDAO;
    private DaiLyConDAO daiLyConDAO;

    public DanhMucController() {
        hangHoaDAO = new HangHoaDAO();
        nhaCungCapDAO = new NhaCungCapDAO();
        daiLyConDAO = new DaiLyConDAO();
    }

    // ================= KHU VỰC ĐIỀU KHIỂN HÀNG HÓA =================
    public List<HangHoa> timKiemHangHoa(String tuKhoa) throws SQLException {
        return hangHoaDAO.search(tuKhoa);
    }

    public String themHangHoa(HangHoa hh) {
        try {
            if (hangHoaDAO.getByMa(hh.getMaHang()) !=null) {
                return "Mã đối tượng đã tồn tại, vui lòng kiểm tra lại"; //
            }
            hangHoaDAO.insert(hh);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String capNhatHangHoa(HangHoa hh) {
        try {
            hangHoaDAO.update(hh);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String xoaHangHoa(int id) {
        try {
            hangHoaDAO.delete(id);
            return "Thành công";
        } catch (IllegalStateException e) {
            return "Từ chối xóa. Đối tượng hiện đang có lịch sử giao dịch trong hệ thống!";
        } catch (SQLException e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    // ================= KHU VỰC ĐIỀU KHIỂN NHÀ CUNG CẤP =================
    public List<NhaCungCap> timKiemNCC(String tuKhoa) throws SQLException {
        return nhaCungCapDAO.search(tuKhoa);
    }

    public String themNCC(NhaCungCap ncc) {
        try {
            if (nhaCungCapDAO.getByMa(ncc.getMaNCC())!= null) {
                return "Mã đối tượng đã tồn tại, vui lòng kiểm tra lại";
            }
            nhaCungCapDAO.insert(ncc);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String capNhatNCC(NhaCungCap ncc) {
        try {
            nhaCungCapDAO.update(ncc);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String xoaNCC(int id) {
        try {
            nhaCungCapDAO.delete(id);
            return "Thành công";
        } catch (IllegalStateException e) {
            return "Từ chối xóa. Đối tượng hiện đang có lịch sử giao dịch trong hệ thống!";
        } catch (SQLException e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }


    // ================= KHU VỰC ĐIỀU KHIỂN ĐẠI LÝ CON =================
    public List<DaiLyCon> timKiemDaiLy(String tuKhoa) throws SQLException {
        return daiLyConDAO.search(tuKhoa);
    }

    public String themDaiLy(DaiLyCon dl) {
        try {
            if (daiLyConDAO.getByMa(dl.getMaDL())!= null) {
                return "Mã đối tượng đã tồn tại, vui lòng kiểm tra lại";
            }
            daiLyConDAO.insert(dl);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String capNhatDaiLy(DaiLyCon dl) {
        try {
            daiLyConDAO.update(dl);
            return "Thành công";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String xoaDaiLy(int id) {
        try {
            daiLyConDAO.delete(id);
            return "Thành công";
        } catch (IllegalStateException e) {
            return "Từ chối xóa. Đối tượng hiện đang có lịch sử giao dịch trong hệ thống!";
        } catch (SQLException e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
}
