# Unit Tests - Quản lý bài test đầu vào (Entrance Test Management)

## 1. Tổng quan

Unit test cho module **Quản lý bài test đầu vào** (Spring Boot backend), tập trung vào **tầng service** (business logic) với Mockito.

### 1.1. Tools and Libraries

| Thành phần | Vai trò |
|------------|---------|
| **JUnit 5 (Jupiter)** | Framework chạy test, annotation `@Test`, `@DisplayName`, lifecycle (`spring-boot-starter-test`). |
| **Mockito** | Mock repository / service phụ thuộc, `verify`, `when`, `@Mock`, `@InjectMocks` (`spring-boot-starter-test`). |
| **AssertJ** | Assertion dạng fluent (đi kèm `spring-boot-starter-test`). |
| **Spring Boot Test** | `@ExtendWith(MockitoExtension.class)`, tích hợp Mockito với JUnit 5. |
| **Spring Boot 3.4.4** | Parent POM, Java 17; `spring-boot-starter-data-jpa`, `spring-boot-starter-web` cho biên dịch mã nguồn được test. |
| **JaCoCo 0.8.12** | Đo **code coverage**; báo cáo HTML sau `mvn test` tại `target/site/jacoco/index.html`. |
| **Maven Surefire Plugin 3.5.3** | Thực thi test; kèm **maven-surefire-junit5-tree-reporter** để in cây Pass/Fail trên console. |
| **H2** (scope test) | Có thể dùng cho test tích hợp DB; hiện các testcase service chủ yếu dùng mock, không bắt buộc H2. |
| **Lombok**, **ModelMapper**, **Guava** | Hỗ trợ biên dịch mã nguồn giống môi trường dự án gốc. |
| **Apache POI 5.2.5** (scope test) | Xuất ma trận testcase ra Excel (`TestCaseExcelExporter`). |
| **Maven Exec Plugin** | Chạy `mvn test-compile exec:java` để tạo file Excel. |
| **Java 17**, **Maven 3.x** | Ngôn ngữ và công cụ build. |

### 1.2. Scope of Testing

#### Được kiểm thử (có unit test trực tiếp)

Các **lớp triển khai** sau được test qua lớp `*Test` tương ứng (mock dependency, không gọi DB thật):

| Lớp (implementation) | File test | Phương thức / hành vi được bao phủ bởi testcase |
|----------------------|------------|--------------------------------------------------|
| `TestServiceImpl` | `TestServiceImplTest` | `createTest`, `createFirstTest`, `commpletedStar`, `getMaxScore`, `isLock`, `isCompletedTest` |
| `TestAttemptServiceImpl` | `TestAttemptServiceImplTest` | `saveResultFirstTest`, `saveResultMiniTest` (gồm luồng tính điểm), `getTestAttemptDetailById` |
| `TestProgressServiceImpl` | `TestProgressServiceImplTest` | `checkCompletionCondition` |
| `AssessmentServiceImpl` | `AssessmentServiceImplTest` | `createAssessment`, `updateAssessment`, `getAssessmentDetailForFistTest` |

**Số testcase:** 29 (TS_01–TS_10, TA_01–TA_07, TP_01–TP_05, AS_01–AS_07).

#### Không nằm trong scope unit test (và lý do)

| Nhóm | Ví dụ trong mã nguồn | Lý do không ưu tiên / không test |
|------|----------------------|----------------------------------|
| **Entity** | `entity/*` | POJO/JPA entity, chủ yếu chứa dữ liệu; ít logic, thường kiểm thử gián tiếp qua service. |
| **DTO (request/response)** | `dto/*` | Chỉ mang dữ liệu, không có quy tắc nghiệp vụ. |
| **Repository (interface Spring Data)** | `repository/*` (trừ custom impl) | Phương thức truy vấn do framework sinh; độ tin cậy thuộc Spring Data JPA. |
| **Repository custom implementation** | `repository/custom/impl/*` | Thuộc module truy vấn mở rộng, không thuộc luồng “bài test đầu vào” đang chọn scope; có thể bổ sung test riêng nếu có logic JPQL/Criteria phức tạp. |
| **Converter** | `converter/*` | Ánh xạ đơn giản; rủi ro thấp, có thể cover gián tiếp qua service test. |
| **Exception / ErrorCode** | `exception/*` | Định nghĩa mã lỗi; không cần unit test độc lập. |
| **Các phương thức còn lại của cùng lớp service đã test** | Ví dụ trên `TestServiceImpl`: `getTestById`, `getFirstTestsSummery`, `getTestResponseDetail`, `getCompletedTestsOfStudent`, `getTestAttemptIds`, `getMiniTestsSummery`, `deleteTest`; trên `AssessmentServiceImpl`: `getSummaryAssessmentsByTestId`, `getAssessmentDetailById`, `deleteAssessmentById`, `getAssessmentsDetailByTestId` | Ngoài phạm vi use case đồ án đã chọn; có thể bổ sung sau nếu mở rộng scope. |
| **Service khác chỉ là dependency mock** | `LessonService`, `LessonProgressService`, `TrackService`, `EnrollmentServece`, … | Được mock trong `TestProgressServiceImplTest` (v.v.); logic thật nằm ở module khác, không test trong project này. |
| **Controller / REST** | (nếu có trong bản full của dự án) | Thường chỉ ủy quyền cho service; phù hợp test tích hợp/API riêng. |

---

## 2. Cấu trúc thư mục

```
MaiHieu-unit-tests/
  pom.xml
  README.md
  docs/                          (mặc định: testcase_matrix.xlsx sau khi chạy exporter)
  src/main/java/com/mxhieu/doantotnghiep/
    entity/, dto/, repository/, service/, converter/, exception/, utils/
  src/test/java/com/mxhieu/doantotnghiep/
    service/                     (TestServiceImplTest, …)
    report/TestCaseExcelExporter.java
```

---

## 3. Cách chạy

### Chạy toàn bộ test

```bash
cd MaiHieu-unit-tests
mvn clean test
```

**Lưu ý:** Đóng file `docs/testcase_matrix.xlsx` (nếu đang mở) trước khi `mvn clean` nếu trước đó bạn xuất Excel ra ngoài `target/`.

### Báo cáo coverage (JaCoCo)

```bash
mvn clean test
```

Mở: `target/site/jacoco/index.html`

### Chạy một lớp test

```bash
mvn test -Dtest=TestServiceImplTest
```

### Xuất ma trận testcase ra Excel

```bash
mvn test              # tùy chọn: để cột Kết quả có Pass/Fail
mvn test-compile exec:java
```

File mặc định: `docs/testcase_matrix.xlsx`. Đổi đường dẫn: `-Dexport.path=...`

---

## 4. Tóm tắt testcase

### TestServiceImplTest (10 — TS_01 … TS_10)

- TS_01–TS_08: luồng tạo test, sao hoàn thành, điểm tối đa (như mô tả `@DisplayName` trong mã).
- TS_09, TS_10: testcase phát hiện bug (NPE) trên `isLock` / `isCompletedTest` khi dữ liệu null.

### TestAttemptServiceImplTest (7 — TA_01 … TA_07)

- Lưu kết quả First test / Mini test, tính điểm, chi tiết attempt.

### TestProgressServiceImplTest (5 — TP_01 … TP_05)

- `checkCompletionCondition`: đủ điểm, chưa đủ điểm, test/student/progress không tồn tại.

### AssessmentServiceImplTest (7 — AS_01 … AS_07)

- Tạo/cập nhật assessment, lấy chi tiết cho First test, lỗi không tìm thấy.
