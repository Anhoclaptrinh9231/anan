# Bắt đầu với website hiệu sách

Bản hiện tại dùng 11 bảng theo mô tả và ERD của nhóm ngày 29/10/2026. Đọc [đối chiếu bản mới](docs/CAP-NHAT-MO-HINH-11-BANG.md).

## Mở web trên máy đã thiết lập

Nhấp đúp vào **CHAY-WEB.bat**, đợi cửa sổ hiện địa chỉ rồi mở [website trên máy](http://localhost:8080). Giữ cửa sổ mở trong lúc dùng web. Nhấn Ctrl+C để dừng web.

Nhấp đúp vào **MO-CSDL.bat** để mở MySQL trên máy. Gõ `SHOW TABLES;` và nhấn Enter để xem 11 bảng và 2 view. Gõ `exit` để đóng.

## Thiết lập lần đầu trên máy Windows khác

1. Cài JDK 21 trở lên. Mở Command Prompt và chạy `javac -version` để kiểm tra.
2. Cài MySQL Community Server 8.4 LTS, chọn gói **Windows (x86, 64-bit), MSI Installer**. Trong MySQL Configurator, giữ cổng `3306`, đặt mật khẩu cho tài khoản quản trị `root` và bật chạy dưới dạng Windows Service. [Trang tải MySQL](https://dev.mysql.com/downloads/mysql/8.4.html?os=3)
3. Mở **MySQL 8.4 Command Line Client** từ Start và nhập mật khẩu `root`. Đổi `DAT_MAT_KHAU_RIENG` thành mật khẩu dành cho website, rồi chạy các lệnh sau.

   ```sql
   CREATE DATABASE IF NOT EXISTS bookstore_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin;
   CREATE USER 'hieusach'@'localhost' IDENTIFIED BY 'DAT_MAT_KHAU_RIENG';
   GRANT ALL PRIVILEGES ON bookstore_v2.* TO 'hieusach'@'localhost';
   GRANT ALL PRIVILEGES ON bookstore_test.* TO 'hieusach'@'localhost';
   ```

4. Trong thư mục chứa CHAY-WEB.bat, mở PowerShell và sao chép mẫu cấu hình.

   ```powershell
   New-Item -ItemType Directory -Path data -Force | Out-Null
   Copy-Item mysql.properties.example data/mysql.properties
   notepad data/mysql.properties
   ```

5. Điền mật khẩu của `hieusach` sau `BOOKSTORE_DB_PASSWORD=` và lưu. Nếu mật khẩu có dấu gạch chéo ngược `\`, viết mỗi dấu thành hai dấu trong tệp này.
6. Nhấp đúp vào **CHAY-WEB.bat**. Lần đầu cần Internet để tải hai thư viện Java từ Maven Central. Web tự tạo bảng và nạp dữ liệu mẫu khi cơ sở dữ liệu trống.

Tên, cổng và mật khẩu tùy theo MySQL trên máy. Khi dùng biến môi trường `BOOKSTORE_DB_URL`, `BOOKSTORE_DB_USER` hoặc `BOOKSTORE_DB_PASSWORD`, biến đó được ưu tiên hơn tệp cấu hình. Tệp `data/mysql.properties` chứa mật khẩu và nằm ngoài gói ZIP.

## Cách dùng

- Sách: thêm/sửa tên, ISBN, tác giả, nhà xuất bản, năm, thể loại và giá. Xóa sách khỏi danh mục; chứng từ đã phát sinh vẫn được giữ.
- Khách hàng: sửa được cả mã khách; có nút Xóa. Hóa đơn cũ giữ số tiền và chuyển về khách lẻ khi xóa hồ sơ.
- Danh mục: thêm/sửa chi nhánh hiệu sách, nhân viên, thể loại và nhà cung cấp.
- Nhập hàng: chọn chi nhánh nhận, nhà cung cấp, nhân viên, **ngày giờ nhập**, sách, số lượng và giá nhập. Xác nhận mới tăng tồn. Nút Xóa gỡ phiếu và chi tiết khỏi danh sách, đồng thời hoàn tác tồn kho đã cộng.
- Bán hàng: chọn chi nhánh, nhân viên, khách hoặc Khách lẻ, **ngày giờ bán**, thanh toán và sách. Giá thực tế được lưu vào hóa đơn; Hoàn tất mới trừ tồn và tính doanh thu.
- Tổng quan: chọn năm/chi nhánh để xem doanh thu, số cuốn bán, hóa đơn từng tháng. Bấm cột để xem chi tiết; mở bảng để xem cả 12 tháng.
- Tìm kiếm: từ khóa, ISBN, giá, thể loại và tồn tại chi nhánh.
- Báo cáo: hai màn hình đúng mô tả; CSV, in/lưu PDF và SQL tương ứng.

## Dữ liệu minh họa và nộp bài

Dữ liệu khởi tạo theo tài liệu nhóm: 4 đầu sách, 2 chi nhánh, 3 hóa đơn bán, 2 phiếu nhập; giao dịch mẫu nằm trong tháng 10/2026. Các tháng còn lại có thể bằng 0. Doanh thu mẫu 2.235.000 đ, bán 18 cuốn.

File sql/schema.sql định nghĩa 11 bảng MySQL. File sql/data.sql chứa dữ liệu mẫu. File sql/queries.sql chứa truy vấn cho form, tìm kiếm, báo cáo và biểu đồ.

KIEM-TRA.bat dùng cơ sở dữ liệu MySQL riêng có tên `bookstore_test`. Chương trình tự tạo cơ sở dữ liệu này khi chạy kiểm tra rồi dọn sau mỗi bộ kiểm tra.
