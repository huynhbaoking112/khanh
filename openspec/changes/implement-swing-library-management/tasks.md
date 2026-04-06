## 1. Foundation

- [x] 1.1 Tổ chức lại `src/` thành các package cho `ui`, `application`, `domain`, `infrastructure` và cập nhật entry point Swing.
- [x] 1.2 Thêm phụ thuộc JDBC cần thiết và bootstrap cơ sở dữ liệu SQLite cục bộ cho ứng dụng desktop.
- [x] 1.3 Tạo các model và enum cho `Reader`, `Librarian`, `Book`, `Loan`, `LoanDetail`, `Fine`.

## 2. Persistence And Services

- [x] 2.1 Tạo schema và repository cho các bảng `READER`, `LIBRARIAN`, `BOOK`, `LOAN`, `LOAN_DETAIL`, `FINE`.
- [x] 2.2 Implement service interface cho xác thực, quản lý sách, quản lý độc giả, lưu thông mượn trả và lịch sử.
- [x] 2.3 Implement các rule nghiệp vụ về giới hạn mượn, trạng thái độc giả, số lượng khả dụng, gia hạn và tính phạt.

## 3. Desktop UI

- [x] 3.1 Xây dựng màn hình đăng nhập và `MainFrame` điều hướng theo quyền.
- [x] 3.2 Xây dựng màn hình tra cứu và quản lý sách với bảng dữ liệu và form cập nhật.
- [x] 3.3 Xây dựng màn hình tra cứu và quản lý độc giả với trạng thái hoạt động và giới hạn mượn.
- [x] 3.4 Xây dựng luồng lập phiếu mượn, trả sách và gia hạn với kiểm tra lỗi nghiệp vụ tại chỗ.
- [x] 3.5 Xây dựng màn hình lịch sử mượn và thông tin phạt theo độc giả hoặc phiếu mượn.

## 4. Verification

- [ ] 4.1 Viết test cho service layer bao phủ các trường hợp mượn hợp lệ, vượt giới hạn, sách hết, trả trễ và gia hạn không hợp lệ.
- [ ] 4.2 Kiểm tra thủ công các luồng Swing chính với dữ liệu mẫu và cập nhật tài liệu chạy dự án.
