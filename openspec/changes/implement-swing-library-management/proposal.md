## Why

`docs/doc.md` mô tả khá đầy đủ một module quản lý mượn sách cho thư viện, nhưng mã nguồn hiện tại mới chỉ là một demo Swing rất nhỏ và chưa có phạm vi triển khai được chuẩn hóa. Cần chuyển nội dung tài liệu này thành một OpenSpec proposal rõ ràng để đội có thể implement ứng dụng desktop Swing theo đúng use case, quy tắc nghiệp vụ và mô hình dữ liệu đã phân tích.

## What Changes

- Xác định phạm vi cho ứng dụng desktop Swing quản lý thư viện tập trung vào đăng nhập, tra cứu, quản lý sách, quản lý độc giả, mượn, trả, gia hạn và xem lịch sử mượn.
- Chuẩn hóa các capability mức sản phẩm từ tài liệu phân tích sang các spec có thể kiểm thử thay vì giữ ở mức mô tả học thuật UML/EJB.
- Định nghĩa dữ liệu lõi của hệ thống theo các bảng `READER`, `LIBRARIAN`, `BOOK`, `LOAN`, `LOAN_DETAIL`, `FINE`.
- Chuyển kiến trúc từ mô hình EJB trong tài liệu sang kiến trúc desktop Swing có phân lớp rõ ràng giữa giao diện, dịch vụ nghiệp vụ và truy cập dữ liệu.
- Đặt ra task triển khai cụ thể để thay thế mã demo hiện có bằng ứng dụng quản lý thư viện hoàn chỉnh.

## Capabilities

### New Capabilities

- `desktop-authentication`: Đăng nhập desktop và điều hướng theo quyền của tài khoản vận hành.
- `book-catalog-management`: Quản lý danh mục sách và số lượng khả dụng phục vụ tra cứu và mượn trả.
- `reader-management`: Quản lý hồ sơ độc giả và kiểm tra điều kiện mượn theo trạng thái, giới hạn mượn.
- `loan-circulation`: Lập phiếu mượn, trả sách, gia hạn và tính tiền phạt theo quy tắc nghiệp vụ.
- `history-and-search`: Tra cứu sách và xem lịch sử mượn, trạng thái phiếu mượn, thông tin phạt.

### Modified Capabilities

- None.

## Impact

- Ảnh hưởng trực tiếp tới toàn bộ mã trong `src/` vì ứng dụng sẽ được tổ chức lại thành các package giao diện, nghiệp vụ, miền dữ liệu và truy cập dữ liệu.
- Cần bổ sung cơ chế lưu trữ bền vững cho dữ liệu thư viện và dữ liệu đăng nhập thay vì chỉ hiển thị giao diện mẫu.
- Cần thêm phụ thuộc runtime cho tầng dữ liệu desktop và tài liệu hướng dẫn khởi tạo dữ liệu mẫu.
