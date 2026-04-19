# TOEIC Exam Management - Unit Tests

Đây là một Repository chuyên biệt (isolated repository) được tách ra phụ vụ mục đích kiểm thử (Unit Testing) dành riêng cho module **Quản lý Bài test đầu vào** (Exam Service) trong dự án TOEIC. 

Mục đích của việc tách repository là để thực thi quy trình TDD (Test-Driven Development) và SQA (Software Quality Assurance) độc lập, không làm phình to core logic của hệ thống.

---

## 📂 Cấu Trúc Dự Án

```text
exam-management-unit-tests/
├── src/                    # Data models, DTOs & Entity Objects từ Core Backend (dùng để Mock Input)
├── tests/                  # Thư mục chính chứa tất cả các Test Suite (exam.service.test.ts)
├── export-report.js        # Script Node.js tự động parse kết quả và xuất báo cáo Test ra Excel
├── jest.config.ts          # File thiết lập scope và settings cho platform Jest
├── package.json            # Chứa các thư viện test độc lập (Jest, ts-jest, ts-node, exceljs)
└── tsconfig.json           # Cấu hình biên dịch Typescript (được map vào thư mục src và tests)
```

---

## 🛠 Yêu Cầu Cài Đặt (Prerequisites)

Để chạy được repo này, máy tính của bạn cần được cài sẵn:
- **Node.js** (Khuyên dùng v18.x trở lên)
- **NPM** (Node Package Manager)

---

## 🚀 Hướng Dẫn Sử Dụng

### Bước 1: Cài đặt Dependencies
Mở thư mục gốc của project qua Terminal / Command Prompt và chạy:
```bash
npm install
```

### Bước 2: Chạy kịch bản kiểm thử (Test Scripts)

Hệ thống có 69 kịch bản kiểm thử chi tiết. Vui lòng chạy một trong các lệnh sau tùy mục đích:

- **1. Để chạy bộ kiểm thử toàn diện:**
  ```bash
  npm run test
  ```
  *(Lệnh này khởi động toàn bộ Unit Tests và in ra Logs Pass/Fail tại console cho bạn tiến hành đối chiếu. Lưu ý: có chứa 3 test được thiết kế chủ ý để FAIL)*

- **2. Để bắt các thống kê Code Coverage (Bao phủ mã):**
  ```bash
  npm run test:coverage
  ```
  *(Lệnh này giúp sinh ra báo cáo Coverage dạng văn bản ở Terminal và tạo một thư mục `.coverage` phân tích tỷ lệ bao phủ của Test Set so với tổng line of code (Statement: ~82%))*

- **3. Xuất file Báo Cáo Test Cases:**
  ```bash
  npm run export-report
  ```
  *(Quét thư mục source code và tự động kết xuất ra một tệp Excel `Unit_Testing_Report_Final.xlsx` với 9 cột dữ liệu hoàn chỉnh chuẩn template Report cho đồ án/dự án)*

---

## 💡 Thông Tin Kỹ Thuật (Architecture info)

- **Framework:** `Jest` đóng vai trò Test Runner và thư viện assertion.
- **Transpiler:** `ts-jest` biên dịch `TypeScript` sang `JavaScript` On The Fly.
- **Approach:** 
    - Database Access Layer (Repositories) không được kết nối với MySQL thật.
    - Application Layer (ExamService) được isolate qua cơ chế **Mock Data / Mock Method** chuyên dụng của Jest để tránh hiệu ứng Domino Lỗi và giảm chi phí Database Rollback.

---
*Created by QA team for SQA Graduation Project.*
