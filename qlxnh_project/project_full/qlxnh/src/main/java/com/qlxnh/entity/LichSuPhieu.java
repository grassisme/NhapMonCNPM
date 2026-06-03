package com.qlxnh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng cho màn hình tra cứu lịch sử (gộp phiếu nhập + xuất)
 */
public class LichSuPhieu {

    private int id;
    private String maPhieu;
    private LocalDate ngay;
    private String loai; // "NHAP" hoặc "XUAT"
    private String nguoiLap;
    private int soDong;
    private BigDecimal tongTien;

    public LichSuPhieu() {}

    public LichSuPhieu(int id, String maPhieu, LocalDate ngay, String loai, String nguoiLap, int soDong, BigDecimal tongTien) {
        this.id = id;
        this.maPhieu = maPhieu;
        this.ngay = ngay;
        this.loai = loai;
        this.nguoiLap = nguoiLap;
        this.soDong = soDong;
        this.tongTien = tongTien;
    }

    public int getId() { 
        return id; 
    }
    public String getMaPhieu() { 
        return maPhieu; 
    }
    public LocalDate getNgay() { 
        return ngay; 
    }
    public String getLoai() { 
        return loai; 
    }
    public String getNguoiLap() { 
        return nguoiLap; 
    }
    public int getSoDong() { 
        return soDong; 
    }
    public BigDecimal getTongTien() { 
        return tongTien; 
    }

    public void setId(int id) { 
        this.id = id; 
    }
    public void setMaPhieu(String maPhieu) { 
        this.maPhieu = maPhieu; 
    }
    public void setNgay(LocalDate ngay) { 
        this.ngay = ngay; 
    }
    public void setLoai(String loai) { 
        this.loai = loai; 
    }
    public void setNguoiLap(String nguoiLap) { 
        this.nguoiLap = nguoiLap; 
    }
    public void setSoDong(int soDong) { 
        this.soDong = soDong; 
    }
    public void setTongTien(BigDecimal tongTien) { 
        this.tongTien = tongTien; 
    }
}
