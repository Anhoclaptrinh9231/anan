# Hệ CSDL quản lý hiệu sách và văn phòng phẩm Tri Thức

Đây là bản thiết kế đi cùng mã nguồn đang chạy, để nhóm phát triển báo cáo BTL. Nhóm tự điền tên trường, lớp, giảng viên, thành viên và phân công; không ghi đây là bài mẫu trên mạng được chép nguyên.

## 1. Mô tả yêu cầu dữ liệu

Cửa hàng quản lý nhân viên, khách hàng, hàng hóa gồm sách và dụng cụ học tập / văn phòng phẩm. Nhân viên bán hàng lập hóa đơn cho khách. Nhân viên nhập hàng ghi nhận hàng do các chi nhánh của nhà cung cấp giao. Hệ thống cần theo dõi số lượng tồn, tra cứu hàng hóa, thống kê doanh thu và mặt hàng / khách hàng nổi bật.

Mỗi chủ cửa hàng có tên, địa chỉ, nhiều số điện thoại. Mỗi cửa hàng có tên, địa chỉ, chủ quản lý và nhiều số điện thoại. Nhân viên có mã, tên, địa chỉ, ngày sinh, giới tính, nhiệm vụ và nhiều số điện thoại. Một nhân viên làm tại một cửa hàng trong phạm vi bài.

Hàng hóa có mã, tên, giá bán hiện tại, tồn kho và loại. Sách bổ sung tác giả, thể loại, các thông tin tái bản. Dụng cụ bổ sung nhóm như bút, sổ, vở. Khách hàng có mã, họ tên, địa chỉ, giới tính, ngày sinh, nhiều số điện thoại. Nhà cung cấp có mã, tên, địa chỉ, nhiều số điện thoại, nhiều chi nhánh; tên chi nhánh chỉ duy nhất trong từng nhà cung cấp.

Hóa đơn bán có mã, ngày, một khách hàng, một nhân viên và ít nhất một dòng hàng. Một dòng ghi một mặt hàng, số lượng, đơn giá bán lúc lập hóa đơn. Hóa đơn nhập có mã, ngày, một nhân viên nhập, một chi nhánh của nhà cung cấp và ít nhất một dòng hàng. Một dòng nhập có mặt hàng, số lượng, đơn giá nhập.

### Giả định và ràng buộc nghiệp vụ

- Tên chủ và tên cửa hàng duy nhất trong dữ liệu bài tập, dùng làm khóa tự nhiên theo mẫu. Không giả định tên người nói chung luôn duy nhất.
- Một hóa đơn chỉ có một khách hàng / một nhân viên; một phiếu nhập chỉ có một nhân viên / một chi nhánh. Các dòng cùng mặt hàng phải gộp số lượng.
- Hàng hóa thuộc **đúng một** trong hai nhóm SACH hoặc DUNGCU. Mã bảng con là mã bảng cha. Java tạo cha và con trong một transaction; SQL FK bảo đảm bảng con có cha. Ràng buộc phủ toàn phần và loại trừ chéo của hai bảng con được kiểm tra ở chương trình, chưa có trigger CSDL để cấm mọi trường hợp tự sửa SQL sai loại.
- TacGia là thông tin tác giả hiển thị của sách, không xây dựng danh mục / quan hệ đồng tác giả trong phạm vi này. Các ví dụ dữ liệu nhiều bút danh chỉ là một chuỗi ghi công, không dùng làm quan hệ tác giả độc lập.
- Điện thoại là chuỗi để giữ số 0 đầu; không giả định một số chỉ được dùng cho một chủ thể. Một cặp (chủ thể, số điện thoại) không lặp.
- Tái bản là thuộc tính đa trị theo ảnh nên dùng SACH_TB. Chưa quản lý tồn kho / ISBN riêng cho mỗi lần tái bản. Muốn quản lý từng ấn bản như hàng riêng phải mở rộng mô hình.
- Số lượng tồn không âm; số lượng giao dịch nguyên dương; giá là số nguyên VND không âm, DECIMAL(14,0). Tổng tiền không lưu lặp mà được SUM khi truy vấn.
- Tồn hiện tại là số dư nghiệp vụ, được cập nhật cùng giao dịch nhập/bán. Tồn đầu kỳ trong dữ liệu demo không phải một phiếu nhập. Chưa có trả hàng, khuyến mãi, VAT, công nợ, quản lý lô hoặc xác định giá vốn.
- Mã hàng / khách / nhân viên là giá trị nhận diện, không tính toán từ tên. Mã hóa đơn được Java sinh; CSDL vẫn có PK để chặn trùng.

