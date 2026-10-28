-- CAC CAU TRONG ANH: chuyen sang schema dang dung, khong chep nguyen SQL Access.
-- File nay co the chay tung cau truc tiep trong H2 Console. Du lieu demo khac anh nen ket qua khac.
-- Cac ngay 2015 trong anh duoc giu o cau 2/7 de minh hoa; tren web duoc chon khoang ngay.

-- CAU 1: chi nhanh o Ha Noi duoc nhan vien nam nhap nhieu lan nhat.
-- Mot lan = mot HOA DON, khong dem moi dong hang la mot lan nhap.
WITH T AS (
 SELECT c.MaCC,c.TenChiNhanh,n.Ten,COUNT(DISTINCT h.MaHDN) AS SoLan
 FROM CHINHANH c JOIN NHACUNGCAP n ON n.MaCC=c.MaCC
 JOIN HOADONNHAP h ON h.MaCC=c.MaCC AND h.TenChiNhanh=c.TenChiNhanh
 JOIN NHANVIEN v ON v.MaNV=h.MaNV
 WHERE c.DiaChi LIKE '%Hà Nội%' AND v.GioiTinh='Nam'
 GROUP BY c.MaCC,c.TenChiNhanh,n.Ten
)
SELECT * FROM T WHERE SoLan=(SELECT MAX(SoLan) FROM T);

-- CAU 2: khach Ha Dong mua nhieu hang nhat tu nam 2015.
WITH T AS (
 SELECT k.MaKH,k.Ten,SUM(b.SoLuong) AS Tong
 FROM KHACHHANG k JOIN HOADONBAN h ON h.MaKH=k.MaKH JOIN BANHANG b ON b.MaHDB=h.MaHDB
 WHERE k.DiaChi LIKE '%Hà Đông%' AND h.NgayBan>=DATE '2015-01-01'
 GROUP BY k.MaKH,k.Ten
)
SELECT * FROM T WHERE Tong=(SELECT MAX(Tong) FROM T);

-- CAU 3: nhan vien da nhap tren 5 loai hang khac nhau tu NCC o Ha Noi.
-- SoLanNhap dem hoa don; SoLoaiHang dem ma hang phan biet.
SELECT v.MaNV,v.Ten,COUNT(DISTINCT h.MaHDN) AS SoLanNhap,COUNT(DISTINCT d.MaHH) AS SoLoaiHang
FROM NHANVIEN v JOIN HOADONNHAP h ON h.MaNV=v.MaNV
JOIN NHACUNGCAP n ON n.MaCC=h.MaCC JOIN NHAPHANG d ON d.MaHDN=h.MaHDN
WHERE n.DiaChi LIKE '%Hà Nội%'
GROUP BY v.MaNV,v.Ten HAVING COUNT(DISTINCT d.MaHH)>5;

-- CAU 4: nhan vien va khach cung co SDT dau 016.
-- Giu 016 theo de trong anh; du lieu demo moi khong co dau nay nen co the rong.
-- EXISTS tranh nhan ban so hoa don khi moi nguoi co nhieu SDT.
SELECT v.MaNV,v.Ten,COUNT(DISTINCT h.MaHDB) AS SoLanBan
FROM NHANVIEN v JOIN HOADONBAN h ON h.MaNV=v.MaNV
WHERE EXISTS(SELECT 1 FROM NHANVIEN_SDT s WHERE s.MaNV=v.MaNV AND s.SDT LIKE '016%')
AND EXISTS(SELECT 1 FROM KHACHHANG_SDT s WHERE s.MaKH=h.MaKH AND s.SDT LIKE '016%')
GROUP BY v.MaNV,v.Ten;

-- CAU 5: khach tra nhieu tien nhat trong tung khu vuc.
-- Chi gom khach da mua hang; giu tat ca dong hang.
WITH T AS (
 SELECT k.DiaChi,k.MaKH,k.Ten,SUM(b.SoLuong*b.DonGiaBan) AS SoTien
 FROM KHACHHANG k JOIN HOADONBAN h ON h.MaKH=k.MaKH JOIN BANHANG b ON b.MaHDB=h.MaHDB
 GROUP BY k.DiaChi,k.MaKH,k.Ten
)
SELECT * FROM T t WHERE SoTien=(SELECT MAX(t2.SoTien) FROM T t2 WHERE t2.DiaChi IS NOT DISTINCT FROM t.DiaChi)
ORDER BY DiaChi,MaKH;

-- CAU 6: NCC TUNG NHAP MAT HANG co doanh thu ban cao nhat.
-- Khong phan bo doanh thu cho NCC vi schema chua co truy vet lo nhap xuat.
-- Mot mat hang duoc nhieu NCC cung cap thi liet ke cac NCC tung nhap mat hang do.
-- He so 0.3 trong anh khong phai dinh nghia doanh thu; o day dung tien ban thuc te.
WITH T AS (
 SELECT b.MaHH,SUM(b.SoLuong*b.DonGiaBan) AS DoanhThu FROM BANHANG b GROUP BY b.MaHH
)
SELECT DISTINCT n.MaCC,n.Ten,t.MaHH,t.DoanhThu AS DoanhThuCuaMatHang
FROM T t JOIN NHAPHANG d ON d.MaHH=t.MaHH JOIN HOADONNHAP h ON h.MaHDN=d.MaHDN
JOIN NHACUNGCAP n ON n.MaCC=h.MaCC
WHERE t.DoanhThu=(SELECT MAX(DoanhThu) FROM T)
ORDER BY n.MaCC;

-- CAU 7: hang duoc khach Ha Dong mua nhieu nhat tu nam 2015.
WITH T AS (
 SELECT g.MaHH,g.Ten,SUM(b.SoLuong) AS Tong
 FROM HANGHOA g JOIN BANHANG b ON b.MaHH=g.MaHH JOIN HOADONBAN h ON h.MaHDB=b.MaHDB
 JOIN KHACHHANG k ON k.MaKH=h.MaKH
 WHERE k.DiaChi LIKE '%Hà Đông%' AND h.NgayBan>=DATE '2015-01-01'
 GROUP BY g.MaHH,g.Ten
)
SELECT * FROM T WHERE Tong=(SELECT MAX(Tong) FROM T);
