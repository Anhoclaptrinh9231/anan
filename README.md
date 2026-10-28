# Quản lý hiệu sách và văn phòng phẩm Tri Thức

Website bài tập lớn Cơ sở dữ liệu, viết bằng **Java 21 + JDBC**. Thiết kế gồm 20 bảng, có ERD, Data Diagram, phân tích phụ thuộc hàm / chuẩn hóa, form nhập hàng hóa và khách hàng, hóa đơn bán / nhập, tìm kiếm và bốn báo cáo kèm SQL.

## Chạy tại máy

Đọc `hieu-sach-java/BAT-DAU-O-DAY.md`, mở `CHAY-WEB.bat` rồi truy cập `http://localhost:8080`. Bản tại máy dùng H2 lưu file. Bản ZIP có sẵn thư viện; nếu clone từ GitHub, tải hai JAR đúng phiên bản trong `Dockerfile` vào `hieu-sach-java/lib/` trước.

## Chạy trên máy chủ

`render.yaml` triển khai Web Service Java bằng Docker và PostgreSQL trên Render. Có địa chỉ HTTPS riêng để dùng khi máy cá nhân tắt. Cấu hình chi tiết và giới hạn gói miễn phí ghi trong `hieu-sach-java/docs/DUA-WEB-LEN-MANG.md`.

Lưu ý: PostgreSQL Free của Render hết hạn sau 30 ngày. Có thể dùng PostgreSQL ngoài bằng `DATABASE_URL` với cấu hình `hieu-sach-java/render-external-db.yaml` để tránh giới hạn thời gian đó. Không đưa bí mật kết nối, file CSDL hoặc dữ liệu cá nhân lên kho mã.

## Tài liệu

- `hieu-sach-java/docs/BAO-CAO-THIET-KE.md`: yêu cầu, 20 bảng, khóa và kiểm tra 3NF.
- `hieu-sach-java/docs/DOI-CHIEU-TAI-LIEU.md`: đối chiếu bài mẫu, giáo trình và slide lớp.
- `hieu-sach-java/docs/KICH-BAN-DEMO.md`: thứ tự báo cáo / demo 15 phút.
- `hieu-sach-java/web/erd.svg`, `data-diagram.svg`: sơ đồ để xem và đưa vào báo cáo.
- `hieu-sach-java/sql/`: SQL tạo bảng, dữ liệu mẫu, truy vấn và bảy câu tham khảo từ bài mẫu.

Tài liệu lớp gốc và ảnh giao diện bản tại máy không được đẩy lên kho mã.
