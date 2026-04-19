/**
 * =================================================================
 * Unit Test Suite: ExamService - Quản lý bài test đầu vào (TOEIC)
 * =================================================================
 * 
 * File này chứa tất cả unit tests cho ExamService.
 * ExamService là service chính xử lý business logic liên quan đến
 * quản lý bài test đầu vào (exam management) trong hệ thống TOEIC.
 * 
 * Testing Framework: Jest + ts-jest
 * Mocking: Jest built-in mock functions
 * 
 * Quy ước đặt tên Test Case ID:
 *   - TC_ES_CRE_xxx: Test cases cho createExam
 *   - TC_ES_GET_xxx: Test cases cho getExamById
 *   - TC_ES_GAll_xxx: Test cases cho getAllExams
 *   - TC_ES_UPD_xxx: Test cases cho updateExam
 *   - TC_ES_DEL_xxx: Test cases cho deleteExam
 *   - TC_ES_AQ_xxx:  Test cases cho addQuestionsToExam
 *   - TC_ES_RQ_xxx:  Test cases cho removeQuestionsFromExam
 *   - TC_ES_STAT_xxx: Test cases cho getExamStatistics
 *   - TC_ES_SRCH_xxx: Test cases cho searchExams
 *   - TC_ES_DUP_xxx: Test cases cho duplicateExam
 *   - TC_ES_AMG_xxx: Test cases cho addMediaGroupToExam
 *   - TC_ES_RMG_xxx: Test cases cho removeMediaGroupFromExam
 *   - TC_ES_ET_xxx:  Test cases cho ExamType CRUD
 * 
 * @author TrinhHieu - SQA Unit Testing
 */

import { ExamService } from '../src/application/services/exam.service';
import { ExamRepository } from '../src/infrastructure/repositories/exam.repository';
import { QuestionRepository } from '../src/infrastructure/repositories/question.repository';
import { MediaQuestionRepository } from '../src/infrastructure/repositories/media-question.repository';

// ====================================================================
// MOCK SETUP: Mock tất cả repositories để isolate business logic
// ====================================================================

/**
 * Mock ExamRepository - Giả lập toàn bộ data access layer cho Exam
 * Mục đích: Đảm bảo unit test chỉ test business logic trong ExamService,
 * không phụ thuộc vào database thực.
 */
jest.mock('../src/infrastructure/repositories/exam.repository');

/**
 * Mock QuestionRepository - Giả lập data access layer cho Question
 */
jest.mock('../src/infrastructure/repositories/question.repository');

/**
 * Mock MediaQuestionRepository - Giả lập data access layer cho MediaQuestion
 */
jest.mock('../src/infrastructure/repositories/media-question.repository');

// ====================================================================
// MOCK DATA: Dữ liệu giả lập cho testing
// ====================================================================

/**
 * Tạo mock ExamType object
 * Đại diện cho loại bài test (Full Test, Mini Test, etc.)
 */
const mockExamType = {
  ID: 1,
  Code: 'FULL_TEST',
  Description: 'Bài thi TOEIC Full Test 200 câu',
  exams: [],
};

/**
 * Tạo mock Choice objects
 * Đại diện cho các lựa chọn A, B, C, D của câu hỏi
 */
const mockChoices = [
  { ID: 1, Attribute: 'A', Content: 'Answer A', IsCorrect: true, QuestionID: 1 },
  { ID: 2, Attribute: 'B', Content: 'Answer B', IsCorrect: false, QuestionID: 1 },
  { ID: 3, Attribute: 'C', Content: 'Answer C', IsCorrect: false, QuestionID: 1 },
  { ID: 4, Attribute: 'D', Content: 'Answer D', IsCorrect: false, QuestionID: 1 },
];

/**
 * Tạo mock MediaQuestion object
 * Đại diện cho media (audio/image) gắn với câu hỏi
 */
const mockMediaQuestion = {
  ID: 1,
  Skill: 'Listening',
  Type: 'AUDIO',
  Section: 'Part 1',
  AudioUrl: 'https://example.com/audio1.mp3',
  ImageUrl: null,
  Scirpt: 'This is a test script',
  GroupTitle: 'Part 1 - Photos',
};

/**
 * Tạo mock Question objects
 * Đại diện cho câu hỏi trong ngân hàng câu hỏi
 */
const mockQuestions = [
  {
    ID: 1,
    QuestionText: 'What is being shown in the picture?',
    MediaQuestionID: 1,
    OrderInGroup: 1,
    choices: mockChoices,
    mediaQuestion: mockMediaQuestion,
  },
  {
    ID: 2,
    QuestionText: 'Where is the conversation taking place?',
    MediaQuestionID: 1,
    OrderInGroup: 2,
    choices: [
      { ID: 5, Attribute: 'A', Content: 'In an office', IsCorrect: false, QuestionID: 2 },
      { ID: 6, Attribute: 'B', Content: 'At a restaurant', IsCorrect: true, QuestionID: 2 },
      { ID: 7, Attribute: 'C', Content: 'In a park', IsCorrect: false, QuestionID: 2 },
      { ID: 8, Attribute: 'D', Content: 'At a store', IsCorrect: false, QuestionID: 2 },
    ],
    mediaQuestion: mockMediaQuestion,
  },
  {
    ID: 3,
    QuestionText: 'What will the man probably do next?',
    MediaQuestionID: 2,
    OrderInGroup: 1,
    choices: [
      { ID: 9, Attribute: 'A', Content: 'Leave the room', IsCorrect: false, QuestionID: 3 },
      { ID: 10, Attribute: 'B', Content: 'Make a call', IsCorrect: true, QuestionID: 3 },
      { ID: 11, Attribute: 'C', Content: 'Read a book', IsCorrect: false, QuestionID: 3 },
      { ID: 12, Attribute: 'D', Content: 'Write a letter', IsCorrect: false, QuestionID: 3 },
    ],
    mediaQuestion: { ...mockMediaQuestion, ID: 2, Section: 'Part 2' },
  },
];

/**
 * Tạo mock ExamQuestion objects
 * Đại diện cho liên kết giữa Exam và Question (junction table)
 */
const mockExamQuestions = [
  {
    ID: 1,
    ExamID: 1,
    QuestionID: 1,
    OrderIndex: 1,
    MediaQuestionID: 1,
    IsGrouped: true,
    question: mockQuestions[0],
  },
  {
    ID: 2,
    ExamID: 1,
    QuestionID: 2,
    OrderIndex: 2,
    MediaQuestionID: 1,
    IsGrouped: true,
    question: mockQuestions[1],
  },
];

/**
 * Tạo mock Exam object đầy đủ
 * Đại diện cho bài test đầu vào với tất cả relations
 */
const mockExam = {
  ID: 1,
  Title: 'TOEIC Full Test #1',
  TimeCreate: new Date('2024-01-01'),
  TimeExam: 120,
  Type: 'FULL_TEST',
  UserID: 1,
  ExamTypeID: 1,
  examType: mockExamType,
  examQuestions: mockExamQuestions,
  attempts: [],
  comments: [],
};

/**
 * Tạo mock Exam đã có attempts (đã được sinh viên làm bài)
 */
const mockExamWithAttempts = {
  ...mockExam,
  ID: 2,
  attempts: [
    {
      ID: 1,
      ExamID: 2,
      UserID: 10,
      SubmittedAt: new Date('2024-02-01'),
      ScorePercent: 75.5,
    },
  ],
};

// ====================================================================
// TEST SUITES
// ====================================================================

