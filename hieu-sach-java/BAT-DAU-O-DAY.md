# Bắt đầu ở đây

Bạn được giao phần website Java. Trước mắt, hãy chạy được bản này rồi tập đúng một lượt demo. Chưa cần học toàn bộ lập trình web trước khi bắt đầu.

## 1. Mở website

1. Mở thư mục `hieu-sach-java`.
2. Nhấp đúp `CHAY-WEB.bat`.
3. Giữ cửa sổ hiện ra đang mở. Khi thấy `http://localhost:8080`, mở địa chỉ đó trong trình duyệt.
4. Dừng bằng `Ctrl+C` trong cửa sổ Java. Chạy lại bằng cùng file `.bat`.

Máy hiện tại đã có JDK 21. Máy khác cần JDK 21 trở lên, gồm cả lệnh `java` và `javac`. Toàn bộ thư viện H2 đã có trong `lib`; không cần Maven, Node hoặc Internet để chạy bản bàn giao.

Nếu báo cổng 8080 đang được dùng, kiểm tra xem bản web cũ còn chạy không và dừng bản cũ bằng Ctrl+C. Có thể đổi cổng bằng lệnh `java -Dbookstore.port=8081 -cp "build;lib/h2-2.5.252.jar" Bookstore`, sau khi đã chạy bước biên dịch trong file `.bat`.

## 2. Tập một lượt demo ngay

1. **Hàng hóa → Thêm hàng hóa**: mã `HS015`, tên `Sách demo của nhóm`, chọn Sách, giá `50000`, tồn đầu `5`, tác giả `Tác giả demo`, thể loại `Giáo dục`.
2. **Khách hàng → Thêm khách hàng**: mã `KH009`, tên `Khách demo`, địa chỉ `Hà Đông - Hà Nội`, số điện thoại giả định `0909999999`.
3. **Tìm kiếm**: nhập `HS015`, bấm Tìm kiếm. Đây là màn hình tìm bằng SQL; bộ lọc nhanh ở trang Hàng hóa chỉ lọc danh sách đã tải.
4. **Bán hàng → Lập hóa đơn bán**: chọn khách vừa thêm, nhân viên bán hàng, sách HS015, số lượng `2`; thêm một món khác rồi lưu. Mở hóa đơn vừa lưu, bấm **In / lưu PDF** để mở hộp thoại in của trình duyệt.
5. Quay lại **Hàng hóa**: HS015 còn `3` quyển.
6. Thử lập hóa đơn HS015 số lượng `999`: nhận thông báo thiếu hàng, số lượng tồn vẫn là `3`.
7. **Báo cáo → Doanh thu**: chọn khoảng ngày chứa ngày hóa đơn vừa tạo. Bấm **Xuất CSV** hoặc **In / lưu PDF**.
8. **Báo cáo → Tồn kho**: nhập ngưỡng `20`. HS015 xuất hiện trong danh sách.
9. Mở báo cáo **Khách mua nhiều nhất**, **Hàng bán nhiều nhất** với khu vực `Hà Đông`, xem phần **SQL và tham số**.
10. **Cơ sở dữ liệu**: xem 20 bảng và mở file SQL tạo bảng / truy vấn để giải thích cho cô.

Các mã trên dùng cho lần tập đầu. Nếu tập lại và báo trùng mã, đổi sang `HS016`, `KH010`… Hóa đơn tự sinh mã.

## 3. Hiểu đúng ba phần của website

```text
Bạn thao tác trên trình duyệt
       ↓ yêu cầu HTTP
Java: src/Bookstore.java
       ↓ JDBC + câu lệnh SQL có tham số
H2: data/hieusach.mv.db
       ↓ kết quả
Java gửi kết quả về trình duyệt
```

- **HTML/CSS/JavaScript** trong `web/`: giao diện, nút bấm, form. JavaScript chạy trong trình duyệt.
- **Java** trong `src/Bookstore.java`: máy chủ web, kiểm tra dữ liệu, ghi và đọc CSDL. Đây là phần đáp ứng yêu cầu làm website bằng Java. Java và JavaScript là hai ngôn ngữ khác nhau.
- **SQL/H2**: các bảng, khóa, dữ liệu và truy vấn. H2 là hệ quản trị CSDL quan hệ có thật; bản này lưu dữ liệu vào file trên ổ đĩa, không dùng localStorage để giả lập CSDL.

## 4. Chức năng đã có

