# TOEIC Study - Unit Tests

Đây là một Repository chuyên biệt (isolated repository) phục vụ mục đích kiểm thử (Unit Testing) dành riêng cho module **Học tập TOEIC** (Study Service bao gồm LessonProgress, TestProgress, Lesson, và StudyPlan) trong dự án.

Mục đích của việc tách repository là để thực thi quy trình TDD (Test-Driven Development) và SQA (Software Quality Assurance) độc lập, đánh giá chất lượng mã nguồn bằng Java/Maven mà không làm ảnh hưởng tới codebase chính của hệ thống.

---

## 📂 Cấu Trúc Dự Án

```text
toeic-study-unit-tests/
├── src/main/java       # Chứa các Class, Entity, DTOs, và Services được tách ra từ Core (dùng để test)
├── src/test/java       # Thư mục chính chứa tất cả các Test Suite bằng JUnit 5
├── target/             # Thư mục chứa các file compiled và báo cáo (Coverage Report) sau khi chạy test
├── pom.xml             # File cấu hình Maven, quản lý các dependencies (JUnit, Mockito, JaCoCo...)
└── README.md           # Tài liệu hướng dẫn sử dụng
```

---

## 🛠 Yêu Cầu Cài Đặt (Prerequisites)

Để chạy được project này, máy tính của bạn cần được cài sẵn:
- **Java Development Kit (JDK)**: Khuyên dùng phiên bản 17 trở lên.
- **Maven**: Công cụ build và quản lý thư viện của Java.
- Một IDE hỗ trợ Java (như IntelliJ IDEA, Eclipse, hoặc VS Code kèm Java Extension Pack).

---

## 🚀 Hướng Dẫn Sử Dụng

### Bước 1: Build và tải Dependencies
Mở thư mục gốc của project qua Terminal / Command Prompt và chạy:
```bash
mvn clean compile
```

### Bước 2: Chạy kịch bản kiểm thử (Test Scripts)

- **1. Để chạy toàn bộ các kịch bản kiểm thử (Unit Tests):**
  ```bash
  mvn test
  ```
  *(Lệnh này sẽ khởi chạy tất cả các test case bằng JUnit 5 Test Runner và in ra kết quả Build Success/Failure ở terminal. Kết quả chi tiết (Pass/Fail) sẽ được hiển thị trong log).*

- **2. Để xem báo cáo Code Coverage (JaCoCo - nếu có cấu hình):**
  Sau khi chạy `mvn test`, bạn có thể xem báo cáo độ phủ mã nguồn (Code Coverage) tại:
  `target/site/jacoco/index.html` (Mở file này bằng bất kỳ trình duyệt web nào).

---

## 💡 Thông Tin Kỹ Thuật (Architecture info)

- **Ngôn ngữ:** `Java`
- **Build Tool:** `Maven`
- **Framework Test:** `JUnit 5` đóng vai trò Test Runner và thư viện assertion chính.
- **Mocking:** Sử dụng `Mockito` để mock các dependencies (như database repositories) nhằm cô lập các logic cần kiểm thử trong Application Layer.
- **Approach:** 
    - Database Access Layer (Repositories) không được kết nối với DB thật.
    - Application Layer (StudyService) được cô lập bằng Mocking để tránh ảnh hưởng đến dữ liệu thực tế và đảm bảo tốc độ chạy test tối đa.

---
*Created by QA team for SQA Graduation Project.*
