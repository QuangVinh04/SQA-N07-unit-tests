/**
 * =================================================================
 * Unit Test Suite: CommentService - Quản lý Bình luận TOEIC
 * =================================================================
 * 
 * File này chứa tất cả unit tests cho CommentService.
 * CommentService xử lý logic hệ thống bình luận, thảo luận,
 * hỏi đáp giữa các học viên và giáo viên trong từng bài thi.
 * 
 * Testing Framework: Jest + ts-jest
 * Mocking: Jest built-in mock functions
 * 
 * Quy ước đặt tên Test Case ID:
 *   - TC_CMT_CRE_xxx:  Test cases cho createComment
 *   - TC_CMT_GETE_xxx: Test cases cho getExamComments
 *   - TC_CMT_THR_xxx:  Test cases cho getCommentThread
 *   - TC_CMT_UPD_xxx:  Test cases cho updateComment
 *   - TC_CMT_DEL_xxx:  Test cases cho deleteComment
 *   - TC_CMT_MOD_xxx:  Test cases cho moderateComment
 *   - TC_CMT_STU_xxx:  Test cases cho getStudentComments
 *   - TC_CMT_FLG_xxx:  Test cases cho getFlaggedComments
 *   - TC_CMT_SRCH_xxx: Test cases cho searchComments
 *   - TC_CMT_CNT_xxx:  Test cases cho getExamCommentCount
 *   - TC_CMT_ALL_xxx:  Test cases cho getAllComments
 * 
 * @author TrinhHieu - SQA Unit Testing
 */

import { CommentService } from '../src/application/services/comment.service';
import { CommentRepository } from '../src/infrastructure/repositories/comment.repository';

// ====================================================================
// MOCK SETUP
// ====================================================================
jest.mock('../src/infrastructure/repositories/comment.repository');

// ====================================================================
// MOCK DATA
// ====================================================================

const mockStudentProfile = {
  ID: 1,
  user: {
    FullName: 'Nguyen Van A'
  }
};

const mockComment = {
  ID: 1,
  Content: 'Câu này giải thích sao vậy ạ?',
  CreateAt: new Date('2024-05-20T10:00:00Z'),
  ParentId: 0,
  Status: 1,
  StudentProfileID: 1,
  ExamID: 1,
  studentProfile: mockStudentProfile
};

const mockReply = {
  ID: 2,
  Content: 'Bạn xem ngữ pháp về câu điều kiện loại 2 nhé',
  CreateAt: new Date('2024-05-20T10:15:00Z'),
  ParentId: 1,
  Status: 1,
  StudentProfileID: 2,
  ExamID: 1,
  studentProfile: {
    ID: 2,
    user: { FullName: 'Tran Thi B' }
  }
};