## 2. ERD mức khái niệm

Mở `web/erd.svg` hoặc trang Cơ sở dữ liệu trên web để xem ERD với hình chữ nhật, hình thoi, thuộc tính ellipse và ISA. File chỉnh sửa sơ đồ là `docs/erd.dot`. Đây là **ERD điều chỉnh theo quy tắc đã nêu**, không nhận là bản sao nguyên hình ở ảnh 2.

- Chủ cửa hàng **quản lý** cửa hàng: 1:N.
- Cửa hàng **có / phân công** nhân viên: 1:N, một nhân viên làm tại một cửa hàng. LAM là bảng ánh xạ riêng giữ gần mẫu.
- HANGHOA chuyên biệt thành SACH và DUNGCU: ISA, loại trừ và phủ toàn phần theo quy tắc ứng dụng.
- Khách hàng **mua**, nhân viên **lập** HOADONBAN: mỗi phía 1:N tới hóa đơn.
- HOADONBAN **gồm** nhiều dòng BANHANG; mỗi dòng thuộc một hàng hóa. Đây là cách triển khai liên kết giữa hóa đơn và hàng hóa có thuộc tính số lượng, đơn giá.
- NHACUNGCAP **có** CHINHANH: 1:N. CHINHANH là thực thể phụ thuộc định danh vào nhà cung cấp.
- CHINHANH **giao** HOADONNHAP, NHANVIEN **lập** HOADONNHAP: 1:N.
- HOADONNHAP **gồm** các dòng NHAPHANG gắn HANGHOA.
- SDT của chủ / cửa hàng / nhân viên / khách / nhà cung cấp và tái bản của sách là thuộc tính đa trị, ánh xạ thành quan hệ riêng.

Giới hạn tham gia: một hóa đơn đã lưu qua ứng dụng có từ 1 đến 50 dòng. CSDL FK bảo đảm dòng tham chiếu hóa đơn có thật, còn yêu cầu hóa đơn phải có ít nhất một dòng được ứng dụng bảo đảm trong transaction. Nhân viên chỉ lập đúng loại hóa đơn theo NhiemVu được kiểm tra ở Java; FK chỉ kiểm tra nhân viên tồn tại.

## 3. Chuyển sang lược đồ quan hệ / Data Diagram

PK là khóa chính; FK là khóa ngoại. Danh sách dưới đây thống nhất với `sql/schema.sql`. Data Diagram đầy đủ xem `web/data-diagram.svg`, nguồn `docs/data-diagram.dot`.

