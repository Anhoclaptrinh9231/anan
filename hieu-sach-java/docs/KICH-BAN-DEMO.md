# Kịch bản 15 phút cho cả nhóm

| Thời lượng | Người phụ trách | Nội dung |
|---|---|---|
| 0:00–2:00 | Phân tích yêu cầu | Chủ đề cửa hàng sách và văn phòng phẩm; các dữ liệu cần quản lý; quy tắc mỗi hóa đơn một khách / một nhân viên |
| 2:00–5:00 | Thành viên CSDL | ERD, chuyển sang 20 bảng, khóa ghép dòng hàng, thuộc tính đa trị |
| 5:00–7:00 | Thành viên chuẩn hóa | FDs của hóa đơn, lỗi phụ thuộc bộ phận khi trộn header và detail; phân rã; PK/FK/CHECK |
| 7:00–12:30 | Bạn thao tác web | Hai form nhập → tìm sách → lập hóa đơn → xem tồn → hai báo cáo và SQL |
| 12:30–14:00 | Thành viên truy vấn | Giải thích JOIN, WHERE, GROUP BY, SUM, MAX, đồng hạng; mở câu 2 hoặc câu 7 |
| 14:00–15:00 | Cả nhóm | Dự phòng câu hỏi và chuyển màn hình |

## Lời giải thích khi bạn demo

“Đây là giao diện web của hệ CSDL quản lý hiệu sách và văn phòng phẩm. Phần xử lý viết bằng Java, kết nối H2 qua JDBC. Dữ liệu lưu trong CSDL trên ổ đĩa.”

“Em nhập sách mới. Thông tin chung như mã, tên, giá, số lượng vào HANGHOA; tác giả và thể loại vào SACH. Cả hai thao tác được lưu cùng một giao dịch.”

“Đây là form khách hàng thứ hai. Nếu khách có nhiều số điện thoại, mỗi số là một dòng ở KHACHHANG_SDT.”

“Em tìm sách bằng mã hoặc tác giả. Java dùng PreparedStatement truyền từ khóa vào câu SQL, rồi trả kết quả cho trình duyệt.”

“Một hóa đơn có nhiều mặt hàng. Phần ngày, nhân viên, khách nằm trong HOADONBAN; từng dòng nằm ở BANHANG với khóa ghép mã hóa đơn và mã hàng. Đơn giá được lưu tại thời điểm bán.”

“Khi lưu hóa đơn, hệ thống kiểm tra tồn kho. Nếu một dòng thiếu hàng, toàn bộ hóa đơn và các lần trừ kho trước đó đều được rollback.”

“Đây là báo cáo doanh thu theo ngày, tính SUM của số lượng nhân đơn giá bán. Đây là báo cáo tồn kho. Có thể xuất CSV hoặc in / lưu PDF; phần SQL tương ứng hiển thị bên dưới.”

## Câu cô có thể hỏi

**Java ở đâu, sao vẫn có JavaScript?** Java chạy máy chủ ở `src/Bookstore.java`; HTML/CSS/JavaScript là giao diện trình duyệt. JavaScript không thay thế Java xử lý CSDL.

**Khóa chính của BANHANG là gì?** (MaHDB,MaHH). Mỗi mặt hàng chỉ xuất hiện một lần trong một hóa đơn; mua nhiều thì tăng số lượng.

**Sao tách HOADONBAN và BANHANG?** Ngày bán, khách, nhân viên chỉ phụ thuộc MaHDB; số lượng và giá bán của dòng phụ thuộc cả MaHDB,MaHH. Nếu gộp sẽ lặp thông tin chung và có phụ thuộc bộ phận.

**Giá hàng đổi có làm sai doanh thu cũ không?** Không. Doanh thu dùng DonGiaBan trong BANHANG, không JOIN giá hiện tại để tính lại hóa đơn cũ.

**Sao không dùng SDT làm khóa khách hàng?** Có thể chung số liên hệ, một khách có nhiều số, hoặc đổi số; dùng MaKH làm định danh khách.

**Một nhà cung cấp có hai chi nhánh cùng tên với nhà cung cấp khác thì sao?** CHINHANH có khóa ghép (MaCC,TenChiNhanh); hóa đơn nhập tham chiếu đúng cả cặp.

**Có nhất thiết 20 bảng không?** Không. Đây là thiết kế giữ gần bài mẫu đã chọn; yêu cầu cô gửi không ấn định số bảng. Các bảng SDT và tái bản xuất hiện do thuộc tính đa trị.

**Hai khách mua bằng nhau có lấy đủ không?** Có. Truy vấn so với MAX, không chỉ lấy một dòng đầu tiên.

**Dữ liệu tồn kho lấy ở đâu?** Với dữ liệu đầu kỳ đã nạp, tồn hiện tại = tồn đầu + nhập - bán. Số lượng đang được lưu để demo, mọi hóa đơn cập nhật số lượng trong giao dịch. Chưa quản lý lô hàng / hoàn trả.

## Chuẩn bị trước buổi báo cáo

Mở web trước khi trình bày; kiểm tra chọn đúng khoảng ngày. Giữ một bản sao cả thư mục khi đã dừng Java. Chuẩn bị ảnh hai form, tìm kiếm và hai báo cáo làm dự phòng, nhưng demo trực tiếp nếu máy hoạt động bình thường. Đừng dành quá nhiều thời gian giới thiệu màu sắc giao diện; ưu tiên dữ liệu, quan hệ, ràng buộc và SQL.
