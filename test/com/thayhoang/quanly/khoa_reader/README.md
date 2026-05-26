# Hướng dẫn chạy Test - Vũ Đình Khoa

**Người phụ trách:** Vũ Đình Khoa
**Module:** Quản lý thông tin độc giả (Reader Management)
**Phạm vi:** Checklist RV07-RV09 · Test data TD09-TD12 · Test cases **TC13-TC18**
**Kỹ thuật kiểm thử:** Phân hoạch tương đương, Bảng quyết định

---

## 1. Cấu trúc folder

```
test/com/thayhoang/quanly/khoa_reader/
├── README.md                              ← file này
├── QcReport.java                          ← helper in báo cáo QC (terminal + file)
├── GapReportBuilder.java                  ← helper format GAP/BUG (template multi-line)
├── GapDetector.java                       ← helper reflection check gap (enum / method / UI)
├── TestDbHelper.java                      ← helper thao tác DB thật (upsert/dump/...)
├── ReaderManagementIntegrationTest.java   ← TC13, TC14, TC18 (Integration)
├── ReaderManagementRulesTest.java         ← TC15, TC16 (Unit)
└── ReaderManagementSystemTest.java        ← TC17 (System - cần GUI)

test/report/                               ← auto-tạo khi chạy test
├── qc_report_<timestamp>.txt              ← lưu mỗi lần chạy
└── qc_report_latest.txt                   ← luôn là lần chạy gần nhất
```

---

## 2. Yêu cầu môi trường

| Công cụ | Phiên bản | Vị trí trên máy này |
|---------|-----------|---------------------|
| Java JDK | 17+ (đang dùng 23) | `D:\java` |
| Maven | 3.6.3 (qua wrapper) | `mvnw.cmd` ở project root |
| SQLite | tự download qua Maven | `lib/sqlite-jdbc-3.51.2.0.jar` |

> Không cần cài `mvn` global — wrapper `mvnw.cmd` đã có sẵn.

---

## 3. Cách chạy

### Bước 1: Set Java env (PowerShell)
```powershell
$env:JAVA_HOME = 'D:\java'
$env:Path = "D:\java\bin;$env:Path"
```

### Bước 2: Chạy test

| Mục đích | Lệnh |
|----------|------|
| Chạy **tất cả 6 testcase** của Khoa | `.\mvnw.cmd test` |
| Chạy **chỉ unit tests** (TC15, TC16) | `.\mvnw.cmd "-Dtest=ReaderManagementRulesTest" test` |
| Chạy **chỉ integration tests** (TC13, TC14, TC18) | `.\mvnw.cmd "-Dtest=ReaderManagementIntegrationTest" test` |
| Chạy **chỉ system test** (TC17) | `.\mvnw.cmd "-Dtest=ReaderManagementSystemTest" test` |
| Chạy **1 testcase cụ thể** | `.\mvnw.cmd "-Dtest=ReaderManagementRulesTest#tc15_updateReaderStatusToLockedPersistsNewState" test` |

### Bước 3: Xem báo cáo

| Cách | Nơi xem |
|------|---------|
| Trên terminal (live) | Output ngay khi chạy |
| File text (history) | `test/report/qc_report_<timestamp>.txt` |
| File latest | `test/report/qc_report_latest.txt` |
| DB evidence | `sqlite3 data/library.db "SELECT * FROM READER WHERE reader_id IN ('R013','R015','R016','RT14','RT18A','RT18B','RT18C')"` |

---

## 4. Bảng tra cứu Test Case

| TC | Cấp độ | Yêu cầu | Test method | DB rows tạo ra |
|----|--------|---------|-------------|----------------|
| **TC13** | Integration | FR06 - Tạo độc giả mới | `tc13_createReaderWithValidFormPersistsAsActive` | R013 (Reader Create Valid) |
| **TC14** | Integration | FR06 - Ràng buộc PK trùng | `tc14_duplicateReaderIdCausesRejection` | RT14 (Reader Duplicate Target) |
| **TC15** | Unit | FR07 - Đổi trạng thái thẻ | `tc15_updateReaderStatusToLockedPersistsNewState` | R015 (Reader Status Lock) - status INACTIVE |
| **TC16** | Unit | FR07 - Validate phone bắt buộc | `tc16_emptyReaderPhoneCurrentlyPassesValidation` | R016 (Reader With Phone) - phone='' (gap) |
| **TC17** | System | FR08 - Filter JTable | `tc17_readerTableFilterShowsOnlyMatchingRows` | (mock, không ghi DB) |
| **TC18** | Integration | FR08 - Tìm độc giả trễ hạn | `tc18_overdueReadersResolvedFromRealLoanData` | RT18A/B/C + LT18A/B/C |