| Yêu cầu cô gửi | Chỗ demo |
|---|---|
| Ít nhất 2 form nhập | Thêm hàng hóa; Thêm khách hàng |
| Nghiệp vụ nhập / bán theo bài mẫu | Lập phiếu nhập; Lập hóa đơn bán nhiều mặt hàng |
| 1 màn hình tìm kiếm | Tìm kiếm theo mã, tên, tác giả, thể loại / nhóm hàng; lọc loại |
| Ít nhất 2 báo cáo | Doanh thu theo ngày; Hàng tồn kho dưới ngưỡng |
| Bám truy vấn trong ảnh | Khách mua nhiều nhất theo khu vực (câu 2); Hàng bán nhiều nhất theo khu vực (câu 7) |
| SQL tương ứng | `sql/schema.sql`, `sql/data.sql`, `sql/queries.sql`; SQL báo cáo hiện ngay trên web |
| Phân tích, thiết kế, chuẩn hóa | `docs/BAO-CAO-THIET-KE.md`; sơ đồ ERD và Data Diagram dạng SVG |

Đây là ứng dụng quản lý nội bộ cho demo môn học. Danh mục nhân viên, chủ cửa hàng, nhà cung cấp, chi nhánh được nạp sẵn; hiện chưa có form sửa/xóa các danh mục này, đăng nhập, phân quyền hoặc bán hàng trực tuyến. Các ảnh là bài tham khảo trên mạng; nhóm cần trình bày mô hình đã điều chỉnh thực sự dùng trong chương trình.

## 5. Xem dữ liệu và sao lưu

- Dữ liệu thật nằm ở `data/hieusach.mv.db` sau lần chạy đầu. Khởi động lại giữ nguyên dữ liệu; dữ liệu mẫu chỉ nạp một lần khi CSDL mới được tạo.
- Để sao lưu: dừng Java bằng Ctrl+C rồi copy cả thư mục dự án sang nơi khác. Không copy file CSDL khi Java đang ghi dữ liệu. File sao lưu có thể chứa những dữ liệu bạn đã nhập.
- Để chạy trên máy thuyết trình: copy toàn bộ `hieu-sach-java`, bảo đảm máy có JDK 21+, rồi mở `CHAY-WEB.bat`. Không cần đưa website lên Internet để demo trên máy.
- Muốn gõ SQL trực tiếp: dừng web, mở `MO-CSDL.bat`; JDBC URL `jdbc:h2:file:./data/hieusach`, user `sa`, mật khẩu để trống. Chỉ mở H2 Console hoặc website tại một thời điểm. Dừng cửa sổ Console bằng Ctrl+C (không chỉ đóng tab trình duyệt), rồi chạy lại web. Console dùng cho học tập tại máy.
- Nếu CSV mở chưa tách cột đúng trong Excel, dùng Data → From Text/CSV, chọn UTF-8 và dấu phẩy. CSV đã có BOM để giữ tiếng Việt.

## 6. Việc nhóm cần làm trước ngày nộp

Theo thông báo bạn cung cấp: hạn nộp **23:00 ngày 03/11/2026**, báo cáo **04/11 hoặc 11/11/2026**, mỗi nhóm **15 phút**. Tính từ 28/10, nhóm còn khoảng 6 ngày đến hạn nộp.

| Ngày | Việc cần hoàn thành |
|---|---|
| 28/10 | Chạy web, chốt phạm vi với nhóm, đọc các thay đổi so với bài mẫu |
| 29/10 | Thành viên làm CSDL đối chiếu ERD, Data Diagram, khóa và phụ thuộc hàm với SQL đang chạy |
| 30/10 | Tập nhập, tìm, lập hóa đơn và in / xuất hai báo cáo |
| 31/10 | Ghép báo cáo và slide của nhóm; thống nhất tên bảng / thuộc tính |
| 01/11 | Chạy thử trên máy sẽ thuyết trình, lưu bản dự phòng |
| 02/11 | Tập cả nhóm dưới 15 phút, chia người nói và người thao tác |
| 03/11 | Chốt file nộp và nộp trước 23:00, dành thời gian dự phòng |

Nếu cô đã yêu cầu riêng SQL Server / MySQL ở thông báo khác, nhóm cần chuyển đúng hệ trước khi nộp. Thông báo hiện bạn gửi chưa chỉ định hệ quản trị; H2 được chọn để bản Java chạy ngay. Cú pháp và công cụ H2 không thể mặc nhiên coi là giống SQL Server / MySQL.

## 7. Cần đọc tiếp file nào?

- `docs/BAO-CAO-THIET-KE.md`: đặc tả dữ liệu, 20 bảng, khóa, phụ thuộc hàm, 3NF, sơ đồ.
- `docs/DOI-CHIEU-TAI-LIEU.md`: ảnh nào / slide nào được dùng, thay đổi và lý do.
- `docs/KICH-BAN-DEMO.md`: lời giải thích ngắn và thứ tự demo 15 phút.
- `tests/IntegrationTest.java`: kiểm tra nghiệp vụ trên CSDL thử riêng, không dùng CSDL demo của bạn. Mở `KIEM-TRA.bat` để chạy.
