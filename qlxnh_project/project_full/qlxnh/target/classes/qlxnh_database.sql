-- ============================================================
-- qlxnh_database.sql
-- Tạo toàn bộ CSDL cho hệ thống Quản lý Xuất Nhập Hàng:
--   1. Database + 9 bảng + ràng buộc + index
--   2. Các VIEW & FUNCTION phục vụ UC-05 (tra cứu) và UC-06 (báo cáo tồn kho)
--   3. Dữ liệu mẫu để test
-- Chạy lại file này sẽ xóa sạch database cũ và dựng lại từ đầu.
-- ============================================================

USE master;
GO

IF EXISTS (SELECT * FROM sys.databases WHERE name = 'QLXNH')
BEGIN
    ALTER DATABASE QLXNH SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLXNH;
END;
GO

CREATE DATABASE QLXNH;
GO

USE QLXNH;
GO

-- ============================================================
-- PHẦN 1: BẢNG
-- ============================================================

-- Hàng hóa: soLuongTon tự cập nhật khi có phiếu nhập/xuất, không cho âm
CREATE TABLE tblHangHoa (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    maHang      NVARCHAR(20)  NOT NULL UNIQUE,
    tenHang     NVARCHAR(200) NOT NULL,
    moTa        NVARCHAR(500) NULL,
    soLuongTon  INT NOT NULL DEFAULT 0 CHECK (soLuongTon >= 0)
);

CREATE TABLE tblNhaCungCap (
    id      INT IDENTITY(1,1) PRIMARY KEY,
    maNCC   NVARCHAR(20)  NOT NULL UNIQUE,
    tenNCC  NVARCHAR(200) NOT NULL,
    diaChi  NVARCHAR(300) NULL,
    soDT    NVARCHAR(15)  NULL
);

CREATE TABLE tblDaiLyCon (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    maDL   NVARCHAR(20)  NOT NULL UNIQUE,
    tenDL  NVARCHAR(200) NOT NULL,
    diaChi NVARCHAR(300) NULL,
    soDT   NVARCHAR(15)  NULL
);

-- 3 vai trò cố định: Quản lý kho / Nhân viên nhập liệu / Người xem
CREATE TABLE tblVaiTro (
    id        INT IDENTITY(1,1) PRIMARY KEY,
    tenVaiTro NVARCHAR(50) NOT NULL UNIQUE
);

-- kichHoat: cho phép Quản lý kho khóa tài khoản mà không xóa
CREATE TABLE tblNguoiDung (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    hoTen       NVARCHAR(150) NOT NULL,
    tenDangNhap NVARCHAR(50)  NOT NULL UNIQUE,
    matKhau     NVARCHAR(255) NOT NULL,
    kichHoat    BIT NOT NULL DEFAULT 1,
    vaiTroId    INT NOT NULL,
    CONSTRAINT FK_NguoiDung_VaiTro FOREIGN KEY (vaiTroId) REFERENCES tblVaiTro(id)
);

-- maPhieu là cột tự sinh: id=1 -> "PN0001", id=42 -> "PN0042"
CREATE TABLE tblPhieuNhap (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    maPhieu     AS ('PN' + RIGHT('0000' + CAST(id AS VARCHAR(10)), 4)) PERSISTED,
    ngayNhap    DATE NOT NULL,
    nguoiDungId INT NOT NULL,
    CONSTRAINT FK_PhieuNhap_NguoiDung FOREIGN KEY (nguoiDungId) REFERENCES tblNguoiDung(id)
);

-- ON DELETE CASCADE: xóa phiếu thì xóa luôn chi tiết
CREATE TABLE tblCTPhieuNhap (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    phieuNhapId  INT NOT NULL,
    hangHoaId    INT NOT NULL,
    nhaCungCapId INT NOT NULL,
    soLuong      INT NOT NULL CHECK (soLuong > 0),
    donGia       DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    CONSTRAINT FK_CTPN_PN  FOREIGN KEY (phieuNhapId)  REFERENCES tblPhieuNhap(id) ON DELETE CASCADE,
    CONSTRAINT FK_CTPN_HH  FOREIGN KEY (hangHoaId)    REFERENCES tblHangHoa(id),
    CONSTRAINT FK_CTPN_NCC FOREIGN KEY (nhaCungCapId) REFERENCES tblNhaCungCap(id)
);

CREATE TABLE tblPhieuXuat (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    maPhieu     AS ('PX' + RIGHT('0000' + CAST(id AS VARCHAR(10)), 4)) PERSISTED,
    ngayXuat    DATE NOT NULL,
    nguoiDungId INT NOT NULL,
    CONSTRAINT FK_PhieuXuat_NguoiDung FOREIGN KEY (nguoiDungId) REFERENCES tblNguoiDung(id)
);