---

## 5. Đọc báo cáo QC

Mỗi test in ra 1 "report card" cấu trúc cố định:

```
==============================================================================
[PASS | PASS/GAP]  TCxx  |  Level  |  Requirement  |  Dataset
------------------------------------------------------------------------------
PRECONDITION : ...
INPUT        : ...
EXPECTED     : (theo tài liệu V6)
ACTUAL       : (giá trị thật từ runtime + DB)
------------------------------------------------------------------------------
DB BEFORE    : (state DB trước test)
DB AFTER     : (state DB sau test)
------------------------------------------------------------------------------
GAP / BUG    :                                ← chỉ xuất hiện nếu có gap
  [EVIDENCE LABEL]
    field1 : value1                           ← evidence từ reflection/runtime
    field2 : value2
  CONCLUSION:
    ...                                       ← kết luận từ evidence
  FIX SUGGESTION:
    ...                                       ← code/hành động dev cần làm
  SEVERITY: LOW | MEDIUM | HIGH
==============================================================================
```

**Status:**
- `[PASS]` = Code khớp 100% với tài liệu V6
- `[PASS / GAP]` = Test pass nhưng phát hiện gap giữa code và tài liệu → Dev cần fix

---

## 6. Bằng chứng "không hardcode"

4 GAPs hiện có (TC15/TC16/TC17/TC18) đều dùng **runtime evidence** từ reflection/DB:

| TC | Evidence từ đâu |
|----|----------------|
| TC15 | `Arrays.toString(ReaderStatus.values())` - thật |
| TC16 | `fromDb.phone()` sau khi UPDATE - thật trong DB |
| TC17 | Reflect vào `LibraryShellFrame$ReaderPanel`, gọi `table.getRowSorter()` - thật |
| TC18 | Đếm số `findByReaderId` calls + `JdbcReaderRepository.getDeclaredMethods()` - thật |

→ Nếu Dev fix các gap này, báo cáo tự đổi `[PASS / GAP]` thành `[PASS]`, không cần Khoa sửa test.

---

## 7. Troubleshooting

| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `'mvnw.cmd' is not recognized` | Đang ở folder khác project root | `cd D:\Ptiter\2026\khanh` |
| `java: command not found` | Chưa set JAVA_HOME | Chạy lại 2 dòng `$env:` ở Bước 1 |
| `Khong tim thay SQLite JDBC driver` | Chưa add dependency trong pom.xml | Đã fix - kiểm tra pom.xml có `<artifactId>sqlite-jdbc</artifactId>` |
| `FOREIGN KEY constraint failed` | Setup test xung đột với LOAN cũ | Helper `upsertReader` đã dùng `ON CONFLICT` - không còn xảy ra |
| `assumeFalse(isHeadless())` skip TC17 | Đang chạy trên server không có màn hình | TC17 chỉ chạy trên máy có GUI |
| File `qc_report_*.txt` không sinh ra | Working dir sai (không phải project root) | Chạy `mvnw.cmd` từ `D:\Ptiter\2026\khanh\` |

---

## 8. Git workflow (cho Khoa)

```powershell
# Branch riêng cho test runs (đụng DB)
git checkout test/khoa-reader-management-runs

# Khi cần PR phần Khoa lên repo nhóm (KHÔNG kèm DB binary):
git checkout main
git checkout -b test/khoa-reader-management
git checkout test/khoa-reader-management-runs -- test/com/thayhoang/quanly/khoa_reader/ pom.xml mvnw mvnw.cmd .mvn/
git commit -m "test(reader): add Khoa's Reader Management testcases TC13-TC18"
git push -u origin test/khoa-reader-management
```

**⚠️ KHÔNG push:** `data/library.db` (binary, đầy data test) và branch `test/khoa-reader-management-runs`.

---

## 9. Liên hệ

**Tester:** Vũ Đình Khoa
**Email:** dev1@wolffungame.com
**Nhóm:** Nhóm 9 - QLTV V6
**Tài liệu:** `docs/Chuong 4 - Hoat dong SQA va Thiet ke Kiem thu Nhom 9 V2.docx`
