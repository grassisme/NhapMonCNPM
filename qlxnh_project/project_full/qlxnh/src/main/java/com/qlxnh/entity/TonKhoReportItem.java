package com.qlxnh.entity;

/**
 * Lớp DTO/Entity đại diện cho một dòng dữ liệu trong Báo cáo tồn kho.
 *
 * Chứa thông tin về hàng hóa, số lượng tồn kho hiện tại,
 * số lượng tồn tại ngày tham chiếu, và biến động số lượng giữa hai thời điểm.
 */
public class TonKhoReportItem {
    private int hangHoaId;
    private String maHang;
    private String tenHang;
    private String moTa;
    private int tonHienTai;
    private int tonTaiNgay; // Bằng -1 nếu xem chế độ tồn hiện tại (không so sánh)

    public TonKhoReportItem() {}

    public TonKhoReportItem(int hangHoaId, String maHang, String tenHang, String moTa, int tonHienTai, int tonTaiNgay) {
        this.hangHoaId = hangHoaId;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.moTa = moTa;
        this.tonHienTai = tonHienTai;
        this.tonTaiNgay = tonTaiNgay;
    }

    public int getHangHoaId() {
        return hangHoaId;
    }

    public void setHangHoaId(int hangHoaId) {
        this.hangHoaId = hangHoaId;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getTonHienTai() {
        return tonHienTai;
    }

    public void setTonHienTai(int tonHienTai) {
        this.tonHienTai = tonHienTai;
    }

    public int getTonTaiNgay() {
        return tonTaiNgay;
    }

    public void setTonTaiNgay(int tonTaiNgay) {
        this.tonTaiNgay = tonTaiNgay;
    }

    /**
     * Tính biến động số lượng tồn kho: Tồn hiện tại - Tồn tại ngày tham chiếu.
     * Trả về 0 nếu không ở chế độ so sánh ngày (tonTaiNgay == -1).
     */
    public int getBienDong() {
        if (tonTaiNgay < 0) {
            return 0;
        }
        return tonHienTai - tonTaiNgay;
    }
}
