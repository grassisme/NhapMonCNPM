package com.qlxnh.entity;

/**
 * Lớp thực thể ánh xạ với bảng tblNguoiDung.
 * Quan hệ: mỗi người dùng có 1 vai trò (vaiTroId tham chiếu tblVaiTro).
 * Trường vaiTro (object) được gán sau khi DAO load — tiện hiển thị tên vai trò.
 */
public class NguoiDung {
    private int id;
    private String hoTen;
    private String tenDangNhap;
    private String matKhau;
    private boolean kichHoat;
    private int vaiTroId;
    private VaiTro vaiTro; // chỉ dùng khi cần hiển thị tên vai trò, không bắt buộc load

    public NguoiDung() {}

    public NguoiDung(int id, String hoTen, String tenDangNhap, String matKhau,
                     boolean kichHoat, int vaiTroId) {
        this.id = id;
        this.hoTen = hoTen;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.kichHoat = kichHoat;
        this.vaiTroId = vaiTroId;
    }

    public int     getId()          { return id; }
    public String  getHoTen()       { return hoTen; }
    public String  getTenDangNhap() { return tenDangNhap; }
    public String  getMatKhau()     { return matKhau; }
    public boolean isKichHoat()     { return kichHoat; }
    public int     getVaiTroId()    { return vaiTroId; }
    public VaiTro  getVaiTro()      { return vaiTro; }

    public void setId(int id)                       { this.id = id; }
    public void setHoTen(String hoTen)              { this.hoTen = hoTen; }
    public void setTenDangNhap(String tenDangNhap)  { this.tenDangNhap = tenDangNhap; }
    public void setMatKhau(String matKhau)          { this.matKhau = matKhau; }
    public void setKichHoat(boolean kichHoat)       { this.kichHoat = kichHoat; }
    public void setVaiTroId(int vaiTroId)           { this.vaiTroId = vaiTroId; }
    public void setVaiTro(VaiTro vaiTro)            { this.vaiTro = vaiTro; }

    @Override
    public String toString() {
        return tenDangNhap + " (" + hoTen + ")";
    }
}
