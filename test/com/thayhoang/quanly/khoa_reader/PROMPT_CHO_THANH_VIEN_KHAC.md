# Prompt gửi thành viên khác trong nhóm

> Copy nguyên đoạn dưới (giữa 2 dấu `---`) rồi paste vào Claude / Cursor / ChatGPT của bạn.
> Sửa 3 chỗ `<...>` ở đầu cho phù hợp với phần của mình.

---

Bạn là Senior Java Engineer kiêm SQA. Tôi là **`<TÊN BẠN>`** (Nhóm 9), thành viên phụ trách module **`<TÊN MODULE>`** với scope **`<MÃ TC, vd: TC07-TC12>`** trong project Quản lý Thư viện (Java Swing + JDBC SQLite).

## 1. Bối cảnh dự án

- **Project root:** `D:\Ptiter\2026\khanh\` (hoặc đường dẫn local)
- **Tài liệu nguồn (source of truth):** `docs/Chuong 4 - Hoat dong SQA va Thiet ke Kiem thu Nhom 9 V2.docx`
  - Phân công 4 thành viên (mục 4.1)
  - Checklist RV01-RV12 (mục 4.3)
  - Dữ liệu mẫu TD01-TD16 (mục 4.4)
  - RTM mapping FR ↔ Class Java ↔ TC (mục 4.5)
  - 24 testcase TC01-TC24 (mục 4.6)
- **Architecture:** layered Java
  - `src/com/thayhoang/quanly/domain/` - model + enum + rules
  - `src/com/thayhoang/quanly/application/` - service interface + impl + repository interface
  - `src/com/thayhoang/quanly/infrastructure/` - JDBC repository impl + DB bootstrap
  - `src/com/thayhoang/quanly/ui/` - Swing UI (LibraryShellFrame, LoginDialog)
- **Build:** Maven wrapper `mvnw.cmd` (Java 23 ở `D:\java`), test framework JUnit 5 + Mockito (đã có trong `pom.xml`)
- **DB:** SQLite file `data/library.db` (đã seed sẵn 2 librarian + 3 reader + 3 book)

## 2. Phần đã có làm mẫu (Khoa - Reader Management)

Toàn bộ phần làm của **Vũ Đình Khoa** (TC13-TC18, Reader Management) đã được implement đầy đủ ở:

```
test/com/thayhoang/quanly/khoa_reader/
├── README.md                              ← Hướng dẫn chạy chi tiết - ĐỌC TRƯỚC
├── QcReport.java                          ← Helper in báo cáo QC + auto-ghi file
├── GapReportBuilder.java                  ← Helper format GAP/BUG dạng template multi-line
├── GapDetector.java                       ← Helper reflection check enum/method/UI
├── TestDbHelper.java                      ← Helper thao tác DB thật (upsert/dump)
├── ReaderManagementIntegrationTest.java   ← TC13, TC14, TC18 (Integration)
├── ReaderManagementRulesTest.java         ← TC15, TC16 (Unit)
└── ReaderManagementSystemTest.java        ← TC17 (System - GUI)
```

**=> Hãy đọc kỹ folder `khoa_reader/` trước khi bắt đầu. Style/convention copy y hệt cho module mình.**

## 3. Task của tôi

1. **Đọc docx** → tìm dòng phân công có tên tôi (`<TÊN BẠN>`) ở mục 4.1
2. **Liệt kê** các checklist (RVxx-RVxx), data mẫu (TDxx-TDxx), testcase (TCxx-TCxx) của tôi
3. **Cross-check** từng TC với mục 4.6 để hiểu Input / Expected
4. **Implement** từng TC theo y chang style `khoa_reader/`
5. **Tạo folder mới** tên `test/com/thayhoang/quanly/<ten_module>/` (vd: `khanh_book/`, `king_auth/`, `thien_circulation/`)
6. **Tạo file README.md** hướng dẫn chạy y như Khoa
7. **Verify**: `.\mvnw.cmd test` phải PASS hết, có evidence trong DB + file `test/report/*.txt`

## 4. CONVENTIONS BẮT BUỘC (đúc kết từ phần Khoa)

### 4.1. Tests phải hit DB THẬT, không mock

Tài liệu V6 phân cấp Unit / Integration / System. Trừ Unit pure logic test, mọi test Integration / System của tôi PHẢI:
- Dùng class Jdbc thật (`JdbcReaderRepository`, `JdbcBookRepository`, ...) chứ không `mock(...)`
- Setup DB qua `TestDbHelper` (copy + mở rộng từ `khoa_reader/TestDbHelper.java`)
- Verify bằng SELECT lại từ DB
- Giáo viên có thể `sqlite3 data/library.db "SELECT ..."` để thấy evidence

### 4.2. Test data PHẢI có tên tự mô tả

KHÔNG dùng tên giả Vietnamese (vd. "Nguyen Van A") cho test data vì lẫn với reader thật. Dùng pattern `<Domain> <Purpose>`:
- ✅ `"Reader Create Valid"`, `"Reader With Phone"`, `"Reader Duplicate Target"` (Khoa đã làm)
- ✅ `"Book Create Valid"`, `"Book Out Of Stock"`, `"Book Damaged Test"` (cho Khánh)
- ✅ `"Librarian Admin Test"`, `"Librarian Locked Account"` (cho King)
- ❌ `"Nguyen Van A"`, `"Phạm Hùng Anh"` — lẫn với data thật

Email follow tên: `reader.create.valid@example.com`, `book.out.of.stock@example.com`.

### 4.3. Test ID PHẢI dedicated (không đụng seed/seed của thành viên khác)

- Seed có sẵn R001/R002/R003, B001/B002/B003, LIB001/LIB002 → KHÔNG được modify (sẽ phá test thành viên khác)
- Tạo ID test riêng cho module mình:
  - Khoa: `R013`, `R015`, `R016`, `RT14`, `RT18A/B/C`, `LT18A/B/C`
  - Khánh: nên dùng `B007`, `B008`, `BT09/10/11/12`, ...
  - King: nên dùng `LT01A/B/C`, ...
  - Thiên: nên dùng `LT19A/B/C`, ...
- Prefix `T` để mark "test fixture" (vd. `RT14` = Reader Test for TC14)

### 4.4. Setup phải IDEMPOTENT (chạy nhiều lần vẫn pass)

Mọi `upsertReader/upsertLoan` PHẢI dùng `INSERT ... ON CONFLICT ... DO UPDATE` (không DELETE+INSERT) để tránh FK violation khi LOAN còn ref READER. Xem `TestDbHelper.upsertReader` của Khoa.

### 4.5. GAP claims PHẢI có runtime evidence (không hardcode prose)

Khi tài liệu V6 yêu cầu behavior X mà code chưa làm, KHÔNG được tự viết "code thiếu X". Phải:
1. Dùng **reflection** để verify code có/không có feature đó:
   - Enum: `Arrays.asList(EnumClass.values()).contains(...)` hoặc `EnumClass.valueOf(...)`
   - Method: `Class.getDeclaredMethods()` rồi check method name
   - UI Component: `Class.forName("...$NestedClass")` rồi get field via reflection
2. Hoặc verify qua **DB state**: setup → action → SELECT lại, so sánh trước/sau
3. In ra evidence từ runtime (không hardcode giá trị)

Ví dụ phần Khoa - TC17 reflection check ReaderPanel có RowSorter không, in ra:
```
[REFLECTION EVIDENCE]
  inspected_class       : com.thayhoang.quanly.ui.LibraryShellFrame$ReaderPanel
  constructor_modifiers : private (mod=2)
  table.rowCount        : 3
  table.getRowSorter()  : null    ← evidence thật!
CONCLUSION: ...
FIX SUGGESTION: ...
SEVERITY: MEDIUM
```

Nếu dev fix feature → reflection check tự thấy method/sorter mới → test tự đổi PASS không cần sửa.

### 4.6. Báo cáo QC dạng template (dùng GapReportBuilder)

GAP/BUG dùng builder pattern:
```java
report.gap(GapReportBuilder.evidence("ENUM EVIDENCE")
    .field("BookStatus.values()", enumNames)
    .field("spec_required", "'DAMAGED'")
    .field("present_in_code", hasDamaged)
    .conclusion("Spec yêu cầu DAMAGED nhưng code dùng INACTIVE...")
    .fixSuggestion("Bổ sung enum DAMAGED vào BookStatus.java...")
    .severity("LOW (naming)")
    .build());
```

### 4.7. Output kép: terminal + file

`QcReport` tự ghi file `test/report/qc_report_<timestamp>.txt` + `qc_report_latest.txt`. KHÔNG xóa logic này — copy nguyên xi từ `khoa_reader/QcReport.java`.

### 4.8. KHÔNG đụng vào src/

Tests CHỈ thêm vào test/, KHÔNG sửa code trong src/ (đó là responsibility của Dev khi fix các GAP). Nếu cần thêm method (vd. `JdbcReaderRepository.findOverdueReaders`) → report là GAP, không tự sửa.

## 5. Deliverables

1. **Folder mới**: `test/com/thayhoang/quanly/<ten_module>/` với 6 file Java + 1 README.md
2. **6 test method** (1 file/cấp độ): Unit (Rules) / Integration / System
3. **README.md** structure y như Khoa: cấu trúc folder / yêu cầu env / cách chạy / bảng tra TC / troubleshooting
4. **Tất cả test PASS** (Failures=0, Errors=0)
5. **File report** `test/report/qc_report_*.txt` được auto-tạo
6. **DB evidence**: SELECT từ DB thấy row test có tên tự mô tả

## 6. Quy trình suggest

```
Phase 1 - Đọc & hiểu (KHÔNG code):
  1. Đọc README.md của Khoa
  2. Đọc 6 file Java của Khoa (đặc biệt QcReport, GapReportBuilder, TestDbHelper)
  3. Đọc docx để hiểu phần của tôi
  4. Map từng TC của tôi → cấp độ (Unit / Integration / System)
  5. Map từng TC → các class src/ mà nó cần test

Phase 2 - Implement (copy + adapt):
  6. Copy folder khoa_reader/ → <ten_module>/ rồi đổi tên file
  7. Đổi package declaration trong các file
  8. Sửa import (đổi Reader → Book/Librarian/Loan tùy module)
  9. Implement TC theo Input/Expected từ docx
  10. Mọi GAP claim phải có evidence động

Phase 3 - Verify:
  11. .\mvnw.cmd test → 100% PASS
  12. sqlite3 data/library.db → thấy test data của mình
  13. Mở test/report/qc_report_latest.txt → đọc xem có đầy đủ Input/Expected/Actual/GAP không
  14. Commit lên branch riêng (vd. test/<ten>-<module>-runs), KHÔNG push data/library.db
```

## 7. Lệnh chạy

```powershell
$env:JAVA_HOME = 'D:\java'
$env:Path = "D:\java\bin;$env:Path"
.\mvnw.cmd test
```

Verify DB:
```powershell
sqlite3 data\library.db ".tables"
sqlite3 data\library.db "SELECT * FROM <BANG_CUA_MODULE>"
```

## 8. Tài liệu tham khảo

- `test/com/thayhoang/quanly/khoa_reader/README.md` ← đọc trước
- `test/com/thayhoang/quanly/khoa_reader/*.java` ← reference implementation
- `docs/Chuong 4 ... .docx` ← spec gốc
- `pom.xml` ← Maven config (đã có sqlite-jdbc + junit + mockito)

## 9. KHÔNG cần làm

- ❌ Sửa code trong `src/` (đó là việc của Dev, không phải QA)
- ❌ Add Maven dependency mới (pom.xml đủ rồi)
- ❌ Mock các repository nếu có thể hit DB thật
- ❌ Hardcode giá trị vào GAP report message
- ❌ Đụng vào folder của thành viên khác

---

**Bắt đầu Phase 1 đi: đọc folder `khoa_reader/` + README + docx, rồi tóm tắt cho tôi: tôi sẽ làm TC nào, mỗi TC ở cấp độ nào, cần test class src/ nào. CHƯA cần code.**
