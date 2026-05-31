package com.qlxnh.entity;

import java.math.BigDecimal;

/**
 * Lớp thực thể ánh xạ với bảng tblCTPhieuXuat (chi tiết phiếu xuất).
 * Cấu trúc tương tự CTPhieuNhap nhưng thay NhaCungCap bằng DaiLyCon.
 */
public class CTPhieuXuat {
    private int id;
    private int phieuXuatId;
    private HangHoa hangHoa;
    private DaiLyCon daiLyCon;
    private int soLuong;
    private BigDecimal donGia;

    public CTPhieuXuat() {
        this.donGia = BigDecimal.ZERO;
    }

    public CTPhieuXuat(HangHoa hangHoa, DaiLyCon daiLyCon, int soLuong, BigDecimal donGia) {
        this.hangHoa = hangHoa;
        this.daiLyCon = daiLyCon;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public int        getId()          { return id; }
    public int        getPhieuXuatId() { return phieuXuatId; }
    public HangHoa    getHangHoa()     { return hangHoa; }
    public DaiLyCon   getDaiLyCon()    { return daiLyCon; }
    public int        getSoLuong()     { return soLuong; }
    public BigDecimal getDonGia()      { return donGia; }

    public void setId(int id)                      { this.id = id; }
    public void setPhieuXuatId(int phieuXuatId)    { this.phieuXuatId = phieuXuatId; }
    public void setHangHoa(HangHoa hangHoa)        { this.hangHoa = hangHoa; }
    public void setDaiLyCon(DaiLyCon daiLyCon)     { this.daiLyCon = daiLyCon; }
    public void setSoLuong(int soLuong)            { this.soLuong = soLuong; }
    public void setDonGia(BigDecimal donGia)       { this.donGia = donGia; }

    public BigDecimal tinhThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}
