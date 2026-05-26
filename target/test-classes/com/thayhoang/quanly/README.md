# Phân chia kiểm thử theo 4 người

| Người | Module | RV | TC | Kỹ thuật |
|------|--------|----|----|----------|
| Huỳnh Bảo King | Đăng nhập (Auth) & Tra cứu lịch sử (History) | RV01-RV03 | TC01-TC06 | Phân hoạch tương đương, Đoán lỗi giao diện |
| Văn Duy Khánh | Quản lý danh mục sách (Book Catalog) | RV04-RV06 | TC07-TC12 | Phân tích giá trị biên, Phân hoạch tương đương |
| Vũ Đình Khoa | Quản lý thông tin độc giả (Reader Management) | RV07-RV09 | TC13-TC18 | Phân hoạch tương đương, Bảng quyết định |
| Phạm Hùng Thiên | Lưu thông sách & Tính phạt (Circulation) | RV10-RV12 | TC19-TC24 | Kiểm thử chuyển trạng thái, Phân tích giá trị biên |

## Thư mục test tương ứng

- [king_auth](king_auth)
- [khanh_book](khanh_book)
- [khoa_reader](khoa_reader)
- [thien_circulation](thien_circulation)

## Helper dùng chung

- [testsupport](testsupport)