CREATE TABLE tblCTPhieuXuat (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    phieuXuatId INT NOT NULL,
    hangHoaId   INT NOT NULL,
    daiLyConId  INT NOT NULL,
    soLuong     INT NOT NULL CHECK (soLuong > 0),
    donGia      DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    CONSTRAINT FK_CTPX_PX FOREIGN KEY (phieuXuatId) REFERENCES tblPhieuXuat(id) ON DELETE CASCADE,
    CONSTRAINT FK_CTPX_HH FOREIGN KEY (hangHoaId)   REFERENCES tblHangHoa(id),
    CONSTRAINT FK_CTPX_DL FOREIGN KEY (daiLyConId)  REFERENCES tblDaiLyCon(id)
);

-- Index tăng tốc UC-05 (lọc khoảng ngày) và UC-06 (join chi tiết theo hàng hóa)
CREATE INDEX IX_PhieuNhap_ngayNhap ON tblPhieuNhap(ngayNhap);
CREATE INDEX IX_PhieuXuat_ngayXuat ON tblPhieuXuat(ngayXuat);
CREATE INDEX IX_CTPN_phieuNhapId  ON tblCTPhieuNhap(phieuNhapId);
CREATE INDEX IX_CTPN_hangHoaId    ON tblCTPhieuNhap(hangHoaId);
CREATE INDEX IX_CTPX_phieuXuatId  ON tblCTPhieuXuat(phieuXuatId);
CREATE INDEX IX_CTPX_hangHoaId    ON tblCTPhieuXuat(hangHoaId);
GO

-- ============================================================
-- PHẦN 2: VIEW & FUNCTION
-- ============================================================

-- UC-05: danh sách phiếu nhập kèm số dòng + tổng tiền + tên người lập
CREATE VIEW v_PhieuNhap_TomTat AS
SELECT
    pn.id,
    pn.maPhieu,
    pn.ngayNhap,
    pn.nguoiDungId,
    nd.hoTen           AS nguoiLapHoTen,
    COUNT(ct.id)       AS soDong,
    ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongTien
FROM tblPhieuNhap pn
LEFT JOIN tblNguoiDung    nd ON nd.id = pn.nguoiDungId
LEFT JOIN tblCTPhieuNhap  ct ON ct.phieuNhapId = pn.id
GROUP BY pn.id, pn.maPhieu, pn.ngayNhap, pn.nguoiDungId, nd.hoTen;
GO

CREATE VIEW v_PhieuXuat_TomTat AS
SELECT
    px.id,
    px.maPhieu,
    px.ngayXuat,
    px.nguoiDungId,
    nd.hoTen           AS nguoiLapHoTen,
    COUNT(ct.id)       AS soDong,
    ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongTien
FROM tblPhieuXuat px
LEFT JOIN tblNguoiDung   nd ON nd.id = px.nguoiDungId
LEFT JOIN tblCTPhieuXuat ct ON ct.phieuXuatId = px.id
GROUP BY px.id, px.maPhieu, px.ngayXuat, px.nguoiDungId, nd.hoTen;
GO

-- UC-05: chi tiết phiếu kèm tên hàng + tên NCC/đại lý + thành tiền
CREATE VIEW v_CTPhieuNhap_FullInfo AS
SELECT
    ct.id, ct.phieuNhapId,
    ct.hangHoaId, hh.maHang, hh.tenHang,
    ct.nhaCungCapId, ncc.maNCC, ncc.tenNCC,
    ct.soLuong, ct.donGia,
    (ct.soLuong * ct.donGia) AS thanhTien
FROM tblCTPhieuNhap ct
JOIN tblHangHoa     hh  ON hh.id  = ct.hangHoaId
JOIN tblNhaCungCap  ncc ON ncc.id = ct.nhaCungCapId;
GO

CREATE VIEW v_CTPhieuXuat_FullInfo AS
SELECT
    ct.id, ct.phieuXuatId,
    ct.hangHoaId, hh.maHang, hh.tenHang,
    ct.daiLyConId, dl.maDL, dl.tenDL,
    ct.soLuong, ct.donGia,
    (ct.soLuong * ct.donGia) AS thanhTien
FROM tblCTPhieuXuat ct
JOIN tblHangHoa  hh ON hh.id = ct.hangHoaId
JOIN tblDaiLyCon dl ON dl.id = ct.daiLyConId;
GO

-- UC-06 chế độ 1: tồn kho hiện tại
CREATE VIEW v_TonKhoHienTai AS
SELECT id AS hangHoaId, maHang, tenHang, moTa, soLuongTon
FROM tblHangHoa;
GO

