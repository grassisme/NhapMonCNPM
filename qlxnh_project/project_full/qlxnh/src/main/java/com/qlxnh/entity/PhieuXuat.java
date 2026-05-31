package com.qlxnh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp thực thể ánh xạ với bảng tblPhieuXuat.
 * Cấu trúc tương tự PhieuNhap nhưng chứa danh sách CTPhieuXuat.
 */
public class PhieuXuat {
    private int id;
    private String maPhieu;        // computed column từ DB ("PX0001"...)
    private LocalDate ngayXuat;
    private int nguoiDungId;
    private String nguoiDungHoTen;
    private List<CTPhieuXuat> chiTietList = new ArrayList<>();

    public PhieuXuat() {}

    public PhieuXuat(int id, LocalDate ngayXuat, int nguoiDungId) {
        this.id = id;
        this.ngayXuat = ngayXuat;
        this.nguoiDungId = nguoiDungId;
    }

    public int       getId()             { return id; }
    public String    getMaPhieu()        { return maPhieu; }
    public LocalDate getNgayXuat()       { return ngayXuat; }
    public int       getNguoiDungId()    { return nguoiDungId; }
    public String    getNguoiDungHoTen() { return nguoiDungHoTen; }
    public List<CTPhieuXuat> getChiTietList() { return chiTietList; }

    public void setId(int id)                            { this.id = id; }
    public void setMaPhieu(String maPhieu)               { this.maPhieu = maPhieu; }
    public void setNgayXuat(LocalDate ngayXuat)          { this.ngayXuat = ngayXuat; }
    public void setNguoiDungId(int nguoiDungId)          { this.nguoiDungId = nguoiDungId; }
    public void setNguoiDungHoTen(String nguoiDungHoTen) { this.nguoiDungHoTen = nguoiDungHoTen; }
    public void setChiTietList(List<CTPhieuXuat> ct)     { this.chiTietList = ct; }

    public BigDecimal tinhTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (CTPhieuXuat ct : chiTietList) {
            tong = tong.add(ct.tinhThanhTien());
        }
        return tong;
    }
}
