package com.qlxnh.entity;

/**
 * Lớp thực thể ánh xạ với bảng tblVaiTro.
 * Chỉ có 3 bản ghi: Quản lý kho / Nhân viên nhập liệu / Người xem.
 */
public class VaiTro {
    private int id;
    private String tenVaiTro;

    /** Hằng số id cho các vai trò — dùng khi kiểm tra phân quyền trong code. */
    public static final int ID_QUAN_LY_KHO     = 1;
    public static final int ID_NHAN_VIEN       = 2;
    public static final int ID_NGUOI_XEM       = 3;

    public VaiTro() {}

    public VaiTro(int id, String tenVaiTro) {
        this.id = id;
        this.tenVaiTro = tenVaiTro;
    }

    public int    getId()        { return id; }
    public String getTenVaiTro() { return tenVaiTro; }

    public void setId(int id)                  { this.id = id; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }

    @Override
    public String toString() {
        return tenVaiTro;
    }
}