-- UC-06 chế độ 2: tồn kho tại một ngày trong quá khứ
-- Công thức: Tồn(ngày) = Tồn hiện tại - Tổng nhập sau ngày + Tổng xuất sau ngày
CREATE FUNCTION dbo.fn_TonKhoTaiNgay(@ngay DATE)
RETURNS TABLE AS RETURN
(
    SELECT
        hh.id AS hangHoaId,
        hh.maHang, hh.tenHang, hh.moTa,
        hh.soLuongTon AS tonHienTai,
        hh.soLuongTon
            - ISNULL((SELECT SUM(ct.soLuong) FROM tblCTPhieuNhap ct
                      JOIN tblPhieuNhap pn ON pn.id = ct.phieuNhapId
                      WHERE ct.hangHoaId = hh.id AND pn.ngayNhap > @ngay), 0)
            + ISNULL((SELECT SUM(ct.soLuong) FROM tblCTPhieuXuat ct
                      JOIN tblPhieuXuat px ON px.id = ct.phieuXuatId
                      WHERE ct.hangHoaId = hh.id AND px.ngayXuat > @ngay), 0)
        AS tonTaiNgay
    FROM tblHangHoa hh
);
GO

-- ============================================================
-- PHẦN 3: DỮ LIỆU MẪU
-- Mật khẩu mặc định cho 3 tài khoản: 123456
-- ============================================================

INSERT INTO tblVaiTro (tenVaiTro) VALUES
    (N'Quản lý kho'),
    (N'Nhân viên nhập liệu'),
    (N'Người xem');

INSERT INTO tblNguoiDung (hoTen, tenDangNhap, matKhau, vaiTroId, kichHoat) VALUES
    (N'Nguyễn Văn A', 'admin',    '123456', 1, 1),
    (N'Trần Thị B',   'nhanvien', '123456', 2, 1),
    (N'Lê Văn C',     'guest',    '123456', 3, 1);

INSERT INTO tblHangHoa (maHang, tenHang, moTa, soLuongTon) VALUES
    ('HH001', N'Mì gói A',   N'Gói 75g',    100),
    ('HH002', N'Nước suối',  N'Chai 500ml',  50),
    ('HH003', N'Bánh quy B', N'Hộp 200g',    12),
    ('HH004', N'Sữa hộp',    N'Hộp 180ml',    8),
    ('HH005', N'Cà phê gói', N'Túi 20 gói', 230),
    ('HH006', N'Dầu ăn 1L',  N'Chai nhựa',   85);

INSERT INTO tblNhaCungCap (maNCC, tenNCC, diaChi, soDT) VALUES
    ('NCC01', N'Công ty Thực phẩm X', N'123 Lê Lợi, Q.1, TP.HCM', '0901111111'),
    ('NCC02', N'Công ty Đồ uống Y',   N'45 Cầu Giấy, Hà Nội',     '0902222222'),
    ('NCC03', N'Công ty Sữa Z',       N'78 Nguyễn Trãi, TP.HCM',  '0903333333');

INSERT INTO tblDaiLyCon (maDL, tenDL, diaChi, soDT) VALUES
    ('DL01', N'Đại lý A', N'12 Hai Bà Trưng, Hà Nội', '0911111111'),
    ('DL02', N'Đại lý B', N'34 Trần Phú, Đà Nẵng',    '0922222222'),
    ('DL03', N'Đại lý C', N'56 Lý Thường Kiệt, Huế',  '0933333333');

-- Hai phiếu nhập mẫu
INSERT INTO tblPhieuNhap (ngayNhap, nguoiDungId) VALUES ('2026-04-15', 1);
DECLARE @pn1 INT = SCOPE_IDENTITY();
INSERT INTO tblCTPhieuNhap (phieuNhapId, hangHoaId, nhaCungCapId, soLuong, donGia) VALUES
    (@pn1, 1, 1, 100, 5000),
    (@pn1, 2, 2, 200, 4000);

INSERT INTO tblPhieuNhap (ngayNhap, nguoiDungId) VALUES ('2026-05-02', 2);
DECLARE @pn2 INT = SCOPE_IDENTITY();
INSERT INTO tblCTPhieuNhap (phieuNhapId, hangHoaId, nhaCungCapId, soLuong, donGia) VALUES
    (@pn2, 4, 3, 50, 12000);

-- Hai phiếu xuất mẫu
INSERT INTO tblPhieuXuat (ngayXuat, nguoiDungId) VALUES ('2026-05-22', 1);
DECLARE @px1 INT = SCOPE_IDENTITY();
INSERT INTO tblCTPhieuXuat (phieuXuatId, hangHoaId, daiLyConId, soLuong, donGia) VALUES
    (@px1, 1, 2, 40, 6000);

INSERT INTO tblPhieuXuat (ngayXuat, nguoiDungId) VALUES ('2026-05-28', 2);
DECLARE @px2 INT = SCOPE_IDENTITY();
INSERT INTO tblCTPhieuXuat (phieuXuatId, hangHoaId, daiLyConId, soLuong, donGia) VALUES
    (@px2, 5, 1, 20, 8000),
    (@px2, 6, 1, 15, 35000);
GO

PRINT N'Tạo CSDL QLXNH thành công.';
GO
