# Hướng dẫn chạy Test - Huỳnh Bảo King

**Người phụ trách:** Huỳnh Bảo King
**Module:** Đăng nhập (Auth) & Tra cứu lịch sử (History)
**Phạm vi:** RV01-RV03 · TC01-TC06
**Kỹ thuật kiểm thử:** Phân hoạch tương đương, Đoán lỗi giao diện

## Test file

- `AuthHistoryTest.java` - toàn bộ TC01-TC06

## Chạy riêng

```powershell
.
mvnw.cmd "-Dtest=AuthHistoryTest" test
```

## Ghi chú

- Dùng DB thật qua `com.thayhoang.quanly.testsupport.TestDbHelper`
- Báo cáo QC tự ghi vào `test/report/qc_report_latest.txt`