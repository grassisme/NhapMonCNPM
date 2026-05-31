package com.qlxnh.entity;

import java.math.BigDecimal;

/**
 * Lớp thực thể ánh xạ với bảng tblCTPhieuNhap (chi tiết phiếu nhập).
 *
 * Tham chiếu HangHoa và NhaCungCap dưới dạng object (không chỉ id) để
 * tiện hiển thị tên trong JTable. Khi lưu xuống DB, DAO chỉ lấy id.
 *
 * donGia dùng BigDecimal thay vì double — chính xác cho tiền tệ.
 * thanhTien KHÔNG lưu DB (tính được từ soLuong × donGia).
 */
public class CTPhieuNhap {
    private int id;
    private int phieuNhapId;
    private HangHoa hangHoa;
    private NhaCungCap nhaCungCap;
    private int soLuong;
    private BigDecimal donGia;

    public CTPhieuNhap() {
        this.donGia = BigDecimal.ZERO;
    }

    public CTPhieuNhap(HangHoa hangHoa, NhaCungCap nhaCungCap, int soLuong, BigDecimal donGia) {
        this.hangHoa = hangHoa;
        this.nhaCungCap = nhaCungCap;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public int        getId()           { return id; }
    public int        getPhieuNhapId()  { return phieuNhapId; }
    public HangHoa    getHangHoa()      { return hangHoa; }
    public NhaCungCap getNhaCungCap()   { return nhaCungCap; }
    public int        getSoLuong()      { return soLuong; }
    public BigDecimal getDonGia()       { return donGia; }

    public void setId(int id)                        { this.id = id; }
    public void setPhieuNhapId(int phieuNhapId)      { this.phieuNhapId = phieuNhapId; }
    public void setHangHoa(HangHoa hangHoa)          { this.hangHoa = hangHoa; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public void setSoLuong(int soLuong)              { this.soLuong = soLuong; }
    public void setDonGia(BigDecimal donGia)         { this.donGia = donGia; }

    /** Thành tiền = số lượng × đơn giá. */
    public BigDecimal tinhThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}
