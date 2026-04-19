# TOEIC Question Bank Management - Unit Tests

Đây là một Repository chuyên biệt (isolated repository) được tách ra phục vụ mục đích kiểm thử (Unit Testing) dành riêng cho module **Quản lý Ngân hàng câu hỏi** (QuestionService) trong dự án TOEIC.

Mục đích của việc tách repository là để thực thi quy trình TDD (Test-Driven Development) và SQA (Software Quality Assurance) độc lập, không làm phình to core logic của hệ thống.

---

## 📂 Cấu Trúc Dự Án

```text
question-bank-unit-tests/
├── src/                    # Source code từ Core Backend (dùng để Mock Input)
│   ├── application/        # Services, DTOs
│   ├── domain/             # Entities (Question, Choice, MediaQuestion)
│   └── infrastructure/     # Repositories (QuestionRepository)
├── tests/                  # Thư mục chứa Test Suite
│   └── question.service.test.ts   # 45 test cases cho QuestionService
├── export-report.js        # Script tự động xuất báo cáo Test ra Excel
├── jest.config.ts          # Cấu hình Jest runner
├── package.json            # Dependencies cho testing
└── tsconfig.json           # Cấu hình TypeScript
```

---

## 🛠 Yêu Cầu Cài Đặt

- **Node.js** (v18.x trở lên)
- **NPM** (Node Package Manager)

---

## 🚀 Hướng Dẫn Sử Dụng

### Bước 1: Cài đặt Dependencies
```bash
npm install
```

### Bước 2: Chạy kịch bản kiểm thử

- **Chạy toàn bộ Unit Tests:**
  ```bash
  npm run test
  ```

- **Chạy với Code Coverage:**
  ```bash
  npm run test:coverage
  ```
  *(Sinh ra báo cáo coverage tại thư mục `coverage/`)*

- **Xuất báo cáo Excel:**
  ```bash
  npm run export-report
  ```
  *(Tạo file `Unit_Testing_Report_QuestionService.xlsx`)*

---

## 📊 Phạm Vi Kiểm Thử (Scope)

### Các phương thức ĐƯỢC kiểm thử:

| # | Phương thức | Số Test Cases | Mô tả |
|---|---|---|---|
| 1 | `createQuestion` | 14 | Tạo câu hỏi mới với media và choices |
| 2 | `getQuestionById` | 2 | Lấy chi tiết câu hỏi theo ID |
| 3 | `searchQuestions` | 6 | Tìm kiếm/lọc câu hỏi với pagination |
| 4 | `updateQuestion` | 6 | Cập nhật câu hỏi (text, media, choices) |
| 5 | `deleteQuestion` | 4 | Xóa câu hỏi (kiểm tra usage) |
| 6 | `getQuestionStatistics` | 2 | Lấy thống kê sử dụng |
| 7 | `getQuestionsBySection` | 4 | Lấy câu hỏi theo Part cho luyện tập |
| 8 | `performBulkOperation` | 4 | Thao tác hàng loạt (DELETE, ADD_TO_EXAM) |
| 9 | `validateChoices` (private) | 2 | Kiểm tra gián tiếp qua createQuestion |
| 10 | `validateMediaRequirements` (private) | 2 | Kiểm tra gián tiếp qua createQuestion |
| 11 | `isValidUrl` (private) | 4 | URL format validation |

### Các thành phần KHÔNG kiểm thử:

| Thành phần | Lý do |
|---|---|
| `QuestionRepository` | Là Data Access Layer, truy cập DB trực tiếp - được mock hoàn toàn |
| `QuestionController` | Thuộc Presentation Layer, chỉ chứa logic routing |
| Entities (Question, Choice, MediaQuestion) | Là data models thuần túy, không có logic |
| DTOs | Chỉ chứa validation decorators, không có business logic |

---

## 💡 Thông Tin Kỹ Thuật

- **Framework:** `Jest` v29 - Test Runner và Assertion Library
- **Transpiler:** `ts-jest` - Biên dịch TypeScript on-the-fly
- **Approach:** Mock-based Unit Testing
  - QuestionRepository được mock hoàn toàn
  - Không kết nối database thực
  - Isolate business logic trong QuestionService
- **Coverage Target:** `question.service.ts` (~80%+ line coverage)

---

## 📝 Quy ước đặt tên Test Case ID

```
TC_QS_{METHOD}_{NUMBER}

TC    = Test Case
QS    = QuestionService
METHOD = Mã viết tắt phương thức
NUMBER = Số thứ tự (001, 002, ...)
```

| Prefix | Phương thức |
|---|---|
| CRE | createQuestion |
| GET | getQuestionById |
| SRCH | searchQuestions |
| UPD | updateQuestion |
| DEL | deleteQuestion |
| STAT | getQuestionStatistics |
| SEC | getQuestionsBySection |
| BULK | performBulkOperation |
| VC | validateChoices |
| VM | validateMediaRequirements |
| URL | isValidUrl |

---
*Created by QA team for SQA Graduation Project - Quản lý Ngân hàng câu hỏi TOEIC.*
