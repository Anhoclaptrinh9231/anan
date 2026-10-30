-- Dữ liệu minh họa theo mô tả nhóm; thông tin liên hệ chỉ dùng để demo.
-- 1. Dữ liệu CHI_NHANH
INSERT INTO CHI_NHANH VALUES ('CN01', 'Hiệu sách Cơ sở Hà Đông', 'Km10 Nguyễn Trãi, Hà Đông, Hà Nội', '0900001111', 'Hoạt động');
INSERT INTO CHI_NHANH VALUES ('CN02', 'Hiệu sách Cơ sở Cầu Giấy', '122 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', '0900002222', 'Hoạt động');

-- 2. Dữ liệu NHAN_VIEN
INSERT INTO NHAN_VIEN VALUES ('NV01', 'Nguyễn Việt Anh', '1999-05-12', 'Nam', '0900002233', 'Quản trị viên', '2024-01-10', 'Đang làm việc', 'CN01');
INSERT INTO NHAN_VIEN VALUES ('NV02', 'Đặng Đình Quang', '2003-08-20', 'Nam', '0900003344', 'Nhân viên bán hàng', '2025-02-15', 'Đang làm việc', 'CN01');
INSERT INTO NHAN_VIEN VALUES ('NV03', 'Đoàn Mạnh Sơn', '2002-11-05', 'Nam', '0900004455', 'Nhân viên nhập hàng', '2025-03-01', 'Đang làm việc', 'CN01');
INSERT INTO NHAN_VIEN VALUES ('NV04', 'Mạc Đình Toàn', '2003-04-18', 'Nam', '0900005566', 'Nhân viên bán hàng', '2025-06-01', 'Đang làm việc', 'CN02');

-- 3. Dữ liệu KHACH_HANG
INSERT INTO KHACH_HANG(MaKH,HoTen,SoDienThoai,Email,DiaChi,NgayDangKy) VALUES ('KH01', 'Phạm Trường An', '0900005678', 'demo@example.com', 'Thanh Xuân, Hà Nội', '2026-01-15');
INSERT INTO KHACH_HANG(MaKH,HoTen,SoDienThoai,Email,DiaChi,NgayDangKy) VALUES ('KH02', 'Trần Thị Thu Hà', '0900004321', 'demo@example.com', 'Hoàng Mai, Hà Nội', '2026-03-20');

-- 4. Dữ liệu THE_LOAI
INSERT INTO THE_LOAI VALUES ('TL01', 'Công nghệ thông tin', 'Sách giáo trình lập trình, cơ sở dữ liệu, mạng máy tính');
INSERT INTO THE_LOAI VALUES ('TL02', 'Ngoại ngữ', 'Sách luyện thi tiếng Nhật JLPT, tiếng Anh IELTS/TOEIC');
INSERT INTO THE_LOAI VALUES ('TL03', 'Văn học', 'Tiểu thuyết, truyện ngắn trong nước và quốc tế');

-- 5. Dữ liệu SACH
INSERT INTO SACH(MaSach,ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL) VALUES ('S001', '978-604-80-1001', 'Bài giảng Cơ sở dữ liệu', 'Nguyễn Quỳnh Chi', 'NXB Thông tin và Truyền thông', 2024, 125000, 'Đang kinh doanh', 'TL01');
INSERT INTO SACH(MaSach,ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL) VALUES ('S002', '978-604-80-1002', 'Cấu trúc dữ liệu và Giải thuật C++', 'Lê Minh Hoàng', 'NXB Đại học Quốc gia', 2025, 155000, 'Đang kinh doanh', 'TL01');
INSERT INTO SACH(MaSach,ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL) VALUES ('S003', '978-604-80-1003', 'Minna no Nihongo Sơ cấp II (N4)', '3A Corporation', 'NXB Giáo dục Việt Nam', 2025, 110000, 'Đang kinh doanh', 'TL02');
INSERT INTO SACH(MaSach,ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL) VALUES ('S004', '978-604-80-1004', 'Mắt Biếc', 'Nguyễn Nhật Ánh', 'NXB Trẻ', 2023, 95000, 'Đang kinh doanh', 'TL03');

-- 6. Dữ liệu NHA_CUNG_CAP
INSERT INTO NHA_CUNG_CAP VALUES ('NCC01', 'Công ty Phát hành sách FAHASA', '60 Nguyễn Huệ, TP.HCM', '0900005796', 'demo@example.com');
INSERT INTO NHA_CUNG_CAP VALUES ('NCC02', 'Công ty Sách và Thiết bị Giáo dục Hà Nội', '45 Lý Thường Kiệt, Hà Nội', '0900005432', 'demo@example.com');

-- 7. Dữ liệu PHIEU_NHAP
INSERT INTO PHIEU_NHAP VALUES ('PN20261001', '2026-10-01 09:00:00', 'Đã xác nhận', 'NCC02', 'NV03', 'CN01');
INSERT INTO PHIEU_NHAP VALUES ('PN20261002', '2026-10-05 14:30:00', 'Đã xác nhận', 'NCC01', 'NV01', 'CN02');

-- 8. Dữ liệu CT_PHIEU_NHAP
INSERT INTO CT_PHIEU_NHAP VALUES ('PN20261001', 'S001', 50, 90000);
INSERT INTO CT_PHIEU_NHAP VALUES ('PN20261001', 'S002', 10, 115000);
INSERT INTO CT_PHIEU_NHAP VALUES ('PN20261001', 'S003', 8, 80000);
INSERT INTO CT_PHIEU_NHAP VALUES ('PN20261002', 'S001', 30, 90000);
INSERT INTO CT_PHIEU_NHAP VALUES ('PN20261002', 'S004', 25, 68000);

-- 9. Dữ liệu HOA_DON_BAN
INSERT INTO HOA_DON_BAN VALUES ('HDB20261001', '2026-10-20 10:15:00', 'Hoàn tất', 'Chuyển khoản', 'NV02', 'CN01', 'KH01');
INSERT INTO HOA_DON_BAN VALUES ('HDB20261002', '2026-10-22 16:40:00', 'Hoàn tất', 'Tiền mặt', 'NV02', 'CN01', NULL); -- Khách lẻ
INSERT INTO HOA_DON_BAN VALUES ('HDB20261003', '2026-10-25 11:00:00', 'Hoàn tất', 'Thẻ', 'NV04', 'CN02', 'KH02');

-- 10. Dữ liệu CT_HOA_DON_BAN
INSERT INTO CT_HOA_DON_BAN VALUES ('HDB20261001', 'S001', 3, 125000);
INSERT INTO CT_HOA_DON_BAN VALUES ('HDB20261001', 'S002', 6, 155000);
INSERT INTO CT_HOA_DON_BAN VALUES ('HDB20261002', 'S003', 5, 110000);
INSERT INTO CT_HOA_DON_BAN VALUES ('HDB20261003', 'S004', 4, 95000);

-- 11. Dữ liệu TON_KHO (Sau khi cộng nhập và trừ bán)
INSERT INTO TON_KHO VALUES ('CN01', 'S001', 47);
INSERT INTO TON_KHO VALUES ('CN01', 'S002', 4);  -- Sắp hết hàng (< 5 cuốn)
INSERT INTO TON_KHO VALUES ('CN01', 'S003', 3);  -- Sắp hết hàng (< 5 cuốn)
INSERT INTO TON_KHO VALUES ('CN02', 'S001', 30);
INSERT INTO TON_KHO VALUES ('CN02', 'S004', 21);
