package com.qlxnh;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlxnh.view.PhieuNhapForm;
import javax.swing.*;

/**
 * ================================================================
 *  PhieuNhapMain — Launcher RIÊNG cho module Lập phiếu nhập.
 * ================================================================
 *
 *  MỤC ĐÍCH:
 *    File này chỉ phục vụ developer phụ trách module Lập phiếu nhập
 *    có thể chạy thử module độc lập, KHÔNG cần đợi các module khác
 *    (đăng nhập, cửa sổ chính, menu...) hoàn thành.
 *
 *  CÁCH TÍCH HỢP VÀO HỆ THỐNG NHÓM:
 *    - Khi gộp module với nhóm, KHÔNG dùng file này.
 *    - Trưởng nhóm / người làm cửa sổ chính sẽ gọi PhieuNhapForm
 *      trực tiếp từ menu, ví dụ:
 *
 *          // Trong MainFrame.java của bạn trong nhóm:
 *          menuItemLapPhieuNhap.addActionListener(e -> {
 *              PhieuNhapForm form = new PhieuNhapForm(
 *                  Session.getCurrentUser().getId(),
 *                  Session.getCurrentUser().getHoTen()
 *              );
 *              form.setVisible(true);
 *          });
 *
 *    - Khi đó, file PhieuNhapMain.java này có thể XÓA hoặc giữ lại
 *      trong nhánh dev cá nhân để test riêng.
 *
 *  CONTRACT VỚI NHÓM (cam kết của module này):
 *    Input  : (int userId, String userName) — id và tên người đang đăng nhập
 *    Output : phiếu nhập được ghi vào tblPhieuNhap + tblCTPhieuNhap,
 *             tồn kho trong tblHangHoa được cộng tự động
 *    Phạm vi: KHÔNG đụng các bảng khác ngoài 3 bảng trên
 *    Class công khai: chỉ duy nhất com.qlxnh.view.PhieuNhapForm
 * ================================================================
 */
public class PhieuNhapMain {

    // ===== Mock user để dev độc lập =====
    // Khi tích hợp với module Đăng nhập thật, hai dòng này bỏ đi —
    // user thật sẽ lấy từ Session sau khi đăng nhập thành công.
    private static final int    MOCK_USER_ID   = 1;             // id của admin trong tblNguoiDung
    private static final String MOCK_USER_NAME = "Nguyễn Văn A"; // tên hiển thị

    public static void main(String[] args) {

        // ===== Cài đặt Look & Feel FlatLaf =====
        // Phải gọi TRƯỚC khi tạo bất kỳ component Swing nào.
        // Nếu fail (thiếu jar), app vẫn chạy được với L&F mặc định.
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Không cài được FlatLaf, dùng L&F mặc định: " + ex.getMessage());
        }

        // ===== Mở form trên Event Dispatch Thread =====
        // Mọi thao tác với Swing phải nằm trong EDT (quy tắc bắt buộc).
        // SwingUtilities.invokeLater() đảm bảo điều đó.
        SwingUtilities.invokeLater(() -> {
            PhieuNhapForm form = new PhieuNhapForm(MOCK_USER_ID, MOCK_USER_NAME);
            form.setVisible(true);
        });
    }
}
