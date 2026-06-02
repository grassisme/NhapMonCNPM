package com.qlxnh.controller;

import com.qlxnh.dao.BaoCaoDAO;
import com.qlxnh.entity.TonKhoReportItem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller cho module Báo cáo tồn kho tại một thời điểm (UC-06).
 */
public class BaoCaoController {

    private final BaoCaoDAO baoCaoDAO = new BaoCaoDAO();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Lấy danh sách báo cáo tồn kho.
     *
     * @param isHienTai true nếu xem tồn hiện tại, false nếu xem tồn tại một ngày tham chiếu
     * @param ngayStr chuỗi ngày định dạng dd/MM/yyyy (chỉ bắt buộc khi isHienTai = false)
     * @return danh sách các dòng dữ liệu báo cáo
     */
    public List<TonKhoReportItem> getBaoCaoTonKho(boolean isHienTai, String ngayStr) throws SQLException, IllegalArgumentException {
        if (isHienTai) {
            return baoCaoDAO.layTonKhoHienTai();
        } else {
            if (ngayStr == null || ngayStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập ngày tham chiếu.");
            }
            try {
                LocalDate ngay = LocalDate.parse(ngayStr.trim(), DATE_FORMATTER);
                // Không cho phép chọn ngày tương lai để tính báo cáo quá khứ
                if (ngay.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("Ngày tham chiếu không được lớn hơn ngày hiện tại.");
                }
                return baoCaoDAO.layTonKhoTaiNgay(ngay);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Vui lòng nhập theo dạng dd/MM/yyyy (Ví dụ: 31/03/2026).");
            }
        }
    }

    /**
     * Xuất dữ liệu báo cáo ra file Excel (dưới định dạng CSV mã hóa UTF-8 có BOM).
     *
     * @param data danh sách dữ liệu báo cáo
     * @param file file cần ghi
     * @param isHienTai chế độ hiện tại hay so sánh
     * @param ngayStr chuỗi ngày tham chiếu
     */
    public void xuatExcel(List<TonKhoReportItem> data, File file, boolean isHienTai, String ngayStr) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // Ghi Byte Order Mark (BOM) để Microsoft Excel tự động mở bằng UTF-8
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            // Tiêu đề báo cáo
            writer.write("BÁO CÁO TỒN KHO HÀNG HÓA\n");
            if (isHienTai) {
                writer.write("Chế độ xem: Tồn kho hiện tại\n");
            } else {
                writer.write("Chế độ xem: Tồn kho tại ngày " + ngayStr + "\n");
            }
            writer.write("Ngày xuất báo cáo: " + LocalDate.now().format(DATE_FORMATTER) + "\n\n");

            // Header cột
            if (isHienTai) {
                writer.write("Mã hàng,Tên hàng,Mô tả,Tồn hiện tại\n");
            } else {
                writer.write("Mã hàng,Tên hàng,Mô tả,Tồn tại ngày " + ngayStr + ",Tồn hiện tại,Biến động\n");
            }

            // Dữ liệu các dòng
            for (TonKhoReportItem item : data) {
                String ma = escapeCSV(item.getMaHang());
                String ten = escapeCSV(item.getTenHang());
                String mota = escapeCSV(item.getMoTa());

                if (isHienTai) {
                    writer.write(String.format("%s,%s,%s,%d\n", ma, ten, mota, item.getTonHienTai()));
                } else {
                    int bd = item.getBienDong();
                    String bdStr = bd > 0 ? "+" + bd : String.valueOf(bd);
                    writer.write(String.format("%s,%s,%s,%d,%d,%s\n",
                        ma, ten, mota, item.getTonTaiNgay(), item.getTonHienTai(), bdStr));
                }
            }
            writer.flush();
        }
    }

    /** Tránh lỗi cú pháp CSV khi chuỗi chứa dấu phẩy hoặc ngoặc kép. */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
