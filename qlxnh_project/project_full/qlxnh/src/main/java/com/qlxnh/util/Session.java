package com.qlxnh.util;

import com.qlxnh.entity.NguoiDung;
import com.qlxnh.entity.VaiTro;

/**
 * Lưu thông tin người dùng đang đăng nhập trong phiên làm việc.
 *
 * Cách dùng sau khi đăng nhập thành công:
 *   Session.setCurrentUser(nguoiDung);
 *
 * Cách kiểm tra quyền trong bất kỳ Form/Controller nào:
 *   if (Session.isQuanLyKho()) { ... }
 *   int userId = Session.getCurrentUser().getId();
 *
 * Cách đăng xuất:
 *   Session.clear();
 */
public final class Session {

    private static NguoiDung currentUser;

    private Session() {}

    public static void setCurrentUser(NguoiDung user) {
        currentUser = user;
    }

    public static NguoiDung getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Quản lý kho: toàn quyền. */
    public static boolean isQuanLyKho() {
        return isLoggedIn() && currentUser.getVaiTroId() == VaiTro.ID_QUAN_LY_KHO;
    }

    /** Nhân viên nhập liệu: lập phiếu nhập, xuất, xem danh mục. */
    public static boolean isNhanVien() {
        return isLoggedIn() && currentUser.getVaiTroId() == VaiTro.ID_NHAN_VIEN;
    }

    /** Người xem: chỉ tra cứu và báo cáo. */
    public static boolean isNguoiXem() {
        return isLoggedIn() && currentUser.getVaiTroId() == VaiTro.ID_NGUOI_XEM;
    }

    /** Xóa session khi đăng xuất. */
    public static void clear() {
        currentUser = null;
    }
}
