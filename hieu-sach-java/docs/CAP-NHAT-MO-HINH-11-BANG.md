> Bổ sung xóa nhân viên: nhân viên đã có chứng từ được đánh dấu NHAN_VIEN.DaXoa và nghỉ việc, không còn hiện trong danh mục / ô chọn. Chứng từ cũ giữ nguyên nhân viên và số tiền. Nhân viên nhập hàng chỉ lập phiếu nhập; muốn lập hóa đơn bán cần đổi chức vụ hoặc dùng quản trị viên.

> Cập nhật bổ sung 30/10: bốn danh mục có nút Xóa (bản ghi đang được tham chiếu sẽ báo lý do); sách có Ngưng kinh doanh / Kinh doanh lại riêng với Xóa. Cờ kỹ thuật SACH.DaXoa giữ chứng từ cũ khi xóa sách, ngưng kinh doanh vẫn hiển thị. Báo cáo có thông báo tải/cập nhật và tự cuộn đến kết quả; số điện thoại bỏ độ dài tối thiểu, tối đa 15 ký tự.

> Cập nhật 30/10: Xóa phiếu nhập gỡ hẳn phiếu và chi tiết sau khi hoàn tác tồn; không hiển thị phiếu đã xóa. Sách đã xóa biến mất khỏi danh mục và tìm kiếm; chứng từ cũ được giữ. Khách hàng có nút Xóa và mã khách được sửa với ON UPDATE CASCADE, ON DELETE SET NULL trên hóa đơn. Danh mục sách có lọc thể loại và sắp xếp giá / tồn kho tăng giảm. Các mô tả khôi phục phiếu bên dưới thuộc bản trước.

# Bản cập nhật theo mô tả và ERD của nhóm — 29/10/2026

Nguồn chính: [mô tả ERD-DATA DIAGRAM](https://docs.google.com/document/d/1Nl8SI_FzWJgpHifEfb6gIRrG0407_QkaPEBrJ6d0wAg/edit) và [lược đồ nhóm](https://app.diagrams.net/#G14YQBJLRh5wd0LLUGNSIdfBej51VhAP_g).

## Mô hình đang chạy

11 bảng: CHI_NHANH, NHAN_VIEN, KHACH_HANG, THE_LOAI, SACH, NHA_CUNG_CAP, PHIEU_NHAP, CT_PHIEU_NHAP, HOA_DON_BAN, CT_HOA_DON_BAN, TON_KHO.

Sơ đồ được sinh từ cấu trúc bảng và khóa ngoại trong cơ sở dữ liệu. [Data Diagram MySQL](../web/data-diagram.svg)

- CHI_NHANH là cửa hàng của hiệu sách; nhà cung cấp là một danh mục độc lập.
- TON_KHO có khóa ghép (MaCN, MaSach), không gộp tồn kho của các cửa hàng khi bán.
- Mỗi sách thuộc một thể loại. ISBN duy nhất nếu được cung cấp; giá không âm.
- Nhân viên có chi nhánh, chức vụ và trạng thái. Nhân viên thường chỉ lập đúng nghiệp vụ tại chi nhánh của mình. Quản trị viên có thể lập tại các chi nhánh, phù hợp phiếu mẫu PN20261002.
- Khách mua lẻ không cần hồ sơ: MaKH của hóa đơn là NULL.
- Ngày giờ bắt buộc trên hóa đơn bán và phiếu nhập, lưu bằng kiểu DATETIME(6) trong MySQL theo giờ Việt Nam. Biểu đồ và báo cáo dùng ngày giờ chứng từ.
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
- Tài liệu nhóm có ví dụ cú pháp SQL Server. Website dùng MySQL 8.4 LTS với VARCHAR mã hóa utf8mb4, CURRENT_TIMESTAMP, COALESCE và DATETIME. Tên bảng, quan hệ và nghiệp vụ theo lược đồ nhóm.

## Dữ liệu và kiểm tra

Web dùng cơ sở dữ liệu MySQL bookstore_v2. Lần đầu trên cơ sở dữ liệu trống sẽ nạp 2 chi nhánh, 4 sách, 2 phiếu nhập và 3 hóa đơn mẫu. Dữ liệu của mô hình 20 bảng được lưu trong bản sao cơ sở dữ liệu trước đây.

KIEM-TRA.bat chạy các kiểm tra trên một cơ sở dữ liệu MySQL riêng. Các kiểm tra bao gồm tồn kho theo chi nhánh, hóa đơn khách lẻ, giá lịch sử, xóa phiếu, rollback khi thiếu tồn, tìm kiếm tiếng Việt, báo cáo và thống kê tháng.
