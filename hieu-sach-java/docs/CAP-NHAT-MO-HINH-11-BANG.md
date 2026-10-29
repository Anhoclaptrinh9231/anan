# Bản cập nhật theo mô tả và ERD của nhóm — 29/10/2026

Nguồn chính: [mô tả ERD-DATA DIAGRAM](https://docs.google.com/document/d/1Nl8SI_FzWJgpHifEfb6gIRrG0407_QkaPEBrJ6d0wAg/edit) và [lược đồ nhóm](https://app.diagrams.net/#G14YQBJLRh5wd0LLUGNSIdfBej51VhAP_g).

## Mô hình đang chạy

11 bảng: CHI_NHANH, NHAN_VIEN, KHACH_HANG, THE_LOAI, SACH, NHA_CUNG_CAP, PHIEU_NHAP, CT_PHIEU_NHAP, HOA_DON_BAN, CT_HOA_DON_BAN, TON_KHO.

- CHI_NHANH là cửa hàng của hiệu sách; nhà cung cấp là một danh mục độc lập.
- TON_KHO có khóa ghép (MaCN, MaSach), không gộp tồn kho của các cửa hàng khi bán.
- Mỗi sách thuộc một thể loại. ISBN duy nhất nếu được cung cấp; giá không âm.
- Nhân viên có chi nhánh, chức vụ và trạng thái. Nhân viên thường chỉ lập đúng nghiệp vụ tại chi nhánh của mình. Quản trị viên có thể lập tại các chi nhánh, phù hợp phiếu mẫu PN20261002.
- Khách mua lẻ không cần hồ sơ: MaKH của hóa đơn là NULL.
- Ngày giờ bắt buộc trên hóa đơn bán và phiếu nhập, được lưu kiểu TIMESTAMP. Ngày trong biểu đồ và báo cáo lấy từ ngày giờ chứng từ, không lấy thời điểm mở trang.
- Giá bán thực tế và giá nhập được lưu trong từng dòng chi tiết. Tổng tiền được tính khi truy vấn; không lưu cột tổng tiền dư thừa.

## Các màn hình

Sách (thêm/sửa/ngừng bán), khách hàng, bán hàng, nhập hàng, danh mục (thể loại/chi nhánh/nhân viên/nhà cung cấp), tìm kiếm và hai báo cáo. Đã bỏ mục Cơ sở dữ liệu khỏi giao diện nghiệp vụ.

Tìm kiếm kết hợp từ khóa tên/tác giả/ISBN/mã, thể loại, trạng thái, khoảng giá và chi nhánh. Sách chưa có bản ghi tồn tại chi nhánh được hiển thị tồn 0.

Tổng quan có biểu đồ đủ 12 tháng: doanh thu, số cuốn bán và số hóa đơn, lọc theo năm và chi nhánh, chọn một tháng để xem số chính xác. Tháng không có giao dịch hiển thị 0, không tạo số liệu giả để lấp biểu đồ.

1. Doanh thu và chi phí nhập theo chi nhánh, lọc thời gian. Tổng hợp bán và nhập riêng trước khi nối để tránh nhân đôi tổng tiền. Doanh thu chưa trừ chi phí; chênh lệch thu và nhập không được gọi là lợi nhuận.
2. Sách sắp hết tại từng chi nhánh và sách bán từ mức trung bình trở lên. Phần bán chạy lấy trung bình **tổng số cuốn của mỗi đầu sách đã bán** trong cùng kỳ và chi nhánh, thay cho trung bình số lượng từng dòng ở SQL mẫu.

Cả hai báo cáo có CSV, in/lưu PDF bằng trình duyệt và SQL tương ứng.

## Trạng thái và tồn kho

- Bán: Chờ xử lý chưa trừ tồn; Hoàn tất trừ tồn và tính doanh thu; hủy hóa đơn hoàn tất hoàn lại sách.
- Nhập: Chờ xác nhận chưa cộng tồn; Đã xác nhận cộng tồn và tính chi phí nhập.
- Nút **Xóa** phiếu nhập thực hiện xóa mềm: chuyển Đã hủy và hoàn tác lượng tồn đã nhập. Nếu tồn không đủ, toàn bộ thao tác bị rollback. Chi tiết và ngày giờ vẫn được giữ; nút Khôi phục đưa phiếu về Chờ xác nhận.
- Ngừng bán sách giữ lịch sử. Sửa trạng thái về Đang kinh doanh để bán lại.
- Khóa bản ghi và giao dịch JDBC bảo đảm toàn bộ chứng từ thành công hoặc không thay đổi gì. Sách trong chứng từ được khóa theo cùng thứ tự mã.

## Điểm cần thống nhất trong tài liệu nhóm

- Quy tắc nghiệp vụ nói ISBN có thể không cung cấp, trong khi DDL mẫu ghi NOT NULL. Bản chạy dùng NULL + UNIQUE theo quy tắc nghiệp vụ.
- Phần thực thể và ERD có NgaySinh khách hàng, nhưng danh sách quan hệ và DDL bỏ sót. Bản chạy bổ sung NgaySinh tùy chọn.
- Mô tả dùng SQL Server (`GO`, `NVARCHAR`, `GETDATE`, `ISNULL`). Bản chạy công khai dùng PostgreSQL, bản chạy tại máy dùng H2; thay bằng UTF-8 VARCHAR, CURRENT_TIMESTAMP, COALESCE và TIMESTAMP. Tên bảng, quan hệ và nghiệp vụ giữ theo lược đồ mới.

## Dữ liệu và kiểm tra

Bản mới dùng schema bookstore_v2 và dữ liệu mẫu của mô tả: 2 chi nhánh, 4 sách, 2 phiếu nhập, 3 hóa đơn. Thông tin liên hệ được thay bằng dữ liệu minh họa. Dữ liệu của bản 20 bảng vẫn được giữ trong schema cũ, không nhập lẫn vào mô hình mới.

KIEM-TRA.bat chạy ModelTest trên CSDL H2 tách biệt. Đã kiểm tra nghiệp vụ với H2 và PostgreSQL: tồn kho từng chi nhánh, hóa đơn khách lẻ, giá lịch sử, xóa/khôi phục phiếu, rollback khi thiếu tồn, tìm kiếm tiếng Việt, tổng tiền báo cáo và thống kê tháng. Bản kiểm tra giao diện dùng CSDL cục bộ; không xóa phiếu có sẵn trên web công khai.
