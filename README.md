# Bắt đầu với website hiệu sách

Web công khai: https://hieu-sach-tri-thuc.onrender.com

Bản hiện tại dùng 11 bảng theo mô tả và ERD của nhóm ngày 29/10/2026. Đọc [đối chiếu bản mới](hieu-sach-java/docs/CAP-NHAT-MO-HINH-11-BANG.md).

## Chạy trên máy

1. Cài JDK 21 hoặc mới hơn.
2. Giải nén toàn bộ dự án, nhấp CHAY-WEB.bat.
3. Mở http://localhost:8080 và giữ chương trình Java đang chạy.

H2 lưu vào data/hieusach.mv.db, schema BOOKSTORE_V2. Dừng Java trước khi mở MO-CSDL.bat. Khi xem bảng bằng H2 Console, chọn schema BOOKSTORE_V2 hoặc chạy SET SCHEMA BOOKSTORE_V2.

## Cách dùng

- Sách: thêm/sửa tên, ISBN, tác giả, nhà xuất bản, năm, thể loại và giá. Ngừng bán giữ lịch sử; sửa trạng thái để bán lại.
- Danh mục: thêm/sửa chi nhánh hiệu sách, nhân viên, thể loại và nhà cung cấp.
- Nhập hàng: chọn chi nhánh nhận, nhà cung cấp, nhân viên, **ngày giờ nhập**, sách, số lượng và giá nhập. Xác nhận mới tăng tồn. Nút Xóa hoàn tác tồn và chuyển phiếu Đã hủy; Khôi phục đưa về trạng thái chờ.
- Bán hàng: chọn chi nhánh, nhân viên, khách hoặc Khách lẻ, **ngày giờ bán**, thanh toán và sách. Giá thực tế được lưu vào hóa đơn; Hoàn tất mới trừ tồn và tính doanh thu.
- Tổng quan: chọn năm/chi nhánh để xem doanh thu, số cuốn bán, hóa đơn từng tháng. Bấm cột để xem chi tiết; mở bảng để xem cả 12 tháng.
- Tìm kiếm: từ khóa, ISBN, giá, thể loại và tồn tại chi nhánh.
- Báo cáo: hai màn hình đúng mô tả; CSV, in/lưu PDF và SQL tương ứng.

## Trợ lý Tri Thức và phân tích tháng

- Bấm nút tròn ở góc dưới bên phải để hỏi về tên sách, giá, tồn theo chi nhánh, địa chỉ chi nhánh hoặc cách sử dụng website. Thử hỏi “Mắt Biếc còn bao nhiêu cuốn?” rồi hỏi tiếp “Còn ở Cầu Giấy thì sao?”.
- Trong **Báo cáo → Phân tích theo tháng**, chọn tháng và chi nhánh rồi bấm **Tạo phân tích**. Thử chọn **10/2026** để dùng giao dịch mẫu. Bảng đối chiếu doanh thu, số sách bán, số hóa đơn hoàn tất và chi phí nhập với ba tháng trước. Tháng đang diễn ra được so với cùng số ngày đầu của các tháng trước, tối đa đến cuối từng tháng.
- Khi chưa có khóa hoặc DeepSeek gặp lỗi, website hiển thị tra cứu cơ bản và tóm tắt theo số liệu trong CSDL.

Để bật DeepSeek trên máy, mở PowerShell trong thư mục dự án và chạy:

```powershell
$env:DEEPSEEK_API_KEY = [System.Net.NetworkCredential]::new('', (Read-Host 'Khóa DeepSeek' -AsSecureString)).Password
.\hieu-sach-java\CHAY-WEB.bat
```

Nếu dùng Render, thêm biến môi trường `DEEPSEEK_API_KEY` vào dịch vụ web rồi khởi động lại dịch vụ. Khóa nằm trên máy chủ Java. Website dùng `deepseek-flash` mặc định và có thể đổi bằng biến `DEEPSEEK_MODEL`. Mỗi máy chủ giới hạn tổng cộng 20 lượt gọi DeepSeek mỗi phút cho chat và báo cáo. [Tài liệu API DeepSeek](https://api-docs.deepseek.com/api/create-chat-completion/)

## Dữ liệu minh họa và nộp bài

Dữ liệu khởi tạo theo tài liệu nhóm: 4 đầu sách, 2 chi nhánh, 3 hóa đơn bán, 2 phiếu nhập; giao dịch mẫu nằm trong tháng 10/2026. Các tháng còn lại có thể bằng 0. Doanh thu mẫu 2.235.000 đ, bán 18 cuốn.

File sql/schema.sql là DDL 11 bảng; sql/data.sql là dữ liệu mẫu; sql/queries.sql chứa SQL form, tìm kiếm, báo cáo và biểu đồ. Tài liệu lớp dùng SQL Server, bản công khai chạy PostgreSQL và bản tại máy chạy H2. Ghi rõ hệ quản trị thực tế trong báo cáo.

KIEM-TRA.bat chạy các kiểm tra nghiệp vụ trên dữ liệu tách biệt. Website công khai cho phép mọi người dùng chung dữ liệu minh họa. Gói Render Free có thể khởi động chậm khi không truy cập; PostgreSQL Free của lần triển khai hiện tại chỉ dùng thử 30 ngày.
