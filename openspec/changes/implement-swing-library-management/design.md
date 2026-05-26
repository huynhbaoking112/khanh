## Overview

Thiết kế này chuyển nội dung `docs/doc.md` từ mô hình EJB sang ứng dụng desktop Swing nhưng vẫn giữ nguyên các actor, use case, quy tắc nghiệp vụ và cấu trúc dữ liệu cốt lõi. Mục tiêu là có một ứng dụng chạy cục bộ, dễ demo và dễ mở rộng, trong đó service layer đóng vai trò tương đương các `*ServiceBean` và `*Local` trong tài liệu.

## Architecture

Ứng dụng được chia thành bốn lớp:

1. `ui`: `JFrame`, `JPanel`, `JDialog`, bảng dữ liệu và form nhập liệu cho đăng nhập, quản lý sách, quản lý độc giả, mượn trả, lịch sử.
2. `application`: các service interface và service implementation cho xác thực, sách, độc giả, lưu thông.
3. `domain`: model, enum trạng thái, request/response object và các rule helper.
4. `infrastructure`: repository JDBC, bootstrap schema, seed data và mapper dữ liệu.

Service layer sẽ dùng interface riêng để giữ ranh giới rõ ràng, tương đương vai trò của `BorrowServiceLocal`, `BookServiceLocal`, `ReaderServiceLocal` trong tài liệu.

## Persistence Strategy

- Dùng SQLite cục bộ để bám sát thiết kế dữ liệu quan hệ trong tài liệu và phù hợp với desktop app.
- Khởi tạo schema theo các bảng `READER`, `LIBRARIAN`, `BOOK`, `LOAN`, `LOAN_DETAIL`, `FINE`.
- Seed ít nhất một tài khoản vận hành và dữ liệu mẫu để có thể chạy ứng dụng ngay sau khi build.

## UI Flow

- Màn hình đầu tiên là đăng nhập.
- Sau đăng nhập, ứng dụng mở `MainFrame` với menu điều hướng theo quyền.
- Các form nghiệp vụ chính gồm:
  - tra cứu và quản lý sách
  - tra cứu và quản lý độc giả
  - lập phiếu mượn
  - trả sách và tính phạt
  - gia hạn phiếu mượn
  - xem lịch sử mượn

## Business Rules

- Mỗi độc giả chỉ được mượn tối đa 3 cuốn đang hoạt động tại cùng thời điểm.
- Chỉ độc giả ở trạng thái hoạt động mới được mượn.
- Chỉ sách có `quantity_available > 0` mới được đưa vào phiếu mượn.
- Hạn trả mặc định là 7 ngày kể từ ngày lập phiếu.
- Tiền phạt được tính theo `so_ngay_tre * 5000`.
- Trả sách phải tăng lại số lượng khả dụng và không được xử lý trùng cho cùng một chi tiết mượn.
- Gia hạn chỉ áp dụng cho phiếu còn hiệu lực và chưa hoàn tất.

## Risks And Mitigations

- Tài liệu gốc có định hướng EJB nhưng codebase là desktop app.
  Giảm rủi ro bằng cách giữ nguyên service boundary và entity model, chỉ thay cơ chế wiring.
- Nghiệp vụ mượn trả dễ phát sinh sai lệch dữ liệu số lượng.
  Giảm rủi ro bằng transaction ở service layer và test các ca ngoại lệ.
- Màn hình Swing dễ trở nên rối nếu xử lý logic trực tiếp trong UI.
  Giảm rủi ro bằng cách để toàn bộ rule kiểm tra ở service layer.
