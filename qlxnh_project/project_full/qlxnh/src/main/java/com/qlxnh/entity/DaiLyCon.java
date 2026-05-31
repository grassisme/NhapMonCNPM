package com.qlxnh.entity;

/**
 * Lớp thực thể ánh xạ với bảng tblDaiLyCon.
 */
public class DaiLyCon {
    private int id;
    private String maDL;
    private String tenDL;
    private String diaChi;
    private String soDT;

    public DaiLyCon() {}

    public DaiLyCon(int id, String maDL, String tenDL, String diaChi, String soDT) {
        this.id = id;
        this.maDL = maDL;
        this.tenDL = tenDL;
        this.diaChi = diaChi;
        this.soDT = soDT;
    }

    public int    getId()     { return id; }
    public String getMaDL()   { return maDL; }
    public String getTenDL()  { return tenDL; }
    public String getDiaChi() { return diaChi; }
    public String getSoDT()   { return soDT; }

    public void setId(int id)            { this.id = id; }
    public void setMaDL(String maDL)     { this.maDL = maDL; }
    public void setTenDL(String tenDL)   { this.tenDL = tenDL; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setSoDT(String soDT)     { this.soDT = soDT; }

    @Override
    public String toString() {
        return maDL + " - " + tenDL;
    }
}
