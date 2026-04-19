# TOEIC Comment Management - Unit Tests

Repository này chứa Unit Test cho module Quản lý Bình luận (`CommentService`). 
Sử dụng Jest để mock Repository layer và kiểm thử logic.

## Scope of Testing

### ✅ Các thành phần ĐƯỢC kiểm thử (Đã Test)

- **Lớp**: `CommentService`
- **Các hàm**:
  1. `createComment` (Tạo bình luận mới, bắt lỗi rỗng/độ dài/reply sai exam)
  2. `getExamComments` (Lấy bình luận theo bài test + Reply count)
  3. `getCommentThread` (Lấy bình luận và các phản hồi)
  4. `updateComment` (Sửa nội dung, check quyền tác giả)
  5. `deleteComment` (Xóa bình luận, check quyền)
  6. `moderateComment` (Thay đổi trạng thái duyệt)
  7. `getStudentComments` (Lịch sử bình luận user)
  8. `getFlaggedComments` (Danh sách bình luận bị báo cáo)
  9. `searchComments` (Tìm kiếm theo nội dung)
  10. `getExamCommentCount` (Đếm số lượng comment hợp lệ)
  11. `getAllComments` (Tìm và phân trang admin)

**Lý do**: Đây là service layer đảm nhận các logic quan trọng (validation, authorization comment) cần phải dùng Mock Unit Test cô lập để đảm bảo bao phủ mọi case.

---

### ❌ Các thành phần KHÔNG kiểm thử trong Unit Test

- **Lớp**: `CommentRepository`
  - **Lý do**: Thuộc tầng Database. Đã được mock. Nếu test sẽ biến thành Integration Test và cần kết nối DB.
- **Lớp**: `CommentController`
  - **Lý do**: API routing Layer không có bussines logic.
- **DTOs / Entities** (`CreateCommentDto`, `Comment` entity)
  - **Lý do**: Chỉ là Class/Property definitions hoặc Class-validator.

---

## Thực thi

```bash
npm install
npx jest --config jest.config.ts --verbose
node export-report.js
```
