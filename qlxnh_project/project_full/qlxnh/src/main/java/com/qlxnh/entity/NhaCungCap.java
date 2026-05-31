package com.qlxnh.entity;

/**
 * Lớp thực thể ánh xạ với bảng tblNhaCungCap.
 */
public class NhaCungCap {
    private int id;
    private String maNCC;
    private String tenNCC;
    private String diaChi;
    private String soDT;

    public NhaCungCap() {}

    public NhaCungCap(int id, String maNCC, String tenNCC, String diaChi, String soDT) {
        this.id = id;
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.diaChi = diaChi;
        this.soDT = soDT;
    }

    public int    getId()     { return id; }
    public String getMaNCC()  { return maNCC; }
    public String getTenNCC() { return tenNCC; }
    public String getDiaChi() { return diaChi; }
    public String getSoDT()   { return soDT; }

    public void setId(int id)            { this.id = id; }
    public void setMaNCC(String maNCC)   { this.maNCC = maNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setSoDT(String soDT)     { this.soDT = soDT; }

    @Override
    public String toString() {
        return maNCC + " - " + tenNCC;
    }
}
