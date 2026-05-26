# Hướng dẫn chạy Test - Phạm Hùng Thiên

**Người phụ trách:** Phạm Hùng Thiên
**Module:** Lưu thông sách & Tính phạt (Circulation)
**Phạm vi:** RV10-RV12 · TC19-TC24
**Kỹ thuật kiểm thử:** Kiểm thử chuyển trạng thái, Phân tích giá trị biên

## Test file

- `CirculationTest.java` - toàn bộ TC19-TC24

## Chạy riêng

```powershell
.
mvnw.cmd "-Dtest=CirculationTest" test
```

## Ghi chú

- Dùng DB thật qua `com.thayhoang.quanly.testsupport.TestDbHelper`
- Báo cáo QC tự ghi vào `test/report/qc_report_latest.txt`