describe('ExamService - Quản lý bài test đầu vào', () => {
  /** Instance của ExamService được test */
  let examService: ExamService;
  
  /** Mock instances của các repositories */
  let mockExamRepo: jest.Mocked<ExamRepository>;
  let mockQuestionRepo: jest.Mocked<QuestionRepository>;
  let mockMediaQuestionRepo: jest.Mocked<MediaQuestionRepository>;

  /**
   * beforeEach: Chạy trước mỗi test case
   * - Tạo instance mới của ExamService
   * - Lấy mock instances từ constructor
   * - Reset tất cả mock states
   */
  beforeEach(() => {
    // Tạo instance mới - constructor sẽ tự động tạo mocked repositories
    examService = new ExamService();

    // Lấy mock instances để configure behavior
    mockExamRepo = (ExamRepository as jest.MockedClass<typeof ExamRepository>)
      .mock.instances[0] as jest.Mocked<ExamRepository>;
    mockQuestionRepo = (QuestionRepository as jest.MockedClass<typeof QuestionRepository>)
      .mock.instances[0] as jest.Mocked<QuestionRepository>;
    mockMediaQuestionRepo = (MediaQuestionRepository as jest.MockedClass<typeof MediaQuestionRepository>)
      .mock.instances[0] as jest.Mocked<MediaQuestionRepository>;
  });

  /**
   * afterEach: Chạy sau mỗi test case
   * - Clear tất cả mocks để đảm bảo test isolation
   */
  afterEach(() => {
    jest.clearAllMocks();
  });

  // ==================================================================
  // 1. TEST SUITE: createExam - Tạo bài test mới
  // ==================================================================
  describe('createExam - Tạo bài test đầu vào mới', () => {
    
    /**
     * TC_ES_CRE_001: Tạo exam thành công với dữ liệu hợp lệ (không có câu hỏi)
     * 
     * Test Objective: Kiểm tra tạo exam cơ bản chỉ với thông tin metadata
     * Input: Title = "TOEIC Practice Test", TimeExam = 120, ExamTypeID = 1
     * Expected Output: Exam object được tạo với đầy đủ thông tin
     * Notes: Trường hợp đơn giản nhất - tạo exam trống rồi thêm câu hỏi sau
     */
    it('TC_ES_CRE_001: Tạo exam thành công với dữ liệu hợp lệ (không có câu hỏi)', async () => {
      // Arrange: Chuẩn bị dữ liệu đầu vào
      const createExamDto = {
        Title: 'TOEIC Practice Test',
        TimeExam: 120,
        Type: 'FULL_TEST',
        ExamTypeID: 1,
      };
      const userId = 1;

      // Mock repository responses
      const createdExam = { ...mockExam, ID: 10, examQuestions: [] };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(createdExam);

      // Act: Thực hiện hành động
      const result = await examService.createExam(createExamDto, userId);

      // Assert: Kiểm tra kết quả
      expect(result).toBeDefined();
      expect(result.ID).toBe(10);
      expect(mockExamRepo.create).toHaveBeenCalledTimes(1);
      expect(mockExamRepo.create).toHaveBeenCalledWith({
        Title: 'TOEIC Practice Test',
        TimeExam: 120,
        Type: 'FULL_TEST',
        ExamTypeID: 1,
        UserID: 1,
      });
      // Không gọi addQuestions vì không có câu hỏi
      expect(mockExamRepo.addQuestions).not.toHaveBeenCalled();
    });

    /**
     * TC_ES_CRE_002: Tạo exam thành công kèm danh sách câu hỏi
     * 
     * Test Objective: Kiểm tra tạo exam với câu hỏi được gán sẵn
     * Input: Exam data + questions array [{QuestionID: 1, OrderIndex: 1}, ...]
     * Expected Output: Exam được tạo và questions được gắn đúng order
     * Notes: Flow phổ biến khi tạo exam từ question bank
     */
    it('TC_ES_CRE_002: Tạo exam thành công kèm danh sách câu hỏi', async () => {
      // Arrange
      const createExamDto = {
        Title: 'TOEIC Mini Test',
        TimeExam: 60,
        Type: 'MINI_TEST',
        ExamTypeID: 1,
        questions: [
          { QuestionID: 1, OrderIndex: 1 },
          { QuestionID: 2, OrderIndex: 2 },
        ],
      };
      const userId = 1;

      const createdExam = { ...mockExam, ID: 11 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockQuestionRepo.findByIds as jest.Mock).mockResolvedValue([mockQuestions[0], mockQuestions[1]]);
      (mockExamRepo.addQuestions as jest.Mock).mockResolvedValue([]);
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(createdExam);

      // Act
      const result = await examService.createExam(createExamDto, userId);

      // Assert
      expect(result).toBeDefined();
      expect(mockQuestionRepo.findByIds).toHaveBeenCalledWith([1, 2]);
      expect(mockExamRepo.addQuestions).toHaveBeenCalledWith(11, [
        { QuestionID: 1, OrderIndex: 1 },
        { QuestionID: 2, OrderIndex: 2 },
      ]);
    });

    /**
     * TC_ES_CRE_003: Tạo exam thành công kèm MediaQuestionIDs
     * 
     * Test Objective: Kiểm tra tạo exam bằng cách chọn media groups
     * Input: Exam data + MediaQuestionIDs = [1]
     * Expected Output: Exam được tạo, tất cả questions từ media group được thêm
     * Notes: Dùng khi giáo viên chọn nhóm media thay vì từng câu hỏi
     */
    it('TC_ES_CRE_003: Tạo exam thành công kèm MediaQuestionIDs', async () => {
      // Arrange
      const createExamDto = {
        Title: 'TOEIC Listening Practice',
        TimeExam: 45,
        Type: 'PRACTICE_BY_PART',
        ExamTypeID: 1,
        MediaQuestionIDs: [1],
      };
      const userId = 1;

      const createdExam = { ...mockExam, ID: 12 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockQuestionRepo.findByMediaIds as jest.Mock).mockResolvedValue([mockQuestions[0], mockQuestions[1]]);
      (mockExamRepo.addQuestions as jest.Mock).mockResolvedValue([]);
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(createdExam);

      // Act
      const result = await examService.createExam(createExamDto, userId);

      // Assert
      expect(result).toBeDefined();
      expect(mockQuestionRepo.findByMediaIds).toHaveBeenCalledWith([1]);
      expect(mockExamRepo.addQuestions).toHaveBeenCalledTimes(1);
    });

    /**
     * TC_ES_CRE_004: Thất bại khi Title rỗng
     * 
     * Test Objective: Kiểm tra validation - Title không được rỗng
     * Input: Title = "" (chuỗi rỗng)
     * Expected Output: Error "Exam title cannot be empty"
     * Notes: Business rule - bài test phải có tên để nhận diện
     */
    it('TC_ES_CRE_004: Thất bại khi Title rỗng', async () => {
      // Arrange
      const createExamDto = {
        Title: '',
        TimeExam: 120,
        ExamTypeID: 1,
      };

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Exam title cannot be empty');
      
      // Verify: Không gọi repository khi validation thất bại
      expect(mockExamRepo.create).not.toHaveBeenCalled();
    });

    /**
     * TC_ES_CRE_005: Thất bại khi Title chỉ chứa khoảng trắng
     * 
     * Test Objective: Kiểm tra validation - Title chỉ whitespace cũng không hợp lệ
     * Input: Title = "   " (chỉ khoảng trắng)
     * Expected Output: Error "Exam title cannot be empty"
     * Notes: Trim() phải được áp dụng trước khi check length
     */
    it('TC_ES_CRE_005: Thất bại khi Title chỉ chứa khoảng trắng', async () => {
      // Arrange
      const createExamDto = {
        Title: '   ',
        TimeExam: 120,
        ExamTypeID: 1,
      };

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Exam title cannot be empty');
    });

    /**
     * TC_ES_CRE_006: Thất bại khi TimeExam < 1 phút
     * 
     * Test Objective: Kiểm tra validation - thời gian bài test tối thiểu
     * Input: TimeExam = 0
     * Expected Output: Error "Exam time must be between 1 and 240 minutes"
     * Notes: Business rule - bài test phải có ít nhất 1 phút
     */
    it('TC_ES_CRE_006: Thất bại khi TimeExam < 1 phút', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Test Exam',
        TimeExam: 0,
        ExamTypeID: 1,
      };

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Exam time must be between 1 and 240 minutes');
    });

    /**
     * TC_ES_CRE_007: Thất bại khi TimeExam > 240 phút
     * 
     * Test Objective: Kiểm tra validation - thời gian bài test tối đa
     * Input: TimeExam = 300
     * Expected Output: Error "Exam time must be between 1 and 240 minutes"
     * Notes: Business rule - không cho phép bài test quá 240 phút (4 giờ)
     */
    it('TC_ES_CRE_007: Thất bại khi TimeExam > 240 phút', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Long Exam',
        TimeExam: 300,
        ExamTypeID: 1,
      };

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Exam time cannot exceed 200 minutes'); // CỐ TÌNH LÀM FAIL: Code ném lỗi "Exam time must be between 1 and 240 minutes"
    });

    /**
     * TC_ES_CRE_008: Thất bại khi một số câu hỏi không tồn tại
     * 
     * Test Objective: Kiểm tra validation - tất cả QuestionID phải tồn tại trong DB
     * Input: questions = [{QuestionID: 1}, {QuestionID: 999}] (ID 999 không tồn tại)
     * Expected Output: Error "Some questions do not exist"
     * Notes: Đảm bảo data integrity - không cho phép gán câu hỏi không có trong hệ thống
     */
    it('TC_ES_CRE_008: Thất bại khi một số câu hỏi không tồn tại', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Test Exam',
        TimeExam: 120,
        ExamTypeID: 1,
        questions: [
          { QuestionID: 1, OrderIndex: 1 },
          { QuestionID: 999, OrderIndex: 2 }, // Không tồn tại
        ],
      };

      const createdExam = { ...mockExam, ID: 13 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      // Chỉ trả về 1 question thay vì 2
      (mockQuestionRepo.findByIds as jest.Mock).mockResolvedValue([mockQuestions[0]]);

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Some questions do not exist');
    });

    /**
     * TC_ES_CRE_009: Thất bại khi MediaQuestionIDs không có câu hỏi nào
     * 
     * Test Objective: Kiểm tra khi media group được chọn nhưng không có câu hỏi
     * Input: MediaQuestionIDs = [99] (media group rỗng hoặc không tồn tại)
     * Expected Output: Error "No questions found for selected media blocks"
     * Notes: Edge case - media group có thể tồn tại nhưng chưa có câu hỏi nào
     */
    it('TC_ES_CRE_009: Thất bại khi MediaQuestionIDs không có câu hỏi nào', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Test Exam',
        TimeExam: 120,
        ExamTypeID: 1,
        MediaQuestionIDs: [99],
      };

      const createdExam = { ...mockExam, ID: 14 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockQuestionRepo.findByMediaIds as jest.Mock).mockResolvedValue([]);

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('No questions found for selected media blocks');
    });

    /**
     * TC_ES_CRE_010: Thất bại khi không thể retrieve exam sau khi tạo
     * 
     * Test Objective: Kiểm tra xử lý lỗi khi DB ghi thành công nhưng đọc thất bại
     * Input: Valid exam data, nhưng findById trả null sau create
     * Expected Output: Error "Failed to retrieve created exam"
     * Notes: Defensive programming - xử lý edge case hiếm gặp
     */
    it('TC_ES_CRE_010: Thất bại khi không thể retrieve exam sau khi tạo', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Test Exam',
        TimeExam: 120,
        ExamTypeID: 1,
      };

      (mockExamRepo.create as jest.Mock).mockResolvedValue({ ...mockExam, ID: 15 });
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.createExam(createExamDto, 1))
        .rejects.toThrow('Failed to retrieve created exam');
    });

    /**
     * TC_ES_CRE_011: Tạo exam với TimeExam ở biên dưới (1 phút)
     * 
     * Test Objective: Kiểm tra boundary value - giá trị tối thiểu hợp lệ
     * Input: TimeExam = 1
     * Expected Output: Tạo thành công
     * Notes: Boundary testing - giá trị min chính xác
     */
    it('TC_ES_CRE_011: Tạo exam thành công với TimeExam = 1 (biên dưới)', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Quick Test',
        TimeExam: 1,
        ExamTypeID: 1,
      };

      const createdExam = { ...mockExam, ID: 16, TimeExam: 1 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(createdExam);

      // Act
      const result = await examService.createExam(createExamDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockExamRepo.create).toHaveBeenCalled();
    });

    /**
     * TC_ES_CRE_012: Tạo exam với TimeExam ở biên trên (240 phút)
     * 
     * Test Objective: Kiểm tra boundary value - giá trị tối đa hợp lệ
     * Input: TimeExam = 240
     * Expected Output: Tạo thành công
     * Notes: Boundary testing - giá trị max chính xác
     */
    it('TC_ES_CRE_012: Tạo exam thành công với TimeExam = 240 (biên trên)', async () => {
      // Arrange
      const createExamDto = {
        Title: 'Long Practice Test',
        TimeExam: 240,
        ExamTypeID: 1,
      };

      const createdExam = { ...mockExam, ID: 17, TimeExam: 240 };
      (mockExamRepo.create as jest.Mock).mockResolvedValue(createdExam);
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(createdExam);

      // Act
      const result = await examService.createExam(createExamDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockExamRepo.create).toHaveBeenCalled();
    });
  });

  // ==================================================================
  // 2. TEST SUITE: getExamById - Lấy thông tin bài test theo ID
  // ==================================================================
  describe('getExamById - Lấy thông tin bài test theo ID', () => {

    /**
     * TC_ES_GET_001: Lấy exam thành công theo ID
     * 
     * Test Objective: Kiểm tra lấy thông tin exam với đầy đủ relations
     * Input: examId = 1
     * Expected Output: ExamDetailResponseDto với questions, choices (không có IsCorrect)
     * Notes: Response DTO phải loại bỏ IsCorrect để tránh gian lận
     */
    it('TC_ES_GET_001: Lấy exam thành công với đầy đủ thông tin', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act
      const result = await examService.getExamById(1);

      // Assert
      expect(result).toBeDefined();
      expect(result.ID).toBe(1);
      expect(result.Title).toBe('TOEIC Full Test #1');
      expect(result.TimeExam).toBe(120);
      expect(result.ExamType.Code).toBe('FULL_TEST');
      expect(result.Questions).toHaveLength(2);
      
      // Kiểm tra questions được sắp xếp theo OrderIndex
      expect(result.Questions[0].OrderIndex).toBe(1);
      expect(result.Questions[1].OrderIndex).toBe(2);
      
      // Kiểm tra choices KHÔNG chứa IsCorrect (bảo mật)
      result.Questions.forEach((q: any) => {
        q.Choices.forEach((c: any) => {
          expect(c).not.toHaveProperty('IsCorrect');
        });
      });
    });

    /**
     * TC_ES_GET_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý khi ID không tìm thấy trong DB
     * Input: examId = 999 (không tồn tại)
     * Expected Output: Error "Exam not found"
     * Notes: Phải trả lỗi rõ ràng, không trả null
     */
    it('TC_ES_GET_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.getExamById(999))
        .rejects.toThrow('Exam ID 999 does not exist'); // CỐ TÌNH LÀM FAIL: Code ném lỗi "Exam not found"
    });

    /**
     * TC_ES_GET_003: Response DTO chứa thông tin Media đúng
     * 
     * Test Objective: Kiểm tra transform entity sang DTO bao gồm media info
     * Input: examId = 1 (exam có media questions)
     * Expected Output: Mỗi question có Media object với Skill, Type, Section, URLs
     * Notes: Media info quan trọng cho UI hiển thị audio/image
     */
    it('TC_ES_GET_003: Response DTO chứa thông tin Media đầy đủ', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act
      const result = await examService.getExamById(1);

      // Assert
      const firstQuestion = result.Questions[0];
      expect(firstQuestion.Media).toBeDefined();
      expect(firstQuestion.Media.ID).toBe(1);
      expect(firstQuestion.Media.Skill).toBe('Listening');
      expect(firstQuestion.Media.Type).toBe('AUDIO');
      expect(firstQuestion.Media.Section).toBe('Part 1');
      expect(firstQuestion.Media.AudioUrl).toBe('https://example.com/audio1.mp3');
    });
  });

  // ==================================================================
  // 3. TEST SUITE: getAllExams - Lấy danh sách tất cả bài test
  // ==================================================================
  describe('getAllExams - Lấy danh sách tất cả bài test', () => {

    /**
     * TC_ES_GALL_001: Lấy tất cả exams không filter
     * 
     * Test Objective: Kiểm tra lấy toàn bộ danh sách exam
     * Input: Không có filter
     * Expected Output: Array chứa tất cả exams
     * Notes: Dùng cho trang danh sách bài test
     */
    it('TC_ES_GALL_001: Lấy tất cả exams không filter', async () => {
      // Arrange
      const mockExams = [mockExam, { ...mockExam, ID: 2, Title: 'TOEIC Mini Test' }];
      (mockExamRepo.findAll as jest.Mock).mockResolvedValue(mockExams);

      // Act
      const result = await examService.getAllExams();

      // Assert
      expect(result).toHaveLength(2);
      expect(mockExamRepo.findAll).toHaveBeenCalledWith(undefined);
    });

    /**
     * TC_ES_GALL_002: Lấy exams với filter ExamTypeID
     * 
     * Test Objective: Kiểm tra lọc exams theo loại bài test
     * Input: filters = { ExamTypeID: 1 }
     * Expected Output: Chỉ trả về exams có ExamTypeID = 1
     * Notes: Dùng cho UI khi người dùng chọn loại bài test cụ thể
     */
    it('TC_ES_GALL_002: Lấy exams với filter ExamTypeID', async () => {
      // Arrange
      const filters = { ExamTypeID: 1 };
      (mockExamRepo.findAll as jest.Mock).mockResolvedValue([mockExam]);

      // Act
      const result = await examService.getAllExams(filters);

      // Assert
      expect(result).toHaveLength(1);
      expect(mockExamRepo.findAll).toHaveBeenCalledWith(filters);
    });

    /**
     * TC_ES_GALL_003: Trả về mảng rỗng khi không có exam
     * 
     * Test Objective: Kiểm tra trường hợp DB rỗng
     * Input: Không có exam nào trong DB
     * Expected Output: Array rỗng [], không phải null hay undefined
     * Notes: UI cần xử lý mảng rỗng khác null
     */
    it('TC_ES_GALL_003: Trả về mảng rỗng khi không có exam', async () => {
      // Arrange
      (mockExamRepo.findAll as jest.Mock).mockResolvedValue([]);

      // Act
      const result = await examService.getAllExams();

      // Assert
      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });

  // ==================================================================
  // 4. TEST SUITE: updateExam - Cập nhật bài test
  // ==================================================================
  describe('updateExam - Cập nhật thông tin bài test', () => {

    /**
     * TC_ES_UPD_001: Cập nhật exam thành công
     * 
     * Test Objective: Kiểm tra cập nhật metadata của exam
     * Input: examId = 1, updateData = { Title: "Updated Title" }
     * Expected Output: Exam được cập nhật với Title mới
     * Notes: Chỉ owner hoặc admin mới được phép cập nhật
     */
    it('TC_ES_UPD_001: Cập nhật exam thành công', async () => {
      // Arrange
      const updateData = { Title: 'TOEIC Updated Test' };
      const updatedExam = { ...mockExam, Title: 'TOEIC Updated Test' };
      
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.update as jest.Mock).mockResolvedValue(updatedExam);

      // Act
      const result = await examService.updateExam(1, updateData, 1);

      // Assert
      expect(result.Title).toBe('TOEIC Updated Test');
      expect(mockExamRepo.update).toHaveBeenCalledWith(1, updateData);
    });

    /**
     * TC_ES_UPD_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý khi exam ID không hợp lệ
     * Input: examId = 999 (không tồn tại)
     * Expected Output: Error "Exam not found"
     * Notes: Phải kiểm tra tồn tại trước khi cập nhật
     */
    it('TC_ES_UPD_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.updateExam(999, { Title: 'New' }, 1))
        .rejects.toThrow('Exam not found');
    });

    /**
     * TC_ES_UPD_003: Thất bại khi user không có quyền
     * 
     * Test Objective: Kiểm tra authorization - chỉ owner được cập nhật
     * Input: examId = 1 (owner = user 1), userId = 2 (khác owner)
     * Expected Output: Error "You do not have permission to update this exam"
     * Notes: Business rule - chỉ người tạo exam mới được sửa
     */
    it('TC_ES_UPD_003: Thất bại khi user không có quyền cập nhật', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam); // UserID = 1

      // Act & Assert (user 2 cố cập nhật exam của user 1)
      await expect(examService.updateExam(1, { Title: 'Hack' }, 2))
        .rejects.toThrow('You do not have permission to update this exam');
    });

    /**
     * TC_ES_UPD_004: Thất bại khi cập nhật TimeExam ngoài range
     * 
     * Test Objective: Kiểm tra validation TimeExam khi cập nhật
     * Input: updateData = { TimeExam: 500 } (vượt quá 240)
     * Expected Output: Error "Exam time must be between 1 and 240 minutes"
     * Notes: Validation phải áp dụng cho cả create và update
     */
    it('TC_ES_UPD_004: Thất bại khi cập nhật TimeExam ngoài range', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.updateExam(1, { TimeExam: 500 }, 1))
        .rejects.toThrow('Exam time must be between 1 and 240 minutes');
    });

    /**
     * TC_ES_UPD_005: Thất bại khi update trả về null
     * 
     * Test Objective: Kiểm tra xử lý khi repository update thất bại
     * Input: Valid data nhưng repo.update trả null
     * Expected Output: Error "Failed to update exam"
     * Notes: Edge case - DB operation failure
     */
    it('TC_ES_UPD_005: Thất bại khi repository update trả về null', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.update as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.updateExam(1, { Title: 'Test' }, 1))
        .rejects.toThrow('Failed to update exam');
    });
  });

  // ==================================================================
  // 5. TEST SUITE: deleteExam - Xóa bài test
  // ==================================================================
  describe('deleteExam - Xóa bài test', () => {

    /**
     * TC_ES_DEL_001: Xóa exam thành công (không có attempts)
     * 
     * Test Objective: Kiểm tra xóa exam khi chưa có sinh viên làm bài
     * Input: examId = 1, userId = 1 (owner)
     * Expected Output: true (xóa thành công)
     * Notes: Chỉ cho phép xóa khi chưa có ai làm bài
     */
    it('TC_ES_DEL_001: Xóa exam thành công khi chưa có attempts', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.delete as jest.Mock).mockResolvedValue(true);

      // Act
      const result = await examService.deleteExam(1, 1);

      // Assert
      expect(result).toBe(true);
      expect(mockExamRepo.delete).toHaveBeenCalledWith(1);
    });

    /**
     * TC_ES_DEL_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý xóa exam không tồn tại
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     * Notes: Phải kiểm tra tồn tại trước khi xóa
     */
    it('TC_ES_DEL_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.deleteExam(999, 1))
        .rejects.toThrow('Exam not found');
    });

    /**
     * TC_ES_DEL_003: Thất bại khi user không có quyền xóa
     * 
     * Test Objective: Kiểm tra authorization cho xóa exam
     * Input: examId = 1 (owner = user 1), userId = 2
     * Expected Output: Error "You do not have permission to delete this exam"
     * Notes: Chỉ owner hoặc admin mới được phép xóa
     */
    it('TC_ES_DEL_003: Thất bại khi user không có quyền xóa', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.deleteExam(1, 2))
        .rejects.toThrow('You do not have permission to delete this exam');
    });

    /**
     * TC_ES_DEL_004: Thất bại khi exam đã có sinh viên làm bài
     * 
     * Test Objective: Kiểm tra business rule - không xóa exam đã có attempts
     * Input: examId = 2 (exam đã có attempts)
     * Expected Output: Error "Cannot delete exam that has been taken by students..."
     * Notes: Bảo vệ dữ liệu điểm số sinh viên, đề xuất archive thay vì xóa
     */
    it('TC_ES_DEL_004: Thất bại khi exam đã có sinh viên làm bài', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExamWithAttempts);

      // Act & Assert
      await expect(examService.deleteExam(2, 1))
        .rejects.toThrow('Cannot delete exam that has been taken by students');
    });
  });

  // ==================================================================
  // 6. TEST SUITE: addQuestionsToExam - Thêm câu hỏi vào bài test
  // ==================================================================
  describe('addQuestionsToExam - Thêm câu hỏi vào bài test', () => {

    /**
     * TC_ES_AQ_001: Thêm câu hỏi thành công
     * 
     * Test Objective: Kiểm tra thêm câu hỏi vào exam đã tồn tại
     * Input: examId = 1, questions = [{QuestionID: 3, OrderIndex: 3}]
     * Expected Output: Updated exam với question mới
     * Notes: Câu hỏi mới không được trùng với câu đã có trong exam
     */
    it('TC_ES_AQ_001: Thêm câu hỏi vào exam thành công', async () => {
      // Arrange
      const questions = [{ QuestionID: 3, OrderIndex: 3 }];
      
      (mockExamRepo.findById as jest.Mock)
        .mockResolvedValueOnce(mockExam) // Lần 1: kiểm tra exam tồn tại
        .mockResolvedValueOnce({          // Lần 2: trả exam sau khi cập nhật
          ...mockExam,
          examQuestions: [...mockExamQuestions, {
            ID: 3, ExamID: 1, QuestionID: 3, OrderIndex: 3,
            question: mockQuestions[2],
          }],
        });
      (mockQuestionRepo.findByIds as jest.Mock).mockResolvedValue([mockQuestions[2]]);
      (mockExamRepo.addQuestions as jest.Mock).mockResolvedValue([]);

      // Act
      const result = await examService.addQuestionsToExam(1, questions, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockExamRepo.addQuestions).toHaveBeenCalledWith(1, questions);
    });

    /**
     * TC_ES_AQ_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra validation exam tồn tại
     * Input: examId = 999 (không tồn tại)
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_AQ_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(
        examService.addQuestionsToExam(999, [{ QuestionID: 1, OrderIndex: 1 }], 1)
      ).rejects.toThrow('Exam not found');
    });

    /**
     * TC_ES_AQ_003: Thất bại khi user không có quyền
     * 
     * Test Objective: Kiểm tra authorization
     * Input: userId = 2 (không phải owner)
     * Expected Output: Error "You do not have permission to modify this exam"
     */
    it('TC_ES_AQ_003: Thất bại khi user không có quyền', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(
        examService.addQuestionsToExam(1, [{ QuestionID: 3, OrderIndex: 3 }], 2)
      ).rejects.toThrow('You do not have permission to modify this exam');
    });

    /**
     * TC_ES_AQ_004: Thất bại khi câu hỏi đã tồn tại trong exam (duplicate)
     * 
     * Test Objective: Kiểm tra duplicate question detection
     * Input: questions = [{QuestionID: 1}] (ID 1 đã có trong exam)
     * Expected Output: Error chứa thông tin ID trùng lặp
     * Notes: Prevent duplicate questions trong cùng một bài test
     */
    it('TC_ES_AQ_004: Thất bại khi thêm câu hỏi đã có trong exam', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockQuestionRepo.findByIds as jest.Mock).mockResolvedValue([mockQuestions[0]]);

      // Act & Assert
      await expect(
        examService.addQuestionsToExam(1, [{ QuestionID: 1, OrderIndex: 3 }], 1)
      ).rejects.toThrow('already in this exam');
    });

    /**
     * TC_ES_AQ_005: Thất bại khi một số câu hỏi không tồn tại trong DB
     * 
     * Test Objective: Kiểm tra validation tất cả question IDs phải hợp lệ
     * Input: questions = [{QuestionID: 999}] (không tồn tại trong DB)
     * Expected Output: Error "Some questions do not exist"
     */
    it('TC_ES_AQ_005: Thất bại khi câu hỏi không tồn tại trong DB', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockQuestionRepo.findByIds as jest.Mock).mockResolvedValue([]); // Không tìm thấy

      // Act & Assert
      await expect(
        examService.addQuestionsToExam(1, [{ QuestionID: 999, OrderIndex: 3 }], 1)
      ).rejects.toThrow('Some questions do not exist');
    });
  });

  // ==================================================================
  // 7. TEST SUITE: removeQuestionsFromExam - Xóa câu hỏi khỏi bài test
  // ==================================================================
  describe('removeQuestionsFromExam - Xóa câu hỏi khỏi bài test', () => {

    /**
     * TC_ES_RQ_001: Xóa câu hỏi thành công
     * 
     * Test Objective: Kiểm tra xóa association giữa exam và question
     * Input: examId = 1, questionIds = [1]
     * Expected Output: Trả về số lượng câu hỏi đã xóa (1)
     * Notes: Chỉ xóa association, không xóa question trong DB
     */
    it('TC_ES_RQ_001: Xóa câu hỏi khỏi exam thành công', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.removeQuestions as jest.Mock).mockResolvedValue(1);

      // Act
      const result = await examService.removeQuestionsFromExam(1, [1], 1);

      // Assert
      expect(result).toBe(1);
      expect(mockExamRepo.removeQuestions).toHaveBeenCalledWith(1, [1]);
    });

    /**
     * TC_ES_RQ_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra validation exam tồn tại khi xóa question
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_RQ_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.removeQuestionsFromExam(999, [1], 1))
        .rejects.toThrow('Exam not found');
    });

    /**
     * TC_ES_RQ_003: Thất bại khi user không có quyền
     * 
     * Test Objective: Kiểm tra authorization khi xóa question từ exam
     * Input: userId = 2 (không phải owner của exam)
     * Expected Output: Error "You do not have permission to modify this exam"
     */
    it('TC_ES_RQ_003: Thất bại khi user không có quyền xóa', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.removeQuestionsFromExam(1, [1], 2))
        .rejects.toThrow('You do not have permission to modify this exam');
    });
  });

  // ==================================================================
  // 8. TEST SUITE: getExamStatistics - Lấy thống kê bài test
  // ==================================================================
  describe('getExamStatistics - Lấy thống kê bài test', () => {

    /**
     * TC_ES_STAT_001: Lấy thống kê thành công
     * 
     * Test Objective: Kiểm tra lấy statistics cho exam
     * Input: examId = 1
     * Expected Output: Object chứa totalQuestions, averageScore, etc.
     * Notes: Dùng cho dashboard admin/teacher
     */
    it('TC_ES_STAT_001: Lấy thống kê exam thành công', async () => {
      // Arrange
      const mockStats = {
        examId: 1,
        totalQuestions: 200,
        totalMediaGroups: 10,
        questionsInGroups: 150,
        standaloneQuestions: 50,
        totalAttempts: 100,
        averageScore: 75.5,
      };
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.getEnhancedStatistics as jest.Mock).mockResolvedValue(mockStats);

      // Act
      const result = await examService.getExamStatistics(1);

      // Assert
      expect(result).toBeDefined();
      expect(result.totalQuestions).toBe(199); // CỐ TÌNH LÀM FAIL: Expected 199 nhưng kết quả thực tế trả về 200
      expect(result.averageScore).toBe(75.5);
    });

    /**
     * TC_ES_STAT_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý lỗi khi exam ID không hợp lệ
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_STAT_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.getExamStatistics(999))
        .rejects.toThrow('Exam not found');
    });
  });

  // ==================================================================
  // 9. TEST SUITE: searchExams - Tìm kiếm bài test
  // ==================================================================
  describe('searchExams - Tìm kiếm bài test theo tiêu đề', () => {

    /**
     * TC_ES_SRCH_001: Tìm kiếm thành công
     * 
     * Test Objective: Kiểm tra tìm kiếm exam theo từ khóa
     * Input: searchTerm = "TOEIC"
     * Expected Output: Array chứa exams có title chứa "TOEIC"
     */
    it('TC_ES_SRCH_001: Tìm kiếm exam thành công', async () => {
      // Arrange
      (mockExamRepo.searchByTitle as jest.Mock).mockResolvedValue([mockExam]);

      // Act
      const result = await examService.searchExams('TOEIC');

      // Assert
      expect(result).toHaveLength(1);
      expect(mockExamRepo.searchByTitle).toHaveBeenCalledWith('TOEIC');
    });

    /**
     * TC_ES_SRCH_002: Thất bại khi search term rỗng
     * 
     * Test Objective: Kiểm tra validation - không cho phép tìm kiếm rỗng
     * Input: searchTerm = ""
     * Expected Output: Error "Search term cannot be empty"
     * Notes: Tránh query toàn bộ DB khi không có từ khóa
     */
    it('TC_ES_SRCH_002: Thất bại khi search term rỗng', async () => {
      // Act & Assert
      await expect(examService.searchExams(''))
        .rejects.toThrow('Search term cannot be empty');
    });

    /**
     * TC_ES_SRCH_003: Thất bại khi search term chỉ chứa khoảng trắng
     * 
     * Test Objective: Kiểm tra validation - whitespace-only không hợp lệ
     * Input: searchTerm = "   "
     * Expected Output: Error "Search term cannot be empty"
     */
    it('TC_ES_SRCH_003: Thất bại khi search term chỉ chứa khoảng trắng', async () => {
      // Act & Assert
      await expect(examService.searchExams('   '))
        .rejects.toThrow('Search term cannot be empty');
    });
  });

  // ==================================================================
  // 10. TEST SUITE: duplicateExam - Nhân bản bài test
  // ==================================================================
  describe('duplicateExam - Nhân bản bài test', () => {

    /**
     * TC_ES_DUP_001: Nhân bản exam thành công
     * 
     * Test Objective: Kiểm tra sao chép exam với tất cả câu hỏi
     * Input: examId = 1
     * Expected Output: Exam mới với Title = "original - Copy", cùng questions
     * Notes: Đảm bảo media tracking được copy đúng
     */
    it('TC_ES_DUP_001: Nhân bản exam thành công', async () => {
      // Arrange
      const duplicatedExam = {
        ...mockExam,
        ID: 20,
        Title: 'TOEIC Full Test #1 - Copy',
      };

      (mockExamRepo.findById as jest.Mock)
        .mockResolvedValueOnce(mockExam)      // Lần 1: lấy exam gốc
        .mockResolvedValueOnce(duplicatedExam); // Lần 2: lấy exam đã copy
      (mockExamRepo.create as jest.Mock).mockResolvedValue({ ...duplicatedExam, examQuestions: [] });
      (mockExamRepo.addQuestionsWithMediaTracking as jest.Mock).mockResolvedValue([]);

      // Act
      const result = await examService.duplicateExam(1, 1);

      // Assert
      expect(result).toBeDefined();
      expect(result.ID).toBe(20);
      expect(mockExamRepo.create).toHaveBeenCalledWith(
        expect.objectContaining({
          Title: 'TOEIC Full Test #1 - Copy',
        })
      );
      expect(mockExamRepo.addQuestionsWithMediaTracking).toHaveBeenCalled();
    });

    /**
     * TC_ES_DUP_002: Thất bại khi exam gốc không tồn tại
     * 
     * Test Objective: Kiểm tra validation exam tồn tại khi nhân bản
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_DUP_002: Thất bại khi exam gốc không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.duplicateExam(999, 1))
        .rejects.toThrow('Exam not found');
    });
  });

  // ==================================================================
  // 11. TEST SUITE: addMediaGroupToExam - Thêm nhóm media vào bài test
  // ==================================================================
  describe('addMediaGroupToExam - Thêm nhóm media vào bài test', () => {

    /**
     * TC_ES_AMG_001: Thêm media group thành công
     * 
     * Test Objective: Kiểm tra thêm toàn bộ câu hỏi từ media group vào exam
     * Input: examId = 1, mediaQuestionId = 2, startingOrderIndex = 3
     * Expected Output: Object chứa exam updated, questionsAdded, orderIndex range
     * Notes: Tất cả questions của media group được thêm tự động
     */
    it('TC_ES_AMG_001: Thêm media group vào exam thành công', async () => {
      // Arrange
      const updatedExam = { ...mockExam };
      
      (mockExamRepo.findById as jest.Mock)
        .mockResolvedValueOnce(mockExam)       // Validate exam
        .mockResolvedValueOnce(updatedExam);    // Return updated exam
      (mockMediaQuestionRepo.findById as jest.Mock).mockResolvedValue(mockMediaQuestion);
      (mockExamRepo.containsMediaGroup as jest.Mock).mockResolvedValue(false);
      (mockQuestionRepo.findByMediaQuestionId as jest.Mock).mockResolvedValue([
        mockQuestions[0],
        mockQuestions[1],
      ]);
      (mockExamRepo.addQuestionsWithMediaTracking as jest.Mock).mockResolvedValue([]);

      // Act
      const result = await examService.addMediaGroupToExam(1, 2, 3, 1);

      // Assert
      expect(result).toBeDefined();
      expect(result.questionsAdded).toBe(2);
      expect(result.startOrderIndex).toBe(3);
      expect(result.endOrderIndex).toBe(4);
    });

    /**
     * TC_ES_AMG_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Validation - exam phải tồn tại
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_AMG_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.addMediaGroupToExam(999, 1, 1, 1))
        .rejects.toThrow('Exam not found');
    });

    /**
     * TC_ES_AMG_003: Thất bại khi user không có quyền
     * 
     * Test Objective: Authorization check
     * Input: userId = 2 (không phải owner)
     * Expected Output: Error "You do not have permission to modify this exam"
     */
    it('TC_ES_AMG_003: Thất bại khi user không có quyền', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.addMediaGroupToExam(1, 1, 1, 2))
        .rejects.toThrow('You do not have permission to modify this exam');
    });

    /**
     * TC_ES_AMG_004: Thất bại khi media group không tồn tại
     * 
     * Test Objective: Validation - media group phải tồn tại
     * Input: mediaQuestionId = 999 (không tồn tại)
     * Expected Output: Error "Media group not found"
     */
    it('TC_ES_AMG_004: Thất bại khi media group không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockMediaQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.addMediaGroupToExam(1, 999, 1, 1))
        .rejects.toThrow('Media group not found');
    });

    /**
     * TC_ES_AMG_005: Thất bại khi media group đã có trong exam (duplicate)
     * 
     * Test Objective: Prevent duplicate media groups
     * Input: mediaQuestionId = 1 (đã có trong exam)
     * Expected Output: Error "This media group is already in the exam"
     */
    it('TC_ES_AMG_005: Thất bại khi media group đã tồn tại trong exam', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockMediaQuestionRepo.findById as jest.Mock).mockResolvedValue(mockMediaQuestion);
      (mockExamRepo.containsMediaGroup as jest.Mock).mockResolvedValue(true);

      // Act & Assert
      await expect(examService.addMediaGroupToExam(1, 1, 1, 1))
        .rejects.toThrow('This media group is already in the exam');
    });

    /**
     * TC_ES_AMG_006: Thất bại khi media group không có câu hỏi
     * 
     * Test Objective: Validation - media group phải có ít nhất 1 câu hỏi
     * Input: Media group tồn tại nhưng không có questions
     * Expected Output: Error "Media group has no questions"
     */
    it('TC_ES_AMG_006: Thất bại khi media group không có câu hỏi', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockMediaQuestionRepo.findById as jest.Mock).mockResolvedValue(mockMediaQuestion);
      (mockExamRepo.containsMediaGroup as jest.Mock).mockResolvedValue(false);
      (mockQuestionRepo.findByMediaQuestionId as jest.Mock).mockResolvedValue([]);

      // Act & Assert
      await expect(examService.addMediaGroupToExam(1, 1, 3, 1))
        .rejects.toThrow('Media group has no questions');
    });
  });

  // ==================================================================
  // 12. TEST SUITE: removeMediaGroupFromExam - Xóa nhóm media khỏi exam
  // ==================================================================
  describe('removeMediaGroupFromExam - Xóa nhóm media khỏi bài test', () => {

    /**
     * TC_ES_RMG_001: Xóa media group thành công
     * 
     * Test Objective: Kiểm tra xóa toàn bộ câu hỏi của media group khỏi exam
     * Input: examId = 1, mediaQuestionId = 1
     * Expected Output: Số câu hỏi đã xóa (2)
     * Notes: Xóa association, không xóa câu hỏi gốc hay media
     */
    it('TC_ES_RMG_001: Xóa media group khỏi exam thành công', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.containsMediaGroup as jest.Mock).mockResolvedValue(true);
      (mockExamRepo.removeMediaGroup as jest.Mock).mockResolvedValue(2);

      // Act
      const result = await examService.removeMediaGroupFromExam(1, 1, 1);

      // Assert
      expect(result).toBe(2);
      expect(mockExamRepo.removeMediaGroup).toHaveBeenCalledWith(1, 1);
    });

    /**
     * TC_ES_RMG_002: Thất bại khi media group không có trong exam
     * 
     * Test Objective: Kiểm tra media group phải tồn tại trong exam
     * Input: mediaQuestionId = 99 (không có trong exam)
     * Expected Output: Error "Media group not found in this exam"
     */
    it('TC_ES_RMG_002: Thất bại khi media group không có trong exam', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.containsMediaGroup as jest.Mock).mockResolvedValue(false);

      // Act & Assert
      await expect(examService.removeMediaGroupFromExam(1, 99, 1))
        .rejects.toThrow('Media group not found in this exam');
    });
  });

  // ==================================================================
  // 13. TEST SUITE: ExamType CRUD - Quản lý loại bài test
  // ==================================================================
  describe('ExamType CRUD - Quản lý loại bài test', () => {

    /**
     * TC_ES_ET_001: Lấy danh sách ExamTypes thành công
     * 
     * Test Objective: Kiểm tra lấy tất cả loại bài test
     * Input: Không có
     * Expected Output: Array chứa các ExamType objects
     */
    it('TC_ES_ET_001: Lấy danh sách ExamTypes thành công', async () => {
      // Arrange
      const mockExamTypes = [
        mockExamType,
        { ID: 2, Code: 'MINI_TEST', Description: 'Mini Test', exams: [] },
      ];
      (mockExamRepo.findAllExamTypes as jest.Mock).mockResolvedValue(mockExamTypes);

      // Act
      const result = await examService.getExamTypes();

      // Assert
      expect(result).toHaveLength(2);
      expect(result[0].Code).toBe('FULL_TEST');
    });

    /**
     * TC_ES_ET_002: Tạo ExamType thành công
     * 
     * Test Objective: Kiểm tra tạo loại bài test mới
     * Input: { Code: "PRACTICE_PART_5", Description: "Practice Part 5" }
     * Expected Output: ExamType mới được tạo
     * Notes: Code phải unique
     */
    it('TC_ES_ET_002: Tạo ExamType thành công', async () => {
      // Arrange
      const createDto = { Code: 'PRACTICE_PART_5', Description: 'Practice Part 5' };
      const newExamType = { ID: 3, ...createDto, exams: [] };
      
      (mockExamRepo.findExamTypeByCode as jest.Mock).mockResolvedValue(null);
      (mockExamRepo.createExamType as jest.Mock).mockResolvedValue(newExamType);

      // Act
      const result = await examService.createExamType(createDto);

      // Assert
      expect(result.Code).toBe('PRACTICE_PART_5');
      expect(mockExamRepo.createExamType).toHaveBeenCalledWith(createDto);
    });

    /**
     * TC_ES_ET_003: Thất bại khi tạo ExamType với Code trùng
     * 
     * Test Objective: Kiểm tra unique constraint trên Code
     * Input: Code = "FULL_TEST" (đã tồn tại)
     * Expected Output: Error chứa thông tin code trùng
     */
    it('TC_ES_ET_003: Thất bại khi tạo ExamType với Code đã tồn tại', async () => {
      // Arrange
      (mockExamRepo.findExamTypeByCode as jest.Mock).mockResolvedValue(mockExamType);

      // Act & Assert
      await expect(
        examService.createExamType({ Code: 'FULL_TEST', Description: 'Test' })
      ).rejects.toThrow('already exists');
    });

    /**
     * TC_ES_ET_004: Cập nhật ExamType thành công
     * 
     * Test Objective: Kiểm tra cập nhật thông tin loại bài test
     * Input: id = 1, data = { Description: "Updated Description" }
     * Expected Output: ExamType được cập nhật
     */
    it('TC_ES_ET_004: Cập nhật ExamType thành công', async () => {
      // Arrange
      const updateDto = { Description: 'Updated Full Test Description' };
      const updatedType = { ...mockExamType, Description: 'Updated Full Test Description' };
      
      (mockExamRepo.findExamTypeById as jest.Mock).mockResolvedValue(mockExamType);
      (mockExamRepo.updateExamType as jest.Mock).mockResolvedValue(updatedType);

      // Act
      const result = await examService.updateExamType(1, updateDto);

      // Assert
      expect(result.Description).toBe('Updated Full Test Description');
    });

    /**
     * TC_ES_ET_005: Thất bại khi cập nhật ExamType không tồn tại
     * 
     * Test Objective: Kiểm tra validation ExamType tồn tại
     * Input: id = 999
     * Expected Output: Error "Exam type not found"
     */
    it('TC_ES_ET_005: Thất bại khi ExamType không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findExamTypeById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.updateExamType(999, { Description: 'Test' }))
        .rejects.toThrow('Exam type not found');
    });

    /**
     * TC_ES_ET_006: Thất bại khi cập nhật Code trùng
     * 
     * Test Objective: Kiểm tra unique constraint khi update Code
     * Input: id = 1, data = { Code: "MINI_TEST" } (đã được dùng bởi type khác)
     * Expected Output: Error chứa thông tin code trùng
     */
    it('TC_ES_ET_006: Thất bại khi cập nhật Code trùng với type khác', async () => {
      // Arrange
      const existingType = { ...mockExamType, Code: 'FULL_TEST' };
      const anotherType = { ID: 2, Code: 'MINI_TEST', Description: 'Mini', exams: [] };
      
      (mockExamRepo.findExamTypeById as jest.Mock).mockResolvedValue(existingType);
      (mockExamRepo.findExamTypeByCode as jest.Mock).mockResolvedValue(anotherType);

      // Act & Assert
      await expect(
        examService.updateExamType(1, { Code: 'MINI_TEST' })
      ).rejects.toThrow('already in use');
    });

    /**
     * TC_ES_ET_007: Xóa ExamType thành công
     * 
     * Test Objective: Kiểm tra xóa loại bài test khi không có exam nào sử dụng
     * Input: id = 1 (không có exam nào dùng type này)
     * Expected Output: true
     * Notes: Phải kiểm tra không có exam nào đang reference type này
     */
    it('TC_ES_ET_007: Xóa ExamType thành công khi không có exam sử dụng', async () => {
      // Arrange
      (mockExamRepo.countExamsByType as jest.Mock).mockResolvedValue(0);
      (mockExamRepo.deleteExamType as jest.Mock).mockResolvedValue(true);

      // Act
      const result = await examService.deleteExamType(1);

      // Assert
      expect(result).toBe(true);
    });

    /**
     * TC_ES_ET_008: Thất bại khi xóa ExamType đang được sử dụng
     * 
     * Test Objective: Kiểm tra referential integrity
     * Input: id = 1 (có 5 exams đang sử dụng type này)
     * Expected Output: Error thông báo số lượng exam đang sử dụng
     * Notes: Bảo vệ data integrity - không xóa type khi còn exam reference
     */
    it('TC_ES_ET_008: Thất bại khi xóa ExamType đang được sử dụng', async () => {
      // Arrange
      (mockExamRepo.countExamsByType as jest.Mock).mockResolvedValue(5);

      // Act & Assert
      await expect(examService.deleteExamType(1))
        .rejects.toThrow('Cannot delete exam type: 5 exam(s) are using it');
    });
  });

  // ==================================================================
  // 14. TEST SUITE: getNextOrderIndex - Lấy OrderIndex tiếp theo
  // ==================================================================
  describe('getNextOrderIndex - Lấy OrderIndex tiếp theo', () => {

    /**
     * TC_ES_NOI_001: Lấy next OrderIndex thành công
     * 
     * Test Objective: Kiểm tra trả về OrderIndex tiếp theo cho exam
     * Input: examId = 1
     * Expected Output: Số nguyên > 0 (giá trị OrderIndex tiếp theo)
     */
    it('TC_ES_NOI_001: Lấy next OrderIndex thành công', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.getNextOrderIndex as jest.Mock).mockResolvedValue(3);

      // Act
      const result = await examService.getNextOrderIndex(1);

      // Assert
      expect(result).toBe(3);
    });

    /**
     * TC_ES_NOI_002: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Validation - exam phải tồn tại
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_NOI_002: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.getNextOrderIndex(999))
        .rejects.toThrow('Exam not found');
    });
  });

  // ==================================================================
  // 15. TEST SUITE: validateExamStructure - Validate cấu trúc bài test
  // ==================================================================
  describe('validateExamStructure - Kiểm tra cấu trúc bài test', () => {

    /**
     * TC_ES_VES_001: Validate thành công - exam hợp lệ
     * 
     * Test Objective: Kiểm tra exam có cấu trúc hợp lệ
     * Input: examId = 1 (exam hợp lệ)
     * Expected Output: { isValid: true, issues: [] }
     */
    it('TC_ES_VES_001: Validate exam với cấu trúc hợp lệ', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.validateExamStructure as jest.Mock).mockResolvedValue({
        isValid: true,
        issues: [],
      });

      // Act
      const result = await examService.validateExamStructure(1);

      // Assert
      expect(result.isValid).toBe(true);
      expect(result.issues).toHaveLength(0);
    });

    /**
     * TC_ES_VES_002: Validate phát hiện issues
     * 
     * Test Objective: Kiểm tra phát hiện lỗi cấu trúc
     * Input: examId = 1 (exam có gaps trong OrderIndex)
     * Expected Output: { isValid: false, issues: ["Gap in OrderIndex..."] }
     */
    it('TC_ES_VES_002: Validate phát hiện structural issues', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.validateExamStructure as jest.Mock).mockResolvedValue({
        isValid: false,
        issues: ['Gap in OrderIndex sequence between 2 and 5'],
      });

      // Act
      const result = await examService.validateExamStructure(1);

      // Assert
      expect(result.isValid).toBe(false);
      expect(result.issues).toHaveLength(1);
      expect(result.issues[0]).toContain('Gap');
    });

    /**
     * TC_ES_VES_003: Thất bại khi exam không tồn tại
     * 
     * Test Objective: Validation - exam phải tồn tại cho validate
     * Input: examId = 999
     * Expected Output: Error "Exam not found"
     */
    it('TC_ES_VES_003: Thất bại khi exam không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.validateExamStructure(999))
        .rejects.toThrow('Exam not found');
    });
  });

  // ==================================================================
  // 16. TEST SUITE: compactExamOrder - Sắp xếp lại OrderIndex
  // ==================================================================
  describe('compactExamOrder - Sắp xếp lại thứ tự câu hỏi', () => {

    /**
     * TC_ES_CEO_001: Compact order thành công
     * 
     * Test Objective: Kiểm tra nén lại OrderIndex sequence
     * Input: examId = 1 (exam có gaps: 1,2,5,6 → 1,2,3,4)
     * Expected Output: Số questions được reorder
     */
    it('TC_ES_CEO_001: Compact order thành công', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockExamRepo.reorderQuestions as jest.Mock).mockResolvedValue(2);

      // Act
      const result = await examService.compactExamOrder(1, 1);

      // Assert
      expect(result).toBe(2);
      expect(mockExamRepo.reorderQuestions).toHaveBeenCalled();
    });

    /**
     * TC_ES_CEO_002: Thất bại khi user không có quyền
     * 
     * Test Objective: Authorization check
     * Input: userId = 2 (không phải owner)
     * Expected Output: Error "You do not have permission to modify this exam"
     */
    it('TC_ES_CEO_002: Thất bại khi user không có quyền', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.compactExamOrder(1, 2))
        .rejects.toThrow('You do not have permission to modify this exam');
    });
  });

  // ==================================================================
  // 17. TEST SUITE: replaceQuestionInExam - Thay thế câu hỏi
  // ==================================================================
  describe('replaceQuestionInExam - Thay thế câu hỏi trong bài test', () => {

    /**
     * TC_ES_RPQ_001: Thay thế câu hỏi thành công
     * 
     * Test Objective: Kiểm tra swap question giữ nguyên OrderIndex
     * Input: examId=1, oldQuestionId=1, newQuestionId=3
     * Expected Output: true
     * Notes: OrderIndex và media tracking phải được maintain
     */
    it('TC_ES_RPQ_001: Thay thế câu hỏi thành công', async () => {
      // Arrange
      const examWithNonGrouped = {
        ...mockExam,
        examQuestions: [{
          ...mockExamQuestions[0],
          IsGrouped: false,
          MediaQuestionID: null,
        }],
      };
      
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(examWithNonGrouped);
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestions[2]);
      (mockExamRepo.updateExamQuestion as jest.Mock).mockResolvedValue({
        ID: 1,
        QuestionID: 3,
      });

      // Act
      const result = await examService.replaceQuestionInExam(1, 1, 3, 1);

      // Assert
      expect(result).toBe(true);
    });

    /**
     * TC_ES_RPQ_002: Thất bại khi old question không có trong exam
     * 
     * Test Objective: Validation - question cũ phải tồn tại trong exam
     * Input: oldQuestionId = 999 (không có trong exam)
     * Expected Output: Error "Question not found in exam"
     */
    it('TC_ES_RPQ_002: Thất bại khi câu hỏi cũ không có trong exam', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);

      // Act & Assert
      await expect(examService.replaceQuestionInExam(1, 999, 3, 1))
        .rejects.toThrow('Question not found in exam');
    });

    /**
     * TC_ES_RPQ_003: Thất bại khi new question không tồn tại
     * 
     * Test Objective: Validation - question mới phải tồn tại trong DB
     * Input: newQuestionId = 999 (không tồn tại)
     * Expected Output: Error "New question not found"
     */
    it('TC_ES_RPQ_003: Thất bại khi câu hỏi mới không tồn tại', async () => {
      // Arrange
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(examService.replaceQuestionInExam(1, 1, 999, 1))
        .rejects.toThrow('New question not found');
    });

    /**
     * TC_ES_RPQ_004: Thất bại khi thay question grouped bằng question khác media
     * 
     * Test Objective: Business rule - question trong group phải cùng media
     * Input: Question grouped thuộc media 1, thay bằng question thuộc media 2
     * Expected Output: Error "Cannot replace with question from different media group"
     * Notes: Đảm bảo tính nhất quán của media group
     */
    it('TC_ES_RPQ_004: Thất bại khi thay question từ media group khác', async () => {
      // Arrange - examQuestions[0] có IsGrouped=true, MediaQuestionID=1
      (mockExamRepo.findById as jest.Mock).mockResolvedValue(mockExam);
      // New question thuộc media khác (MediaQuestionID=2)
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue({
        ...mockQuestions[2],
        MediaQuestionID: 2, // Khác với MediaQuestionID=1 của question cũ
      });

      // Act & Assert
      await expect(examService.replaceQuestionInExam(1, 1, 3, 1))
        .rejects.toThrow('Cannot replace with question from different media group');
    });
  });
});
