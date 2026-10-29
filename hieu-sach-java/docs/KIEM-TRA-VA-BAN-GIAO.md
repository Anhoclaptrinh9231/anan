> Tài liệu lưu trữ của bản 20 bảng trước đây. Bản hiện hành đã thay bằng [mô hình 11 bảng](CAP-NHAT-MO-HINH-11-BANG.md) theo mô tả mới của nhóm. Không dùng số liệu/SQL bên dưới cho bản mới.

# Kiểm tra bản bàn giao ngày 28/10/2026

## Đã kiểm tra

- Biên dịch bằng JDK 21; kiểm tra cú pháp JavaScript.
- 40 kiểm tra nghiệp vụ bằng `tests/IntegrationTest.java` trên file CSDL thử riêng: đúng 20 bảng, nạp mẫu, hai form, khóa ngoại / khóa chính, dữ liệu không hợp lệ, rollback nhiều dòng, giá lịch sử, nhập tăng / bán giảm kho, tìm kiếm tiếng Việt, tham số SQL, đồng hạng, khoảng ngày rỗng, hai yêu cầu mua món cuối đồng thời, đóng và mở lại CSDL.
- Chạy được cả 7 câu SQL chuyển từ ảnh trong `sql/tham-khao-7-cau.sql` trên H2, bao gồm câu có thể trả kết quả rỗng với dữ liệu mới.
- Thao tác trực tiếp trên giao diện ở CSDL thử riêng: thêm sách, thêm khách có hai điện thoại, tìm sách vừa thêm, lập hóa đơn hai mặt hàng (112.000đ), lập phiếu nhập có nhà cung cấp / chi nhánh.
- Bấm Xuất CSV trong trình duyệt và đọc file thực tế: 7 dòng báo cáo doanh thu, tiêu đề UTF-8 tiếng Việt, tổng khớp 2.007.060đ ở CSDL thử sau giao dịch kiểm tra. Dữ liệu bàn giao ban đầu có tổng 1.895.060đ.
- Kiểm tra bố cục laptop và điện thoại; không tràn ngang toàn trang ở kích thước điện thoại đã thử. Bảng và thanh điều hướng có thể cuộn ngang trong vùng riêng.
- ERD và Data Diagram tải đủ; Data Diagram sinh từ metadata của 20 bảng SQL, không vẽ một schema khác.
- Không ghi nhận lỗi JavaScript trong lượt thao tác trình duyệt đã kiểm tra.

## Phạm vi của xác nhận

Các kiểm tra trên xác nhận bản chạy tại máy hiện tại; chưa phải kiểm thử mọi dữ liệu có thể nhập. Nút In / lưu PDF gọi hộp thoại in của trình duyệt; cần chọn máy in hoặc Save as PDF trong trình duyệt sử dụng lúc thuyết trình. Chưa tự động xác minh một file PDF được lưu qua hộp thoại in hệ điều hành. CSV đã được xuất và kiểm tra file thực tế.

Hệ quản trị bản tại máy là H2 2.5.252. Bản dùng cho triển khai máy chủ đã được kiểm tra thêm với PostgreSQL 16: 38 kiểm tra nghiệp vụ tương đương, cùng một lượt dừng / mở lại máy chủ PostgreSQL và xác nhận hóa đơn / đơn giá lịch sử vẫn còn. Không tuyên bố đã kiểm tra SQL Server / MySQL. Chưa có đăng nhập / phân quyền, giao diện sửa/xóa toàn bộ danh mục hoặc xuất báo cáo Word tự động. Những phần này không có trong phạm vi tối thiểu cô đã nhắn ở thông báo được cung cấp.

## Ảnh giao diện

- `screenshots/01-tong-quan.jpg`
- `screenshots/02-form-hang-hoa.jpg`
- `screenshots/03-form-khach-hang.jpg`
- `screenshots/04-tim-kiem.jpg`
- `screenshots/05-bao-cao-doanh-thu.jpg`
- `screenshots/06-bao-cao-ton-kho.jpg`

Các ảnh được chụp từ bản chạy thật với dữ liệu mẫu, có thể dùng làm hình minh họa cho phần giao diện. Nhóm nên tự chụp lại khi đã chốt dữ liệu và chỉnh thông tin thành viên.
