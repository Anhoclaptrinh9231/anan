# Đưa web Java lên mạng

Render chạy máy chủ Java trong Docker và cấp địa chỉ HTTPS. Dữ liệu được lưu trên một máy chủ MySQL 8.4 có thể truy cập từ Render.

## Thiết lập

1. Tạo cơ sở dữ liệu và tài khoản ứng dụng trên máy chủ MySQL. Tài khoản cần quyền tạo bảng, tạo view và đọc ghi trong cơ sở dữ liệu của hiệu sách. Nếu cần giữ dữ liệu của bản công khai trước, hãy nhập dữ liệu đã chuyển đổi vào MySQL trước lần khởi động đầu tiên.
2. Dùng `render.yaml` ở gốc kho mã để cấu hình Web Service. Dockerfile biên dịch Java 21 và kiểm tra SHA-256 của MySQL Connector/J và Gson khi tải từ Maven Central.
3. Vào **Environment** của dịch vụ Render và đặt ba biến sau. [Tài liệu biến môi trường của Render](https://render.com/docs/configure-environment-variables)

   | Biến | Giá trị |
   | --- | --- |
   | `BOOKSTORE_DB_URL` | URL JDBC dạng `jdbc:mysql://ten-may-chu:3306/bookstore_v2` |
   | `BOOKSTORE_DB_USER` | Tài khoản ứng dụng trên máy chủ MySQL |
   | `BOOKSTORE_DB_PASSWORD` | Mật khẩu của tài khoản đó |

   Khi `BOOKSTORE_PUBLIC=true`, ứng dụng xác minh chứng chỉ và tên máy chủ MySQL. Nếu nhà cung cấp cấp CA riêng, mã hóa tệp CA dạng PEM thành Base64 và đặt vào `BOOKSTORE_DB_CA_PEM_BASE64`. Dockerfile tạo kho chứng chỉ cho JDBC trong lúc build. Thêm chuỗi sau vào cuối URL JDBC và chọn **Save, rebuild, and deploy**. [Cấu hình TLS của MySQL Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html)

   ```text
   ?trustCertificateKeyStoreUrl=file:/app/mysql-ca.p12&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit
   ```

4. Triển khai dịch vụ rồi mở đường dẫn `/api/health`. Kiểm tra kết quả có `"status":"ok"` và `"database":"MySQL"`.

Ứng dụng tạo bảng và nạp dữ liệu mẫu khi cơ sở dữ liệu trống. Dữ liệu nhập ở `localhost` và dữ liệu công khai nằm trong hai cơ sở dữ liệu riêng. Tệp `data/mysql.properties` và mật khẩu chỉ dùng cục bộ trên máy phát triển.