| # | Quan hệ và thuộc tính | PK | FK |
|---|---|---|---|
| 1 | CHUCH(TenChu,DiaChi) | TenChu | — |
| 2 | CHU_SDT(TenChu,SDT) | TenChu,SDT | TenChu → CHUCH |
| 3 | CUAHANG(TenCH,DiaChi,TenChu) | TenCH | TenChu → CHUCH |
| 4 | CUAHANG_SDT(TenCH,SDT) | TenCH,SDT | TenCH → CUAHANG |
| 5 | NHANVIEN(MaNV,Ten,DiaChi,NgaySinh,GioiTinh,NhiemVu) | MaNV | — |
| 6 | NHANVIEN_SDT(MaNV,SDT) | MaNV,SDT | MaNV → NHANVIEN |
| 7 | LAM(MaNV,TenCH) | MaNV | MaNV → NHANVIEN; TenCH → CUAHANG |
| 8 | HANGHOA(MaHH,Ten,Gia,SoLuong,Loai) | MaHH | — |
| 9 | DUNGCU(MaHHD,Loai) | MaHHD | MaHHD → HANGHOA.MaHH |
| 10 | SACH(MaHHS,TacGia,TheLoai) | MaHHS | MaHHS → HANGHOA.MaHH |
| 11 | SACH_TB(MaHHS,TaiBan) | MaHHS,TaiBan | MaHHS → SACH |
| 12 | KHACHHANG(MaKH,Ten,DiaChi,GioiTinh,NgaySinh) | MaKH | — |
| 13 | KHACHHANG_SDT(MaKH,SDT) | MaKH,SDT | MaKH → KHACHHANG |
| 14 | NHACUNGCAP(MaCC,Ten,DiaChi) | MaCC | — |
| 15 | NHACUNGCAP_SDT(MaCC,SDT) | MaCC,SDT | MaCC → NHACUNGCAP |
| 16 | CHINHANH(TenChiNhanh,MaCC,DiaChi) | MaCC,TenChiNhanh | MaCC → NHACUNGCAP |
| 17 | HOADONBAN(MaHDB,NgayBan,MaNV,MaKH) | MaHDB | MaNV → NHANVIEN; MaKH → KHACHHANG |
| 18 | BANHANG(MaHDB,MaHH,SoLuong,DonGiaBan) | MaHDB,MaHH | MaHDB → HOADONBAN; MaHH → HANGHOA |
| 19 | HOADONNHAP(MaHDN,NgayNhap,MaNV,MaCC,TenChiNhanh) | MaHDN | MaNV → NHANVIEN; (MaCC,TenChiNhanh) → CHINHANH |
| 20 | NHAPHANG(MaHDN,MaHH,SoLuong,DonGiaNhap) | MaHDN,MaHH | MaHDN → HOADONNHAP; MaHH → HANGHOA |

Số điện thoại dùng VARCHAR(20); mã dùng VARCHAR(30); tên / địa chỉ dùng VARCHAR độ dài phù hợp; ngày dùng DATE; số lượng INT; tiền DECIMAL(14,0). H2 lưu được tiếng Việt trong VARCHAR. Trong Data Diagram, cạnh nối đi từ bảng được tham chiếu đến bảng chứa FK; dấu khóa PK/FK ghi tại cột. Với HOADONNHAP → CHINHANH phải tham chiếu **cả cặp** thuộc tính, không chỉ tên chi nhánh.

## 4. Phụ thuộc hàm và kiểm tra 3NF

Phụ thuộc hàm là quy tắc của dữ liệu hợp lệ, không phải suy đoán vì vài dòng trong bảng mẫu tình cờ khác nhau. Không thêm các phụ thuộc như SDT → MaKH, tên người → mã, giá → tên hàng.

### 4.1. Tập phụ thuộc hàm và khóa cho từng quan hệ

Mỗi ký hiệu `K → A,B` có thể tách thành các phụ thuộc một thuộc tính ở vế phải. Dưới các giả định đã nêu, không có các FD không tầm thường nào khác ngoài các FD này và những FD suy ra từ chúng.

| Quan hệ | FD không tầm thường cơ bản | Khóa dự bị trong phạm vi thiết kế |
|---|---|---|
| CHUCH | TenChu → DiaChi | {TenChu} |
| CHU_SDT | Không có | {TenChu,SDT} |
| CUAHANG | TenCH → DiaChi,TenChu | {TenCH} |
| CUAHANG_SDT | Không có | {TenCH,SDT} |
| NHANVIEN | MaNV → Ten,DiaChi,NgaySinh,GioiTinh,NhiemVu | {MaNV} |
| NHANVIEN_SDT | Không có | {MaNV,SDT} |
| LAM | MaNV → TenCH | {MaNV} |
| HANGHOA | MaHH → Ten,Gia,SoLuong,Loai | {MaHH} |
| DUNGCU | MaHHD → Loai | {MaHHD} |
| SACH | MaHHS → TacGia,TheLoai | {MaHHS} |
| SACH_TB | Không có | {MaHHS,TaiBan} |
| KHACHHANG | MaKH → Ten,DiaChi,GioiTinh,NgaySinh | {MaKH} |
| KHACHHANG_SDT | Không có | {MaKH,SDT} |
| NHACUNGCAP | MaCC → Ten,DiaChi | {MaCC} |
| NHACUNGCAP_SDT | Không có | {MaCC,SDT} |
| CHINHANH | (MaCC,TenChiNhanh) → DiaChi | {MaCC,TenChiNhanh} |
| HOADONBAN | MaHDB → NgayBan,MaNV,MaKH | {MaHDB} |
| BANHANG | (MaHDB,MaHH) → SoLuong,DonGiaBan | {MaHDB,MaHH} |
| HOADONNHAP | MaHDN → NgayNhap,MaNV,MaCC,TenChiNhanh | {MaHDN} |
| NHAPHANG | (MaHDN,MaHH) → SoLuong,DonGiaNhap | {MaHDN,MaHH} |

