-- SQL dung trong ung dung: JDBC thay cac dau ? bang gia tri nguoi dung nhap.
-- Muon chay truc tiep trong H2 Console: thay ? bang gia tri nhu ghi chu.

-- FORM 1 - Nhap sach (thuc hien trong 1 transaction).
-- INSERT INTO HANGHOA(MaHH,Ten,Gia,SoLuong,Loai) VALUES(?,?,?,?, 'SACH');
-- INSERT INTO SACH(MaHHS,TacGia,TheLoai) VALUES(?,?,?);
-- INSERT INTO SACH_TB(MaHHS,TaiBan) VALUES(?,?);

-- FORM 2 - Nhap khach hang (thuc hien trong 1 transaction).
-- INSERT INTO KHACHHANG(MaKH,Ten,DiaChi,GioiTinh,NgaySinh) VALUES(?,?,?,?,?);
-- INSERT INTO KHACHHANG_SDT(MaKH,SDT) VALUES(?,?);

-- TIM KIEM. 4 tham so dau: '%nhật ánh%', 2 tham so sau: '' hoac 'SACH'.
SELECT h.MaHH,h.Ten,h.Gia,h.SoLuong,h.Loai,s.TacGia,s.TheLoai,d.Loai AS NhomDungCu
FROM HANGHOA h LEFT JOIN SACH s ON s.MaHHS=h.MaHH LEFT JOIN DUNGCU d ON d.MaHHD=h.MaHH
WHERE (LOWER(h.MaHH) LIKE ? ESCAPE '!' OR LOWER(h.Ten) LIKE ? ESCAPE '!'
  OR LOWER(COALESCE(s.TacGia,'')) LIKE ? ESCAPE '!'
  OR LOWER(COALESCE(s.TheLoai,d.Loai,'')) LIKE ? ESCAPE '!')
  AND (?='' OR h.Loai=?)
ORDER BY h.MaHH;

-- BAO CAO 1 - Doanh thu. Tham so: DATE '2026-01-01', DATE '2026-12-31'.
SELECT h.NgayBan AS NgayBan,COUNT(DISTINCT h.MaHDB) AS SoHoaDon,
       SUM(b.SoLuong) AS SoLuongBan,SUM(b.SoLuong*b.DonGiaBan) AS DoanhThu
FROM HOADONBAN h JOIN BANHANG b ON b.MaHDB=h.MaHDB
WHERE h.NgayBan BETWEEN ? AND ?
GROUP BY h.NgayBan ORDER BY h.NgayBan;

-- BAO CAO 2 - Ton kho. Tham so: 20. Day la gia tri theo GIA BAN, khong phai gia von.
SELECT MaHH,Ten,Loai,SoLuong,Gia,SoLuong*Gia AS GiaTriTheoGiaBan
FROM HANGHOA WHERE SoLuong<=? ORDER BY SoLuong,MaHH;

-- BAO CAO 3 - Cau 2 bai mau: khach mua nhieu nhat tai khu vuc trong khoang ngay.
-- Tham so: '%hà đông%', DATE '2026-01-01', DATE '2026-12-31'. Giu tat ca dong dong hang.
WITH T AS (
  SELECT k.MaKH,k.Ten,SUM(b.SoLuong) AS Tong
  FROM KHACHHANG k JOIN HOADONBAN h ON h.MaKH=k.MaKH
  JOIN BANHANG b ON b.MaHDB=h.MaHDB
  WHERE LOWER(k.DiaChi) LIKE ? ESCAPE '!' AND h.NgayBan BETWEEN ? AND ?
  GROUP BY k.MaKH,k.Ten
)
SELECT MaKH,Ten,Tong FROM T WHERE Tong=(SELECT MAX(Tong) FROM T) ORDER BY MaKH;

-- BAO CAO 4 - Cau 7 bai mau: hang ban nhieu nhat tai khu vuc trong khoang ngay.
WITH T AS (
  SELECT g.MaHH,g.Ten,SUM(b.SoLuong) AS Tong
  FROM HANGHOA g JOIN BANHANG b ON b.MaHH=g.MaHH
  JOIN HOADONBAN h ON h.MaHDB=b.MaHDB JOIN KHACHHANG k ON k.MaKH=h.MaKH
  WHERE LOWER(k.DiaChi) LIKE ? ESCAPE '!' AND h.NgayBan BETWEEN ? AND ?
  GROUP BY g.MaHH,g.Ten
)
SELECT MaHH,Ten,Tong FROM T WHERE Tong=(SELECT MAX(Tong) FROM T) ORDER BY MaHH;

-- BAN HANG - ghi hoa don, chi tiet va tru kho trong 1 transaction.
-- INSERT INTO HOADONBAN VALUES(?,?,?,?);
-- SELECT Gia,Ten FROM HANGHOA WHERE MaHH=? FOR UPDATE;
-- UPDATE HANGHOA SET SoLuong=SoLuong-? WHERE MaHH=? AND SoLuong>=?;
-- INSERT INTO BANHANG VALUES(?,?,?,?);
-- Neu UPDATE khong sua duoc dong nao: ROLLBACK toan bo hoa don.
-- DonGiaBan luu gia tai luc ban, khong tinh lai bang gia hien tai.

-- NHAP HANG - gia nhap luu rieng, khong tu dong thay doi gia ban.
-- INSERT INTO HOADONNHAP VALUES(?,?,?,?,?);
-- INSERT INTO NHAPHANG VALUES(?,?,?,?);
-- UPDATE HANGHOA SET SoLuong=SoLuong+? WHERE MaHH=?;
