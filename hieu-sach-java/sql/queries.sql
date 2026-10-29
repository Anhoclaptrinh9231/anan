-- SQL tương ứng giao diện Java, mô hình 11 bảng.
-- Dấu ? là tham số PreparedStatement, không nối giá trị do người dùng nhập vào SQL.

-- Form sách: thể loại và cập nhật giá/trạng thái.
SELECT MaTL,TenTheLoai FROM THE_LOAI ORDER BY TenTheLoai;
INSERT INTO SACH(MaSach,ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL) VALUES(?,?,?,?,?,?,?,?,?);
UPDATE SACH SET TenSach=?,DonGiaBan=?,MaTL=? WHERE MaSach=?;
UPDATE SACH SET TrangThai='Ngừng kinh doanh' WHERE MaSach=?;

-- Hóa đơn / phiếu nhập: ngày giờ TIMESTAMP bắt buộc, khách lẻ có MaKH=NULL.
INSERT INTO HOA_DON_BAN(MaHDB,NgayBan,TrangThai,PhuongThucThanhToan,MaNV,MaCN,MaKH) VALUES(?,?,?,?,?,?,?);
INSERT INTO CT_HOA_DON_BAN VALUES(?,?,?,?);
UPDATE TON_KHO SET SoLuongTon=SoLuongTon-? WHERE MaCN=? AND MaSach=? AND SoLuongTon>=?;
INSERT INTO PHIEU_NHAP VALUES(?,?,?,?,?,?);
INSERT INTO CT_PHIEU_NHAP VALUES(?,?,?,?);
UPDATE TON_KHO SET SoLuongTon=SoLuongTon+? WHERE MaCN=? AND MaSach=?;
-- Các bước của cùng chứng từ chạy trong một transaction và kiểm tra số dòng cập nhật.
-- Xóa phiếu đã xác nhận: trừ đúng lượng nhập với điều kiện tồn không âm, rồi đổi trạng thái.
UPDATE PHIEU_NHAP SET TrangThai='Đã hủy' WHERE MaPN=?;

-- Tìm kiếm kết hợp và xem tồn từng cửa hàng, kể cả tồn 0.
SELECT s.MaSach,s.ISBN,s.TenSach,s.TacGia,l.TenTheLoai,s.DonGiaBan,c.TenCN,COALESCE(t.SoLuongTon,0) AS TonKhaDung
FROM SACH s JOIN THE_LOAI l ON l.MaTL=s.MaTL CROSS JOIN CHI_NHANH c
LEFT JOIN TON_KHO t ON t.MaCN=c.MaCN AND t.MaSach=s.MaSach
WHERE (LOWER(s.TenSach) LIKE ? OR LOWER(s.TacGia) LIKE ? OR LOWER(s.ISBN) LIKE ?)
AND (?='' OR c.MaCN=?) AND (?='' OR s.MaTL=?) AND s.DonGiaBan BETWEEN ? AND ?
ORDER BY s.MaSach,c.MaCN;

-- Báo cáo hoặc biểu đồ: cùng câu SQL đang dùng trong Java.
SELECT EXTRACT(MONTH
FROM h.NgayBan) AS "month",COUNT(DISTINCT h.MaHDB) AS invoices,SUM(d.SoLuongBan) AS quantity,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS revenue
FROM HOA_DON_BAN h
JOIN CT_HOA_DON_BAN d ON d.MaHDB=h.MaHDB
WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<? AND (?='' OR h.MaCN=?)
GROUP BY EXTRACT(MONTH
FROM h.NgayBan)
ORDER BY EXTRACT(MONTH
FROM h.NgayBan);

-- Báo cáo hoặc biểu đồ: cùng câu SQL đang dùng trong Java.
WITH DT AS (SELECT h.MaCN,COUNT(DISTINCT h.MaHDB) AS SoHD,SUM(d.SoLuongBan) AS SLBan,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS DoanhThu
FROM HOA_DON_BAN h
JOIN CT_HOA_DON_BAN d ON d.MaHDB=h.MaHDB
WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<?
GROUP BY h.MaCN), CP AS (SELECT p.MaCN,COUNT(DISTINCT p.MaPN) AS SoPN,SUM(d.SoLuongNhap) AS SLNhap,SUM(d.SoLuongNhap*d.DonGiaNhap) AS ChiPhi
FROM PHIEU_NHAP p
JOIN CT_PHIEU_NHAP d ON d.MaPN=p.MaPN
WHERE p.TrangThai='Đã xác nhận' AND p.NgayNhap>=? AND p.NgayNhap<?
GROUP BY p.MaCN) SELECT c.MaCN AS "Mã CN",c.TenCN AS "Chi nhánh",COALESCE(DT.SoHD,0) AS "Hóa đơn hoàn tất",COALESCE(DT.SLBan,0) AS "Số sách bán",COALESCE(DT.DoanhThu,0) AS "Doanh thu (đ)",COALESCE(CP.SoPN,0) AS "Phiếu đã xác nhận",COALESCE(CP.SLNhap,0) AS "Số sách nhập",COALESCE(CP.ChiPhi,0) AS "Chi phí nhập (đ)"
FROM CHI_NHANH c
LEFT JOIN DT ON DT.MaCN=c.MaCN
LEFT JOIN CP ON CP.MaCN=c.MaCN
WHERE (?='' OR c.MaCN=?)
ORDER BY COALESCE(DT.DoanhThu,0) DESC,c.MaCN;

-- Báo cáo hoặc biểu đồ: cùng câu SQL đang dùng trong Java.
SELECT c.MaCN AS "Mã CN",c.TenCN AS "Chi nhánh",s.MaSach AS "Mã sách",s.ISBN AS "ISBN",s.TenSach AS "Tên sách",l.TenTheLoai AS "Thể loại",t.SoLuongTon AS "Tồn kho"
FROM TON_KHO t
JOIN CHI_NHANH c ON c.MaCN=t.MaCN
JOIN SACH s ON s.MaSach=t.MaSach
JOIN THE_LOAI l ON l.MaTL=s.MaTL
WHERE t.SoLuongTon<=? AND s.TrangThai='Đang kinh doanh' AND (?='' OR c.MaCN=?)
ORDER BY t.SoLuongTon,c.MaCN,s.MaSach;

-- Báo cáo hoặc biểu đồ: cùng câu SQL đang dùng trong Java.
WITH T AS (SELECT s.MaSach,s.ISBN,s.TenSach,l.TenTheLoai,SUM(d.SoLuongBan) AS Tong,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS Tien
FROM SACH s
JOIN THE_LOAI l ON l.MaTL=s.MaTL
JOIN CT_HOA_DON_BAN d ON d.MaSach=s.MaSach
JOIN HOA_DON_BAN h ON h.MaHDB=d.MaHDB
WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<? AND (?='' OR h.MaCN=?)
GROUP BY s.MaSach,s.ISBN,s.TenSach,l.TenTheLoai) SELECT MaSach AS "Mã sách",ISBN AS "ISBN",TenSach AS "Tên sách",TenTheLoai AS "Thể loại",Tong AS "Số lượng bán",Tien AS "Doanh thu (đ)"
FROM T
WHERE Tong>=(SELECT AVG(CAST(Tong AS DECIMAL(18,2)))
FROM T)
ORDER BY Tong DESC,Tien DESC,MaSach;