Ví dụ bao đóng trong BANHANG: `{MaHDB,MaHH}+ = {MaHDB,MaHH,SoLuong,DonGiaBan}`. Riêng `{MaHDB}+` hoặc `{MaHH}+` không xác định số lượng / đơn giá giao dịch. Do đó cặp là khóa và không bỏ được thành phần nào.

Trong CHINHANH, không có TenChiNhanh → MaCC vì nhiều nhà cung cấp có thể cùng có “Chi nhánh số 1”. Cặp (MaCC,TenChiNhanh) mới xác định địa chỉ.

### 4.2. 1NF

Mỗi ô giữ một giá trị thuộc miền đã chọn. Không đặt nhiều điện thoại trong một ô lưu trữ. Ví dụ form nhập `0901..., 0902...` được Java tách thành hai dòng KHACHHANG_SDT. Chuỗi nối số điện thoại trên giao diện chỉ là kết quả trình bày, không phải cấu trúc lưu trữ.

Tái bản được tách SACH_TB. Tên / địa chỉ / thông tin tác giả được coi là chuỗi văn bản của miền bài toán; bản này không hỗ trợ tra cứu một danh mục đồng tác giả riêng. Tổng tiền hóa đơn được tính, không lưu lặp ở header.

### 4.3. 2NF

Các quan hệ có khóa đơn và chỉ FD như bảng trên không có phụ thuộc bộ phận vào khóa dự bị. Các quan hệ điện thoại và tái bản chỉ gồm thuộc tính khóa nên không có thuộc tính không khóa vi phạm 2NF.

Trong BANHANG và NHAPHANG, số lượng và đơn giá giao dịch phụ thuộc **cả** mã hóa đơn và mã hàng. Trong CHINHANH, địa chỉ phụ thuộc cặp mã nhà cung cấp và tên chi nhánh. Không có phụ thuộc bộ phận theo ngữ nghĩa đã chọn.

### 4.4. 3NF

Điều kiện tổng quát: với mỗi FD không tầm thường X → A trong F+, X là siêu khóa hoặc A là thuộc tính khóa. Với tập FD của thiết kế này, mọi FD không tầm thường cơ bản có vế trái là khóa; các quan hệ hai thuộc tính đa trị không có FD không tầm thường. Vì vậy các quan hệ đạt ít nhất 3NF theo các giả định đã nêu.

Không chép “có khóa chính nên tự động 3NF”. Phải kiểm tra có phụ thuộc bộ phận / bắc cầu nào do nghiệp vụ không. Ví dụ nếu LAM giữ thêm TenChu và mỗi cửa hàng một chủ, xuất hiện MaNV → TenCH → TenChu. Bản này chuyển TenChu sang CUAHANG để tránh sự lặp đó.

### 4.5. Minh họa phân rã hóa đơn

Giả sử gộp `R(MaHDB,NgayBan,MaKH,MaNV,MaHH,SoLuong,DonGiaBan)` với:

```text
MaHDB → NgayBan, MaKH, MaNV
(MaHDB, MaHH) → SoLuong, DonGiaBan
Khóa của R: (MaHDB, MaHH)
```

R vi phạm 2NF vì thông tin chung phụ thuộc vào một phần khóa. Phân rã:

