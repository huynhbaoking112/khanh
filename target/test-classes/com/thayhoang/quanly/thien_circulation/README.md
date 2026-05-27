# Huong dan chay Test - Pham Hung Thien

**Nguoi phu trach:** Pham Hung Thien
**Module:** Luu thong sach & Tinh phat (Circulation)
**Pham vi:** RV10-RV12 - TC19-TC24
**Ky thuat kiem thu:** Kiem thu chuyen trang thai, Phan tich gia tri bien

## Test files

- `CirculationLoanCreationTest.java` - TC19
- `CirculationBorrowingRulesTest.java` - TC20-TC21
- `CirculationReturnTest.java` - TC22-TC23
- `CirculationRenewalTest.java` - TC24

## Chay rieng

```powershell
.\mvnw.cmd "-Dtest=com.thayhoang.quanly.thien_circulation.*Test" test
```

## Ghi chu

- Dung DB that qua `com.thayhoang.quanly.testsupport.TestDbHelper`
- Bao cao QC tu ghi vao `test/report/qc_report_latest.txt`
