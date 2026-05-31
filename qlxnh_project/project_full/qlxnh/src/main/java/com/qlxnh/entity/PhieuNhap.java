package com.qlxnh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp thực thể ánh xạ với bảng tblPhieuNhap.
 *
 * Chứa danh sách CTPhieuNhap (quan hệ thành phần — composition).
 * maPhieu là cột tính sẵn trong DB ("PN0001"...), DAO chỉ đọc, không set.
 * tongTien KHÔNG lưu DB, tính lúc cần qua tinhTongTien().
 */
public class PhieuNhap {
    private int id;
    private String maPhieu;        // computed column từ DB
    private LocalDate ngayNhap;
    private int nguoiDungId;
    private String nguoiDungHoTen; // chỉ để hiển thị, không lưu DB
    private List<CTPhieuNhap> chiTietList = new ArrayList<>();

    public PhieuNhap() {}

    public PhieuNhap(int id, LocalDate ngayNhap, int nguoiDungId) {
        this.id = id;
        this.ngayNhap = ngayNhap;
        this.nguoiDungId = nguoiDungId;
    }

    public int       getId()             { return id; }
    public String    getMaPhieu()        { return maPhieu; }
    public LocalDate getNgayNhap()       { return ngayNhap; }
    public int       getNguoiDungId()    { return nguoiDungId; }
    public String    getNguoiDungHoTen() { return nguoiDungHoTen; }
    public List<CTPhieuNhap> getChiTietList() { return chiTietList; }

    public void setId(int id)                            { this.id = id; }
    public void setMaPhieu(String maPhieu)               { this.maPhieu = maPhieu; }
    public void setNgayNhap(LocalDate ngayNhap)          { this.ngayNhap = ngayNhap; }
    public void setNguoiDungId(int nguoiDungId)          { this.nguoiDungId = nguoiDungId; }
    public void setNguoiDungHoTen(String nguoiDungHoTen) { this.nguoiDungHoTen = nguoiDungHoTen; }
    public void setChiTietList(List<CTPhieuNhap> ct)     { this.chiTietList = ct; }

    /** Tổng tiền phiếu = tổng thành tiền của các dòng chi tiết. */
    public BigDecimal tinhTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (CTPhieuNhap ct : chiTietList) {
            tong = tong.add(ct.tinhThanhTien());
        }
        return tong;
    }
}