```text
HOADONBAN(MaHDB, NgayBan, MaKH, MaNV)
BANHANG(MaHDB, MaHH, SoLuong, DonGiaBan)
```

Giao hai quan hệ là MaHDB, xác định toàn bộ HOADONBAN, nên phép phân rã này nối không mất mát. Mỗi FD nêu trên kiểm tra được trong một quan hệ thành phần nên bảo toàn các FD đó. Phân rã hóa đơn nhập tương tự. Không đưa TenKH, TenNV, TenHH vào hóa đơn lưu trữ vì chúng được tra qua FK.

## 5. Cài đặt và giao diện

- Java 21, máy chủ HTTP có sẵn của JDK, JDBC; H2 2.5.252 là hệ quản trị CSDL lưu file. HTML/CSS/JavaScript phụ trách trình duyệt.
- Khi triển khai trên Render, dùng PostgreSQL ngoài máy chủ qua biến DATABASE_URL. Vẫn giữ 20 bảng và các ràng buộc đã mô tả; truy vấn hiển thị nhiều số điện thoại dùng STRING_AGG thay LISTAGG. Bản H2 tại máy vẫn là lựa chọn demo độc lập khi không có biến DATABASE_URL.
- SQL DDL và dữ liệu: `sql/schema.sql`, `sql/data.sql`. Câu lệnh cho các form / tìm / báo cáo: `sql/queries.sql`. Truy vấn còn lại trong ảnh: `sql/tham-khao-7-cau.sql`.
- Giao diện nhập: hàng hóa (cha/con), khách hàng (kèm nhiều điện thoại), hóa đơn bán, hóa đơn nhập.
- Tìm kiếm dùng LEFT JOIN để hàng sách và dụng cụ đều xuất hiện; PreparedStatement truyền giá trị, escape ký tự đặc biệt của LIKE.
- 4 báo cáo: doanh thu, tồn kho, khách mua nhiều nhất và hàng bán nhiều nhất theo khu vực. Hai báo cáo sau chuyển ý tưởng câu 2 / câu 7 trong ảnh sang schema đang dùng; cho chọn ngày thay vì cố định năm 2015.
- Xuất CSV thực sự từ các dòng đang thấy; in / lưu PDF dùng hộp thoại in của trình duyệt, người dùng chọn máy in hoặc Save as PDF.

### Giao dịch bán hàng

```text
BEGIN (setAutoCommit(false))
1. Kiểm tra nhân viên bán và dữ liệu form.
2. INSERT HOADONBAN.
3. Với từng mặt hàng: khóa dòng HANGHOA (FOR UPDATE), lấy giá hiện tại.
4. UPDATE tồn = tồn - số lượng, có điều kiện tồn >= số lượng.
5. Nếu đủ hàng, INSERT BANHANG với DonGiaBan chụp tại thời điểm đó.
6. Tất cả dòng hợp lệ: COMMIT. Bất kỳ lỗi nào: ROLLBACK.
```

Các mặt hàng được khóa theo thứ tự mã để giảm nguy cơ deadlock. Hóa đơn nhập có FK tới cặp (MaCC,TenChiNhanh); giá nhập riêng không tự đổi giá bán. Xử lý sai dữ liệu không để lại header rỗng do rollback. SQL không được ghép từ từ khóa người dùng.

## 6. Đối chiếu yêu cầu nộp

Yêu cầu mô tả / ERD / Data Diagram / chuẩn hóa cần xuất hiện trong báo cáo chính của **cả nhóm**, nhất quán với schema. Hai form, tìm kiếm và hai báo cáo có trong web. Nhóm cần chèn hình giao diện do mình chạy, kết quả truy vấn của dữ liệu mình dùng và diễn giải SQL; không dùng kết quả cũ trong ảnh như bằng chứng chương trình mới.

H2 là lựa chọn cho bản triển khai này, chưa có thông báo cô bắt buộc hệ khác trong nội dung được cung cấp. Nếu có quy định bổ sung, phải đối chiếu và chuyển đúng hệ. Đây là bản demo học phần trên máy cục bộ, chưa thiết kế để cung cấp dịch vụ cho cửa hàng thực tế qua Internet.
