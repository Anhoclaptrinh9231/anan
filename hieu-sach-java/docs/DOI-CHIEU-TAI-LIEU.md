# Đối chiếu với nguồn bạn cung cấp

Tài liệu lớp là căn cứ về kiến thức. 23 ảnh là **bài mẫu trên mạng**, không phải mô hình của nhóm đã được giảng viên duyệt. Thông báo mới của cô do bạn trích dẫn là căn cứ về sản phẩm phải nộp và lịch; không lấy lịch / tỷ trọng điểm năm 2014 trong giáo trình làm lịch hiện tại.

## Nguồn đã đọc

1. 23 ảnh báo cáo: yêu cầu dữ liệu, ERD, lược đồ quan hệ, chuẩn hóa, 20 bảng Access và 7 truy vấn mẫu.
2. **Bài giảng_DBMS_v1.1.pdf**, Nguyễn Quỳnh Chi: [link bạn gửi](https://drive.google.com/file/d/1QLRgrB_IKel7wcjRvIyWXd5SOGsOtOGR/view). Đã đối chiếu phần E-R / ánh xạ quan hệ và chương phụ thuộc hàm / chuẩn hóa. PDF có 178 trang; trang in 157–160 (trang PDF 158–161) bàn về chuẩn hóa, 1NF, 2NF, 3NF. Một số dấu tiếng Việt lỗi mã hóa khi trích xuất, nên không sao chép nguyên văn vào báo cáo nhóm.
3. **CSDL Slides_TV.7z**: [link bạn gửi](https://drive.google.com/file/d/1TB7SOXJYUdTGvcCpAN-HGrkGab7x1mw8/view). Đã giải nén 12 file `.ppt` và trích phần văn bản để đối chiếu; không tuyên bố đã kiểm tra trực quan mọi sơ đồ trên từng trang slide.
4. **Fundamentals of Database Systems, 7th Edition (2015)**: [link bạn gửi](https://drive.google.com/file/d/1C8CSJGsXjhTS25f8UR68vTjCv7v-vy7Y/view). Đã tải bản gốc làm nguồn tham khảo bổ sung; không dùng làm lý do thay đổi yêu cầu cô giao. Việc đối chiếu triển khai chủ yếu dựa vào ảnh, giáo trình tiếng Việt và bộ slide lớp.

Bản nguồn được giữ ở thư mục `tai-lieu-tham-khao` bên cạnh dự án. Bản này không tự phát hành lại tài liệu lớp lên website.

## Ảnh → phần triển khai

| Ảnh | Nội dung nguồn | Cách triển khai |
|---|---|---|
| 1 | Cửa hàng sách và văn phòng phẩm; nhân viên; nhập/bán; khách; nhà cung cấp | Giữ toàn bộ các nhóm đối tượng |
| 2 | ERD có HANGHOA chuyên biệt SACH / DUNGCU | Giữ bảng cha và hai bảng con; form chọn loại hàng |
| 3–7 | Lược đồ quan hệ, số điện thoại đa trị, tái bản, chuẩn hóa | 20 bảng; bảng điện thoại riêng; SACH_TB riêng; giải thích phụ thuộc hàm trong báo cáo thiết kế |
| 8–14 | Danh mục chủ, cửa hàng, nhân viên, hàng hóa, sách, khách | Giữ tên bảng gần bài mẫu; dữ liệu demo có một số tên hàng lấy từ ảnh |
| 15–19 | BANHANG, HOADONBAN, NHACUNGCAP, CHINHANH, NHAPHANG, HOADONNHAP | Form hóa đơn bán / nhập nhiều dòng; liên kết nhà cung cấp với chi nhánh bằng khóa ghép |
| 20 | Câu 2: khách ở Hà Đông mua nhiều nhất từ một mốc thời gian | Báo cáo Khách mua nhiều nhất, cho chọn khu vực và khoảng ngày, giữ tất cả trường hợp đồng hạng |
| 22–23 | Câu 7: hàng được khách Hà Đông mua nhiều nhất | Báo cáo Hàng bán nhiều nhất, có khu vực / khoảng ngày |
| 19–22 | Câu 1, 3, 4, 5, 6 | Giữ trong tài liệu truy vấn tham khảo bổ sung; không tuyên bố có cả 7 màn hình báo cáo trên web |

## Slide → kiến thức thể hiện trong bài

| Slide lớp | Kiến thức dùng | Bằng chứng trong dự án |
|---|---|---|
| `slide 3 - Luoc do E-R.ppt`, `slide 4 - Luoc do E-R (t).ppt` | Thực thể, khóa, liên kết, lực lượng, thực thể yếu, thuộc tính đa trị, ISA | ERD trong báo cáo; CHINHANH có khóa gồm nhà cung cấp + tên chi nhánh; SACH/DUNGCU kế thừa mã HANGHOA |
| `slide 5 - Luoc do quan he.ppt` | Ánh xạ E-R sang quan hệ; toàn vẹn tham chiếu | 20 CREATE TABLE với PK/FK; các bảng SDT và SACH_TB |
| `slide 6 - phu thuoc ham.ppt` | Phụ thuộc hàm xuất phát từ ngữ nghĩa; khóa và bao đóng | Bảng F và khóa trong báo cáo; không suy diễn khóa chỉ từ vài dòng dữ liệu mẫu |
| `slide 7 - chuan hoa.ppt` | Kiểm tra 1NF, phụ thuộc đầy đủ ở 2NF, 3NF | Ví dụ phân rã dòng hóa đơn; phân tích mỗi bảng dưới các giả định đã nêu |
| `slide 8 - dai so quan he.ppt` | Chọn, chiếu, kết nối | Tìm kiếm là chọn / chiếu trên kết nối HANGHOA với SACH/DUNGCU |
| `slide 9.ppt` | SQL, định nghĩa dữ liệu | `sql/schema.sql`, kiểu dữ liệu, PRIMARY KEY, FOREIGN KEY, CHECK |
| `slide 10.ppt` | DML, SELECT, INSERT, UPDATE, COMMIT/ROLLBACK | Form nhập; transaction hóa đơn; tự hủy khi thiếu tồn kho |
| `slide 11.ppt` | ORDER BY, hàm tổng hợp, GROUP BY, HAVING, truy vấn con | SUM / COUNT báo cáo; MAX và truy vấn con để lấy tất cả đồng hạng; HAVING trong truy vấn bổ sung câu 3 |
| `slide 12.ppt` | JOIN | JOIN nhiều bảng cho hóa đơn / báo cáo; LEFT JOIN khi tìm sách và văn phòng phẩm |

`WITH T AS (...)` trong hai báo cáo chỉ đặt tên cho một kết quả trung gian để dễ đọc. Phần lý thuyết cần giải thích vẫn là JOIN, WHERE, GROUP BY, SUM và truy vấn con MAX; không cần trình bày WITH như một nội dung cô bắt buộc dạy.

## Các chỗ điều chỉnh, không sao chép lỗi từ ảnh

| Trong bài mẫu | Trong bản này | Lý do |
|---|---|---|
| Một chỗ viết MaHDB → MaHH, nhưng bảng mẫu có nhiều hàng trong một hóa đơn | HOADONBAN giữ ngày / khách / nhân viên; BANHANG dùng PK (MaHDB,MaHH) | Mã hóa đơn không xác định duy nhất một mặt hàng; tách phần chung khỏi từng dòng hàng |
| Có hóa đơn trong ảnh gắn nhiều khách / nhân viên ở các dòng | Một hóa đơn thuộc một khách và một nhân viên | Quy tắc nghiệp vụ của bản triển khai; nhóm phải ghi rõ trong báo cáo |
| MaNH lặp ở nhiều hàng nhưng có đoạn nói MaNH là khóa | Chi tiết nhập dùng PK (MaHDN,MaHH); phần chung ở HOADONNHAP | Một lần nhập có nhiều mặt hàng; không coi mã nhóm nhập là khóa duy nhất của dòng nhập |
| LAM gồm MaNV,TenCH,TenChu | CUAHANG giữ TenChu; LAM giữ MaNV,TenCH | Với TenCH → TenChu, tránh phụ thuộc bắc cầu MaNV → TenCH → TenChu trong LAM |
| Khẳng định SDT → người sở hữu | Bảng điện thoại dùng khóa ghép (mã chủ thể,SDT) | Không giả định một số điện thoại luôn thuộc độc quyền một người |
| Lưu Tuoi | Lưu NgaySinh | Tuổi thay đổi theo thời gian; có thể tính khi cần |
| Tính tiền theo Gia hiện tại hoặc nhân hệ số 1,3 / 0,3 không giải thích | BANHANG lưu DonGiaBan; doanh thu = SUM(SoLuong×DonGiaBan) | Đổi giá sau này không đổi hóa đơn cũ. Không đánh đồng doanh thu với lãi ước tính |
| Dữ liệu bán năm 2014–2015 | Dữ liệu demo tháng 10/2026, bộ lọc ngày cho người dùng chọn | Dễ demo hiện tại; số liệu / kết quả không được ghi là khớp nguyên ảnh |

Giữ đúng 20 bảng là lựa chọn để dễ đối chiếu bài mẫu, không phải số bảng tối thiểu cô yêu cầu. Tên chủ và tên cửa hàng được dùng làm khóa tự nhiên theo mẫu trong phạm vi bài; nếu triển khai thực tế nên dùng mã riêng để cho phép trùng tên và đổi tên thuận tiện.

## Các việc cần nhóm thống nhất

- Dùng ERD, lược đồ và SQL của **cùng bản triển khai** trong báo cáo. Không ghép ảnh ERD cũ vào rồi trình bày rằng đó là chính xác CSDL đang chạy.
- Mô tả dữ liệu, ERD, Data Diagram và chuẩn hóa là phần cả nhóm cần hiểu. Website không tự thay thế các phần đó.
- Cô yêu cầu tối thiểu 2 báo cáo, nên web hiện có 4 báo cáo là đủ về số lượng theo thông báo đã gửi; điều này không bảo đảm điểm số hoặc thay cho duyệt bài của cô.
