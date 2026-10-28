# Đưa web Java lên mạng

Mục tiêu: ai có link đều mở được và thử nhập dữ liệu, kể cả khi máy cá nhân đã tắt. Chương trình vẫn xử lý bằng Java và CSDL quan hệ gồm 20 bảng.

## GitHub và Render làm gì?

GitHub lưu mã nguồn. GitHub Pages chỉ phục vụ HTML/CSS/JavaScript tĩnh, không chạy máy chủ Java / JDBC này. Bản công khai chạy Java trong Docker trên Render; Render cấp địa chỉ HTTPS dạng `...onrender.com`. Không cần mua tên miền để truy cập địa chỉ đó.

Java tự dùng PostgreSQL khi có biến môi trường `DATABASE_URL`; khi chạy tại máy mà không có biến này, chương trình vẫn dùng file H2 như bản ban đầu. Cấu trúc dữ liệu và nghiệp vụ giữ nguyên. Chỗ nối nhiều số điện thoại dùng STRING_AGG trên PostgreSQL thay cho LISTAGG của H2.

## Cấu hình đã chuẩn bị

- `render.yaml` ở gốc kho mã: Java Web Service gói Free và PostgreSQL gói Free cùng vùng Singapore.
- `hieu-sach-java/Dockerfile`: biên dịch và chạy Java 21. Thư viện H2/PostgreSQL JDBC được tải từ Maven Central đúng phiên bản và kiểm tra SHA-256 lúc build, không cần commit file nhị phân hoặc CSDL lên GitHub.
- Cổng HTTP lấy từ biến `PORT`, lắng nghe `0.0.0.0` trong Docker. Bản tại máy mặc định chỉ lắng nghe `127.0.0.1`.
- `/api/health`: đường dẫn Render dùng để kiểm tra chương trình hoạt động.
- `DATABASE_URL`: bí mật kết nối chỉ đặt trong Render, không lưu vào Git hoặc hiển thị trên giao diện.
- `BOOKSTORE_PUBLIC=true`: bản máy chủ yêu cầu PostgreSQL, tránh vô tình lưu dữ liệu vào file tạm của dịch vụ miễn phí.
- Nạp dữ liệu mẫu khi CSDL mới trống; khởi động lại không nạp đè dữ liệu cũ.

## Giới hạn miễn phí cần hiểu

Theo tài liệu Render kiểm tra ngày 28/10/2026:

- Web Service Free nghỉ sau 15 phút không có lượt truy cập; lần mở tiếp có thể cần khoảng một phút để khởi động.
- File trên máy chủ Free có thể mất khi khởi động / triển khai lại, nên không dùng file H2 để lưu dữ liệu công khai.
- **PostgreSQL Free của Render hết hạn sau 30 ngày**; không phù hợp để coi là CSDL miễn phí vĩnh viễn. Đủ thời gian cho lịch báo cáo 04/11 và 11/11 nếu triển khai cuối tháng 10, nhưng cần sao lưu / chuyển CSDL trước khi hết hạn.
- Không tự chuyển sang gói trả phí. Nếu xuất hiện yêu cầu thanh toán hoặc nâng cấp, người dùng cần tự quyết định.

Nếu cần giữ lâu hơn, có thể dùng PostgreSQL bên ngoài, ví dụ Neon Free. Khi tạo Blueprint dùng `hieu-sach-java/render-external-db.yaml` và cung cấp `DATABASE_URL` của CSDL đó trong ô bí mật của Render. Đây là tùy chọn cấu hình; chưa tự tạo tài khoản Neon hay khẳng định đã triển khai theo tùy chọn này.

Bản công khai cho mọi người thử nhập cùng một bộ dữ liệu dùng chung theo lựa chọn của chủ dự án. Chỉ dùng dữ liệu minh họa khi demo, vì người khác có link cũng xem được dữ liệu đã nhập. Bản chạy trên máy và CSDL công khai độc lập; dữ liệu nhập trên localhost không tự được chuyển lên máy chủ.

## Khi tải mã nguồn từ GitHub về máy

Bản ZIP bàn giao có sẵn thư viện. Kho GitHub không chứa file JAR; nếu clone về để chạy bằng `.bat`, tải hai JAR đúng phiên bản ghi trong `Dockerfile` vào `lib/` trước. Dữ liệu trong `data/`, CSDL thử trong `test-output/` và tài liệu lớp gốc không được đẩy lên GitHub.

## Nguồn

- [GitHub Pages là hosting tĩnh](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages)
- [Giới hạn Free của Render](https://render.com/docs/free)
- [Chạy Java bằng Docker trên Render](https://render.com/docs/docker)
- [Gói miễn phí Neon](https://neon.com/pricing)
