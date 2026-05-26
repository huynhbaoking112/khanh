# Hướng dẫn chạy Test - Văn Duy Khánh

**Người phụ trách:** Văn Duy Khánh
**Module:** Quản lý danh mục sách (Book Catalog)
**Phạm vi:** RV04-RV06 · TC07-TC12
**Kỹ thuật kiểm thử:** Phân tích giá trị biên, Phân hoạch tương đương

## Test file

- `BookCatalogTest.java` - toàn bộ TC07-TC12

## Chạy riêng

```powershell
.
mvnw.cmd "-Dtest=BookCatalogTest" test
```

## Ghi chú

- Dùng DB thật qua `com.thayhoang.quanly.testsupport.TestDbHelper`
- Báo cáo QC tự ghi vào `test/report/qc_report_latest.txt`