describe('CommentService - Quản lý bình luận TOEIC', () => {
  let commentService: CommentService;
  let mockCommentRepo: jest.Mocked<CommentRepository>;

  beforeEach(() => {
    jest.clearAllMocks();
    commentService = new CommentService();
    mockCommentRepo = (CommentRepository as jest.MockedClass<typeof CommentRepository>)
      .mock.instances[0] as jest.Mocked<CommentRepository>;
  });

  // ==================================================================
  // 1. TEST SUITE: createComment 
  // ==================================================================
  describe('createComment - Tạo bình luận/trả lời mới', () => {
    
    /**
     * TC_CMT_CRE_001: Tạo top-level comment thành công
     * 
     * Test Objective: Đảm bảo comment gốc được tạo thành công
     * Input: createDto = { Content: 'Câu này khó quá', ExamID: 1 }, studentProfileId = 1
     * Expected Output: Object Comment chứa thông tin mới
     * Notes: Data hợp lệ, Status mặc định = 1
     */
    it('TC_CMT_CRE_001: Tạo top-level comment thành công', async () => {
      // Arrange
      const createDto = { Content: 'Câu này khó quá', ExamID: 1 };
      (mockCommentRepo.create as jest.Mock).mockResolvedValue(mockComment);

      // Act
      const result = await commentService.createComment(createDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockCommentRepo.create).toHaveBeenCalledWith({
        Content: 'Câu này khó quá',
        ExamID: 1,
        ParentId: 0,
        StudentProfileID: 1,
        Status: 1,
      });
    });

    /**
     * TC_CMT_CRE_002: Thất bại khi content rỗng
     * 
     * Test Objective: Xác minh chặn tạo bình luận nội dung trống
     * Input: createDto = { Content: '   ', ExamID: 1 }
     * Expected Output: Error "Comment cannot be empty"
     * Notes: CỐ TÌNH LÀM FAIL - Message thực tế là "Comment content cannot be empty"
     */
    it('TC_CMT_CRE_002: Thất bại khi content rỗng', async () => {
      // Arrange
      const createDto = { Content: '   ', ExamID: 1 };

      // Act & Assert (Cố tình fail test)
      await expect(commentService.createComment(createDto, 1))
        .rejects.toThrow('Comment cannot be empty');
    });

    /**
     * TC_CMT_CRE_003: Thất bại khi content dài
     * 
     * Test Objective: Xác minh giới hạn độ dài comment (1000 char)
     * Input: createDto = { Content: chuỗi dài 1001 ký tự, ExamID: 1 }
     * Expected Output: Error "Comment content too long (max 1000 characters)"
     * Notes: Ngăn việc comment xả rác database
     */
    it('TC_CMT_CRE_003: Thất bại khi content vượt quá 1000 ký tự', async () => {
      // Arrange
      const createDto = { Content: 'a'.repeat(1001), ExamID: 1 };

      // Act & Assert
      await expect(commentService.createComment(createDto, 1))
        .rejects.toThrow('Comment content too long (max 1000 characters)');
    });

    /**
     * TC_CMT_CRE_004: Thất bại khi parent không tồn tại
     * 
     * Test Objective: Xác nhận reply bị chặn nếu ParentId không đúng
     * Input: ParentId = 99 (id không có)
     * Expected Output: Error "Parent comment not found"
     * Notes: Đảm bảo tính ràng buộc khóa ngoại tầng logic
     */
    it('TC_CMT_CRE_004: Thất bại khi reply parent không tồn tại', async () => {
      // Arrange
      const createDto = { Content: 'Reply', ExamID: 1, ParentId: 99 };
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(commentService.createComment(createDto, 1))
        .rejects.toThrow('Parent comment not found');
    });

    /**
     * TC_CMT_CRE_005: Thất bại khi reply khác exam
     * 
     * Test Objective: Xác nhận không cho phép comment cross-exam
     * Input: createDto.ExamID = 2, parentComment.ExamID = 1
     * Expected Output: Error "Cannot reply to comment from different exam"
     * Notes: Chặn lỗi nghiệp vụ người dùng cố tình cắm cờ bypass API
     */
    it('TC_CMT_CRE_005: Thất bại khi reply khác exam', async () => {
      // Arrange
      const createDto = { Content: 'Reply', ExamID: 2, ParentId: 1 };
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment); // ExamID = 1

      // Act & Assert
      await expect(commentService.createComment(createDto, 1))
        .rejects.toThrow('Cannot reply to comment from different exam');
    });
  });

  // ==================================================================
  // 2. TEST SUITE: getExamComments 
  // ==================================================================
  describe('getExamComments - Lấy comment theo Exam', () => {
    
    /**
     * TC_CMT_GETE_001: Lấy comment thành công
     * 
     * Test Objective: Trả về danh sách comment của bài thi kèm theo số câu reply
     * Input: examId = 1
     * Expected Output: PaginatedCommentsResponseDto có ReplyCount = 2
     * Notes: Test mapping DTO hiển thị Frontend
     */
    it('TC_CMT_GETE_001: Lấy comment thành công', async () => {
      // Arrange
      (mockCommentRepo.findByExamId as jest.Mock).mockResolvedValue({
        comments: [mockComment],
        total: 1
      });
      (mockCommentRepo.getReplyCount as jest.Mock).mockResolvedValue(2);

      // Act
      const result = await commentService.getExamComments(1);

      // Assert
      expect(result.Comments).toHaveLength(1);
      expect(result.Comments[0].ReplyCount).toBe(2);
      expect(result.Pagination.TotalComments).toBe(1);
    });
  });

  // ==================================================================
  // 3. TEST SUITE: getCommentThread 
  // ==================================================================
  describe('getCommentThread - Lấy cả thread', () => {

    /**
     * TC_CMT_THR_001: Lấy thread thành công
     * 
     * Test Objective: Truy xuất cây thư mục thread bình luận
     * Input: commentId = 1
     * Expected Output: Object Comment chứa Thread của nó
     * Notes: Data hợp lệ
     */
    it('TC_CMT_THR_001: Lấy thread thành công', async () => {
      (mockCommentRepo.getCommentThread as jest.Mock).mockResolvedValue(mockComment);
      const result = await commentService.getCommentThread(1);
      expect(result).toBeDefined();
    });

    /**
     * TC_CMT_THR_002: Thất bại khi thread không tồn tại
     * 
     * Test Objective: Trả lỗi nếu id thread gốc không đúng
     * Input: commentId = 99
     * Expected Output: Error "Comment not found"
     * Notes: ID không tồn tại trên CSDL
     */
    it('TC_CMT_THR_002: Thất bại khi thread không tồn tại', async () => {
      (mockCommentRepo.getCommentThread as jest.Mock).mockResolvedValue(null);
      await expect(commentService.getCommentThread(99))
        .rejects.toThrow('Comment not found');
    });
  });

  // ==================================================================
  // 4. TEST SUITE: updateComment 
  // ==================================================================
  describe('updateComment - Sửa comment', () => {

    /**
     * TC_CMT_UPD_001: Sửa comment thành công
     * 
     * Test Objective: Cập nhật nội dung cho bình luận có sẵn
     * Input: commentId = 1, updateData = {Content: 'New'}, authorId = 1
     * Expected Output: Bình luận với content mới
     * Notes: Thực thi hợp lệ đúng tác giả
     */
    it('TC_CMT_UPD_001: Sửa comment thành công', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment);
      (mockCommentRepo.update as jest.Mock).mockResolvedValue({ ...mockComment, Content: 'New' });

      const result = await commentService.updateComment(1, { Content: 'New' }, 1);
      expect(result.Content).toBe('New');
    });

    /**
     * TC_CMT_UPD_002: Thất bại comment không tồn tại
     * 
     * Test Objective: Báo lỗi khi Cập nhật comment ko có thực
     * Input: commentId = 99
     * Expected Output: Error "Comment not found"
     * Notes: Validation comment tồn tại
     */
    it('TC_CMT_UPD_002: Thất bại khi comment không tồn tại', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(null);
      await expect(commentService.updateComment(99, { Content: 'New' }, 1))
        .rejects.toThrow('Comment not found');
    });

    /**
     * TC_CMT_UPD_003: Thất bại không phải tác giả
     * 
     * Test Objective: Kiểm tra validation - người khác sửa comment
     * Input: commentId = 1 (của User 1), userId Request = 2
     * Expected Output: Error "Not authorized to edit"
     * Notes: CỐ TÌNH LÀM FAIL - Message thực tế là "You can only edit your own comments"
     */
    it('TC_CMT_UPD_003: Thất bại khi không phải tác giả (Cố tình fail)', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment); // Author = 1

      // Cố tình fail test
      await expect(commentService.updateComment(1, { Content: 'New' }, 2))
        .rejects.toThrow('Not authorized to edit');
    });
    
    /**
     * TC_CMT_UPD_004: Thất bại nội dung rỗng
     * 
     * Test Objective: Tránh xóa content bình luận thành whitespace
     * Input: commentId = 1, Content = "  "
     * Expected Output: Error "Comment content cannot be empty"
     * Notes: Trim input chống trick của học sinh
     */
    it('TC_CMT_UPD_004: Thất bại khi content rỗng', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment);
      await expect(commentService.updateComment(1, { Content: '  ' }, 1))
        .rejects.toThrow('Comment content cannot be empty');
    });
  });

  // ==================================================================
  // 5. TEST SUITE: deleteComment 
  // ==================================================================
  describe('deleteComment - Xóa comment', () => {

    /**
     * TC_CMT_DEL_001: Tác giả xóa thành công
     * 
     * Test Objective: Đảm bảo tác giả xóa được comment của mình
     * Input: commentId = 1, studentId = 1
     * Expected Output: boolean Result true
     * Notes: Admin pass false default
     */
    it('TC_CMT_DEL_001: Tác giả xóa thành công', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment);
      (mockCommentRepo.getReplyCount as jest.Mock).mockResolvedValue(0);
      (mockCommentRepo.delete as jest.Mock).mockResolvedValue(true);

      const result = await commentService.deleteComment(1, 1);
      expect(result).toBe(true);
    });

    /**
     * TC_CMT_DEL_002: Admin xóa thành công
     * 
     * Test Objective: Đảm bảo quyền admin override quyền user để xóa
     * Input: commentId = 1 (của user 1), userId request = 99, isAdmin = true
     * Expected Output: boolean Result true
     * Notes: Role-based delete check
     */
    it('TC_CMT_DEL_002: Admin xóa thành công', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment); // author = 1
      (mockCommentRepo.getReplyCount as jest.Mock).mockResolvedValue(0);
      (mockCommentRepo.delete as jest.Mock).mockResolvedValue(true);

      const result = await commentService.deleteComment(1, 99, true); // isAdmin = true
      expect(result).toBe(true);
    });

    /**
     * TC_CMT_DEL_003: Xóa thất bại khi thiếu quyền
     * 
     * Test Objective: Chặn user khác xóa comment của nhau
     * Input: commentId = 1 (của user 1), userId Request = 2
     * Expected Output: Error "Not authorized to delete"
     * Notes: CỐ TÌNH LÀM FAIL - Message thực là "You can only delete your own comments"
     */
    it('TC_CMT_DEL_003: Thất bại khi không phải tác giả và ko phải admin (Cố tình fail)', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment);

      await expect(commentService.deleteComment(1, 2))
        .rejects.toThrow('Not authorized to delete');
    });
  });

  // ==================================================================
  // 6. TEST SUITE: moderateComment 
  // ==================================================================
  describe('moderateComment - Kiểm duyệt comment', () => {

    /**
     * TC_CMT_MOD_001: Cập nhật thuộc tính status
     * 
     * Test Objective: Đổi trạng thái hiển thị của comment sang ban (2)
     * Input: commentId = 1, modData = { Status: 2 }
     * Expected Output: Trả về Comment đã update Status
     * Notes: Tool duyệt riêng dành cho giáo viên/admin
     */
    it('TC_CMT_MOD_001: Moderate thành công', async () => {
      (mockCommentRepo.findById as jest.Mock).mockResolvedValue(mockComment);
      (mockCommentRepo.updateStatus as jest.Mock).mockResolvedValue({ ...mockComment, Status: 2 });

      const result = await commentService.moderateComment(1, { Status: 2 });
      expect(result.Status).toBe(2);
    });
  });

  // ==================================================================
  // 7. TEST SUITE: getStudentComments
  // ==================================================================
  describe('getStudentComments - Lấy DS comment của học viên', () => {

    /**
     * TC_CMT_STU_001: Lấy list comment của profile hs
     * 
     * Test Objective: Giúp tra cứu history comment của học viên
     * Input: studentId = 1, limit = 10
     * Expected Output: Danh sách comment của đúng ID đã mock
     * Notes: View trong trang Student dashboard
     */
    it('TC_CMT_STU_001: Lấy ds thành công', async () => {
      (mockCommentRepo.findByStudentId as jest.Mock).mockResolvedValue([mockComment]);
      const result = await commentService.getStudentComments(1, 10);
      expect(result).toHaveLength(1);
    });
  });

  // ==================================================================
  // 8. TEST SUITE: getFlaggedComments
  // ==================================================================
  describe('getFlaggedComments - Lấy cmt bị cắm cờ', () => {

    /**
     * TC_CMT_FLG_001: Report system comment
     * 
     * Test Objective: Chỉ lấy các comment có status cắm cờ để mod duyệt
     * Input: không có input params
     * Expected Output: Mảng comments bị banned 
     * Notes: Query dành cho Admin Dashboard
     */
    it('TC_CMT_FLG_001: Lấy list flagged thành công', async () => {
      (mockCommentRepo.getFlaggedComments as jest.Mock).mockResolvedValue([mockComment]);
      const result = await commentService.getFlaggedComments();
      expect(result).toHaveLength(1);
    });
  });

  // ==================================================================
  // 9. TEST SUITE: searchComments
  // ==================================================================
  describe('searchComments - Tìm kiếm', () => {

    /**
     * TC_CMT_SRCH_001: String match
     * 
     * Test Objective: Tìm các comment theo keyword
     * Input: text = "hello", examId = 1
     * Expected Output: mảng kết quả mock
     * Notes: Thường dùng search filter Admin
     */
    it('TC_CMT_SRCH_001: Search text thành công', async () => {
      (mockCommentRepo.searchComments as jest.Mock).mockResolvedValue([mockComment]);
      const result = await commentService.searchComments('hello', 1);
      expect(result).toHaveLength(1);
    });

    /**
     * TC_CMT_SRCH_002: Lỗi rỗng query
     * 
     * Test Objective: Không search text khoảng trắng
     * Input: text = "  "
     * Expected Output: Exception "Search text cannot be empty"
     * Notes: Limit bug search rỗng fulltable
     */
    it('TC_CMT_SRCH_002: Reject text rỗng', async () => {
      await expect(commentService.searchComments('  '))
        .rejects.toThrow('Search text cannot be empty');
    });
  });

  // ==================================================================
  // 10. TEST SUITE: getExamCommentCount
  // ==================================================================
  describe('getExamCommentCount', () => {

    /**
     * TC_CMT_CNT_001: Khảo sát Count
     * 
     * Test Objective: Đếm tổng record của bài test trên Repository
     * Input: ExamID = 1
     * Expected Output: Trả về số mock value
     * Notes: Fast query SQL Count index
     */
    it('TC_CMT_CNT_001: Đếm số lượng', async () => {
      (mockCommentRepo.getCountByExamId as jest.Mock).mockResolvedValue(5);
      const result = await commentService.getExamCommentCount(1);
      expect(result).toBe(5);
    });
  });

  // ==================================================================
  // 11. TEST SUITE: getAllComments
  // ==================================================================
  describe('getAllComments', () => {

    /**
     * TC_CMT_ALL_001: GetAll with Page
     * 
     * Test Objective: Fetch comment bằng offset + limit chuẩn xác
     * Input: Pagination option page=1, limit=10
     * Expected Output: Paging Object kèm danh sách Comment Mock
     * Notes: Quản lý all DB
     */
    it('TC_CMT_ALL_001: Lấy tất cả comment', async () => {
      (mockCommentRepo.findAll as jest.Mock).mockResolvedValue({
        comments: [mockComment],
        total: 1
      });
      (mockCommentRepo.getReplyCount as jest.Mock).mockResolvedValue(0);

      const result = await commentService.getAllComments({ page: 1, limit: 10 });
      expect(result.pagination.total).toBe(1);
      expect(result.comments).toHaveLength(1);
    });

    /**
     * TC_CMT_ALL_002: Check min Page
     * 
     * Test Objective: Dừng fetch nếu trang số lẻ (<=0)
     * Input: page = 0
     * Expected Output: Error "Page must be greater than 0"
     * Notes: Validate input pagination
     */
    it('TC_CMT_ALL_002: Lỗi page <= 0', async () => {
      await expect(commentService.getAllComments({ page: 0, limit: 10 }))
        .rejects.toThrow('Page must be greater than 0');
    });
  });
});
