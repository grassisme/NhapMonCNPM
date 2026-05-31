package com.qlxnh.entity;

/**
 * Lớp thực thể ánh xạ với bảng tblHangHoa.
 * toString() trả về "mã - tên" để hiển thị trong JComboBox.
 */
public class HangHoa {
    private int id;
    private String maHang;
    private String tenHang;
    private String moTa;
    private int soLuongTon;

    public HangHoa() {}

    public HangHoa(int id, String maHang, String tenHang, String moTa, int soLuongTon) {
        this.id = id;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.moTa = moTa;
        this.soLuongTon = soLuongTon;
    }

    public int    getId()         { return id; }
    public String getMaHang()     { return maHang; }
    public String getTenHang()    { return tenHang; }
    public String getMoTa()       { return moTa; }
    public int    getSoLuongTon() { return soLuongTon; }

    public void setId(int id)                 { this.id = id; }
    public void setMaHang(String maHang)      { this.maHang = maHang; }
    public void setTenHang(String tenHang)    { this.tenHang = tenHang; }
    public void setMoTa(String moTa)          { this.moTa = moTa; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    @Override
    public String toString() {
        return maHang + " - " + tenHang;
    }
}
