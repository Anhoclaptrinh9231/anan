-- Du lieu demo tu tao, muon mot so ten hang tu bai mau; khong chep nguyen ket qua bai mau.
-- Chi nap vao CSDL trong lan chay dau tien.
INSERT INTO CHUCH VALUES ('Nguyễn Vũ Minh','Cầu Giấy - Hà Nội');
INSERT INTO CHU_SDT VALUES ('Nguyễn Vũ Minh','0900000001'),('Nguyễn Vũ Minh','0900000002');
INSERT INTO CUAHANG VALUES ('Hiệu sách Tri Thức','Hà Đông - Hà Nội','Nguyễn Vũ Minh');
INSERT INTO CUAHANG_SDT VALUES ('Hiệu sách Tri Thức','02430000001');
INSERT INTO NHANVIEN VALUES
('NV001','Hoàng Văn Thanh','Hưng Yên','2000-03-12','Nam','Bán hàng'),
('NV002','Ngô Phương Anh','Hải Dương','2001-06-10','Nữ','Bán hàng'),
('NV003','Nguyễn Thu Hương','Hoàng Mai - Hà Nội','2000-04-08','Nữ','Bán hàng'),
('NV004','Phạm Tiến Đạt','Hà Đông - Hà Nội','2001-01-20','Nam','Bán hàng'),
('NV005','Vũ Thùy Linh','Hưng Yên','2000-02-14','Nữ','Bán hàng'),
('NV006','Trần Nam Anh','Đống Đa - Hà Nội','1999-10-15','Nam','Nhập hàng'),
('NV007','Đỗ Ngọc Trâm','Ba Vì - Hà Nội','2002-12-04','Nữ','Nhập hàng'),
('NV008','Nguyễn Hoàng Long','Cầu Giấy - Hà Nội','2001-07-01','Nam','Nhập hàng'),
('NV009','Lê Quỳnh Anh','Hà Đông - Hà Nội','2000-08-09','Nữ','Nhập hàng'),
('NV010','Bùi Thế Đức','Hà Đông - Hà Nội','1999-09-12','Nam','Nhập hàng');
INSERT INTO NHANVIEN_SDT VALUES ('NV001','0901000001'),('NV002','0901000002'),('NV002','0901000012'),('NV006','0901000006'),('NV009','0901000009');
INSERT INTO LAM SELECT MaNV,'Hiệu sách Tri Thức' FROM NHANVIEN;
INSERT INTO HANGHOA VALUES
('HD001','Bút lông bảng HQ',12000,120,'DUNGCU'),
('HD002','Keo khô FlexOffice FO-G03',11200,80,'DUNGCU'),
('HD003','Hộp chì màu F13',32400,80,'DUNGCU'),
('HD004','Compa Hồng Hà C-05',8600,105,'DUNGCU'),
('HD005','Dao rọc giấy 156mm',12900,47,'DUNGCU'),
('HD006','Tập vở Campus 12 cung hoàng đạo',7290,130,'DUNGCU'),
('HD007','Thước thẳng 30 cm',9450,156,'DUNGCU'),
('HD008','Thước thẳng 20 cm',3885,200,'DUNGCU'),
('HD009','Bảng học sinh Thiên Long B-08',9460,120,'DUNGCU'),
('HD010','Dao rọc giấy văn phòng',12650,59,'DUNGCU'),
('HD011','Sổ caro 25x35 (M) SB-03',35640,20,'DUNGCU'),
('HD012','Bút bi 0.5cm Thiên Long',2000,500,'DUNGCU'),
('HD013','Sổ A4 KLONG',20000,200,'DUNGCU'),
('HS001','Cho tôi xin một vé đi tuổi thơ',51000,35,'SACH'),
('HS002','Nhà giả kim',59000,40,'SACH'),
('HS003','Tạm biệt, em ổn',56000,12,'SACH'),
('HS004','Đắc nhân tâm',65000,18,'SACH'),
('HS005','Chúng ta rồi sẽ ổn thôi',86000,45,'SACH'),
('HS006','Mỉm cười cho qua',60000,34,'SACH'),
('HS007','Open the Window, Eyes Closed',58000,15,'SACH'),
('HS008','Cà phê cùng Tony',54000,30,'SACH'),
('HS009','Kỹ năng đọc hiểu văn bản Ngữ văn 12',44000,18,'SACH'),
('HS010','Người cũ còn thương',58000,29,'SACH'),
('HS011','Đừng bao giờ đi ăn một mình',59250,25,'SACH'),
('HS012','Bài tập bổ trợ nâng cao tiếng Anh 10',51000,29,'SACH'),
('HS013','Tôi thấy hoa vàng trên cỏ xanh',61500,50,'SACH'),
('HS014','Rèn luyện kỹ năng giải toán Vật lý 10',45000,32,'SACH');
INSERT INTO DUNGCU VALUES ('HD001','Bút'),('HD002','Keo'),('HD003','Chì màu'),('HD004','Compa'),('HD005','Dao rọc giấy'),('HD006','Vở'),('HD007','Thước'),('HD008','Thước'),('HD009','Bảng học sinh'),('HD010','Dao rọc giấy'),('HD011','Sổ'),('HD012','Bút'),('HD013','Sổ');
INSERT INTO SACH VALUES
('HS001','Nguyễn Nhật Ánh','Tiểu thuyết'),('HS002','Paulo Coelho','Văn học nước ngoài'),
('HS003','Tờ Pi','Tản văn'),('HS004','Dale Carnegie','Kỹ năng sống'),
('HS005','Gào; Minh Nhật','Tản văn'),('HS006','Iris Cao; Hamlet Trương','Tản văn'),
('HS007','Nguyễn Ngọc Thuần','Sách tiếng Anh'),('HS008','Tony Buổi Sáng','Kỹ năng sống'),
('HS009','Nguyễn Kim Phong','Giáo dục'),('HS010','Nguyễn Ngọc Thạch','Tản văn'),
('HS011','Keith Ferrazzi','Kỹ năng sống'),('HS012','Nguyễn Thị Chi','Giáo dục'),
('HS013','Nguyễn Nhật Ánh','Tiểu thuyết'),('HS014','Mai Chánh Trí','Giáo dục');
INSERT INTO SACH_TB VALUES ('HS001','Lần thứ ba'),('HS001','Lần thứ tư'),('HS002','Lần thứ nhất'),('HS004','Lần thứ nhất'),('HS012','Lần thứ ba');
INSERT INTO KHACHHANG VALUES
('KH001','Nguyễn Thúy Hằng','Hà Đông - Hà Nội','Nữ','2005-04-12'),
('KH002','Khúc Xuân Duy','Cầu Giấy - Hà Nội','Nam','2004-06-23'),
('KH003','Lê Hương Giang','Bắc Ninh','Nữ','1998-01-10'),
('KH004','Lê Thị Dương','Hải Dương','Nữ','1986-12-07'),
('KH005','Lê Xuân Nhật','Hà Đông - Hà Nội','Nam','1991-02-18'),
('KH006','Bùi Thế Đức','Hoàn Kiếm - Hà Nội','Nam','2003-08-14'),
('KH007','Vũ Thị Nga','Hà Đông - Hà Nội','Nữ','2002-03-19'),
('KH008','Chu An Dương','Bắc Ninh','Nam','2004-05-20');
INSERT INTO KHACHHANG_SDT VALUES ('KH001','0902000001'),('KH002','0902000002'),('KH002','0902000012'),('KH003','0902000003'),('KH004','0902000004'),('KH005','0902000005'),('KH006','0902000006'),('KH007','0902000007'),('KH008','0902000008');
INSERT INTO NHACUNGCAP VALUES
('CD001','Văn phòng phẩm Phú Thịnh','Văn Giang - Hưng Yên'),
('CD002','Văn phòng phẩm Minh Nam','Thanh Xuân - Hà Nội'),
('CD009','Sách và thiết bị Cửu Long','Định Công - Hà Nội'),
('CS007','Nhà xuất bản Trẻ','Hà Đông - Hà Nội'),
('CS010','Nhà xuất bản Kim Đồng','Hai Bà Trưng - Hà Nội');
INSERT INTO NHACUNGCAP_SDT VALUES ('CD001','0903000001'),('CD002','0903000002'),('CD009','0903000009'),('CS007','0903000007'),('CS010','0903000010');
INSERT INTO CHINHANH VALUES
('Chi nhánh Hưng Yên','CD001','Kim Động - Hưng Yên'),
('Chi nhánh Hải Dương','CD001','Kim Thành - Hải Dương'),
('Chi nhánh Hải Phòng','CD002','Tam Bạc - Hải Phòng'),
('Chi nhánh Hà Nội','CD009','Hà Đông - Hà Nội'),
('Chi nhánh số 1','CS007','Hà Đông - Hà Nội'),
('Chi nhánh số 2','CS007','Hoàn Kiếm - Hà Nội'),
('Chi nhánh số 1','CS010','Hai Bà Trưng - Hà Nội');
INSERT INTO HOADONNHAP VALUES
('HDN001','2026-10-20','NV006','CD001','Chi nhánh Hưng Yên'),
('HDN002','2026-10-21','NV009','CS007','Chi nhánh số 1'),
('HDN003','2026-10-23','NV008','CD009','Chi nhánh Hà Nội');
INSERT INTO NHAPHANG VALUES
('HDN001','HD001',30,8000),('HDN001','HD011',20,25000),
('HDN002','HS001',10,35000),('HDN002','HS013',10,42000),
('HDN003','HS002',10,40000),('HDN003','HS004',10,45000);
INSERT INTO HOADONBAN VALUES
('HDB001','2026-10-22','NV001','KH001'),('HDB002','2026-10-23','NV002','KH007'),
('HDB003','2026-10-24','NV003','KH003'),('HDB004','2026-10-25','NV004','KH005'),
('HDB005','2026-10-26','NV001','KH007'),('HDB006','2026-10-27','NV005','KH002'),
('HDB007','2026-10-28','NV002','KH004'),('HDB008','2026-10-28','NV001','KH007');
INSERT INTO BANHANG VALUES
('HDB001','HS001',2,51000),('HDB001','HD012',5,2000),
('HDB002','HD011',12,35640),('HDB002','HS002',2,59000),
('HDB003','HS004',1,65000),('HDB003','HD006',4,7290),
('HDB004','HS013',3,61500),('HDB004','HD001',2,12000),
('HDB005','HD011',8,35640),('HDB005','HS001',1,51000),
('HDB006','HS008',3,54000),('HDB006','HD013',2,20000),
('HDB007','HS003',1,56000),('HDB007','HD003',1,32400),
('HDB008','HD011',5,35640),('HDB008','HS004',2,65000);
-- Ton hien tai = ton dau ky + nhap - ban.
UPDATE HANGHOA h SET SoLuong=SoLuong
    + COALESCE((SELECT SUM(n.SoLuong) FROM NHAPHANG n WHERE n.MaHH=h.MaHH),0)
    - COALESCE((SELECT SUM(b.SoLuong) FROM BANHANG b WHERE b.MaHH=h.MaHH),0);
