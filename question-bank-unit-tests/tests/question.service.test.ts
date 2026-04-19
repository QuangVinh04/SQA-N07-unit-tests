/**
 * =================================================================
 * Unit Test Suite: QuestionService - Quản lý Ngân hàng câu hỏi TOEIC
 * =================================================================
 * 
 * File này chứa tất cả unit tests cho QuestionService.
 * QuestionService là service chính xử lý business logic liên quan đến
 * quản lý ngân hàng câu hỏi (Question Bank) trong hệ thống TOEIC.
 * 
 * Testing Framework: Jest + ts-jest
 * Mocking: Jest built-in mock functions
 * 
 * Quy ước đặt tên Test Case ID:
 *   - TC_QS_CRE_xxx:  Test cases cho createQuestion
 *   - TC_QS_GET_xxx:  Test cases cho getQuestionById
 *   - TC_QS_SRCH_xxx: Test cases cho searchQuestions
 *   - TC_QS_UPD_xxx:  Test cases cho updateQuestion
 *   - TC_QS_DEL_xxx:  Test cases cho deleteQuestion
 *   - TC_QS_STAT_xxx: Test cases cho getQuestionStatistics
 *   - TC_QS_SEC_xxx:  Test cases cho getQuestionsBySection
 *   - TC_QS_BULK_xxx: Test cases cho performBulkOperation
 *   - TC_QS_VC_xxx:   Test cases cho validateChoices (private)
 *   - TC_QS_VM_xxx:   Test cases cho validateMediaRequirements (private)
 *   - TC_QS_URL_xxx:  Test cases cho isValidUrl (private)
 * 
 * @author TrinhHieu - SQA Unit Testing
 */

import { QuestionService } from '../src/application/services/question.service';
import { QuestionRepository } from '../src/infrastructure/repositories/question.repository';

// ====================================================================
// MOCK SETUP: Mock repository để isolate business logic
// ====================================================================

/**
 * Mock QuestionRepository - Giả lập toàn bộ data access layer cho Question
 * Mục đích: Đảm bảo unit test chỉ test business logic trong QuestionService,
 * không phụ thuộc vào database thực.
 */
jest.mock('../src/infrastructure/repositories/question.repository');

// ====================================================================
// MOCK DATA: Dữ liệu giả lập cho testing
// ====================================================================

/**
 * Tạo mock MediaQuestion object
 * Đại diện cho media (audio/image/passage) gắn với câu hỏi
 */
const mockMediaQuestion = {
  ID: 1,
  Skill: 'LISTENING',
  Type: 'PHOTO_DESCRIPTION',
  Section: '1',
  AudioUrl: 'https://example.com/audio/part1_q1.mp3',
  ImageUrl: 'https://example.com/images/part1_photo1.jpg',
  Scirpt: 'A man is standing near the door.',
  GroupTitle: 'Part 1 - Photo Descriptions',
  GroupDescription: 'Bộ câu hỏi mô tả ảnh Part 1',
  Difficulty: 'MEDIUM',
  Tags: ['part1', 'photo'],
  OrderIndex: 1,
};

/**
 * Tạo mock Choice objects
 * Đại diện cho các lựa chọn A, B, C, D của câu hỏi TOEIC
 */
const mockChoices = [
  { ID: 1, Attribute: 'A', Content: 'He is opening the door.', IsCorrect: true, QuestionID: 1 },
  { ID: 2, Attribute: 'B', Content: 'He is sitting at a desk.', IsCorrect: false, QuestionID: 1 },
  { ID: 3, Attribute: 'C', Content: 'He is reading a book.', IsCorrect: false, QuestionID: 1 },
  { ID: 4, Attribute: 'D', Content: 'He is talking on the phone.', IsCorrect: false, QuestionID: 1 },
];

/**
 * Tạo mock Question object đầy đủ
 * Đại diện cho câu hỏi với tất cả relations (media, choices)
 */
const mockQuestion = {
  ID: 1,
  QuestionText: 'What is the man doing in the picture?',
  UserID: 1,
  MediaQuestionID: 1,
  OrderInGroup: 1,
  mediaQuestion: mockMediaQuestion,
  choices: mockChoices,
  examQuestions: [],
  attemptAnswers: [],
};

/**
 * Tạo mock Question thứ hai - Reading type
 */
const mockQuestionReading = {
  ID: 2,
  QuestionText: 'What is the main topic of the passage?',
  UserID: 1,
  MediaQuestionID: 2,
  OrderInGroup: 1,
  mediaQuestion: {
    ...mockMediaQuestion,
    ID: 2,
    Skill: 'READING',
    Type: 'READING_COMPREHENSION',
    Section: '7',
    AudioUrl: null,
    ImageUrl: null,
    Scirpt: 'The quarterly report shows a significant increase in revenue...',
  },
  choices: [
    { ID: 5, Attribute: 'A', Content: 'Company revenue', IsCorrect: true, QuestionID: 2 },
    { ID: 6, Attribute: 'B', Content: 'Employee benefits', IsCorrect: false, QuestionID: 2 },
    { ID: 7, Attribute: 'C', Content: 'New products', IsCorrect: false, QuestionID: 2 },
    { ID: 8, Attribute: 'D', Content: 'Market trends', IsCorrect: false, QuestionID: 2 },
  ],
  examQuestions: [],
  attemptAnswers: [],
};

/**
 * Mock usage stats object
 */
const mockUsageStats = {
  questionId: 1,
  usedInExams: 3,
  totalAttempts: 150,
  correctAttempts: 105,
  correctPercentage: 70,
  difficulty: 'MEDIUM',
};

/**
 * Mock usage stats - question chưa được sử dụng
 */
const mockUnusedStats = {
  questionId: 3,
  usedInExams: 0,
  totalAttempts: 0,
  correctAttempts: 0,
  correctPercentage: 0,
  difficulty: 'HARD',
};

// ====================================================================
// TEST SUITES
// ====================================================================

describe('QuestionService - Quản lý Ngân hàng câu hỏi TOEIC', () => {
  /** Instance của QuestionService được test */
  let questionService: QuestionService;

  /** Mock instance của QuestionRepository */
  let mockQuestionRepo: jest.Mocked<QuestionRepository>;

  /**
   * beforeEach: Chạy trước mỗi test case
   * - Tạo instance mới của QuestionService
   * - Lấy mock instance từ constructor
   * - Reset tất cả mock states
   */
  beforeEach(() => {
    // Tạo instance mới - constructor sẽ tự động tạo mocked repository
    questionService = new QuestionService();

    // Lấy mock instance để configure behavior
    mockQuestionRepo = (QuestionRepository as jest.MockedClass<typeof QuestionRepository>)
      .mock.instances[0] as jest.Mocked<QuestionRepository>;
  });

  /**
   * afterEach: Chạy sau mỗi test case
   * - Clear tất cả mocks để đảm bảo test isolation
   */
  afterEach(() => {
    jest.clearAllMocks();
  });

  // ==================================================================
  // 1. TEST SUITE: createQuestion - Tạo câu hỏi mới
  // ==================================================================
  describe('createQuestion - Tạo câu hỏi mới vào ngân hàng', () => {

    /**
     * TC_QS_CRE_001: Tạo câu hỏi Listening thành công
     * 
     * Test Objective: Kiểm tra tạo câu hỏi Listening với đầy đủ media và choices
     * Input: QuestionText, Media (Skill=LISTENING, AudioUrl, ImageUrl), 4 Choices
     * Expected Output: Question object được tạo với đầy đủ relations
     * Notes: Trường hợp chuẩn nhất - câu hỏi Part 1 có cả audio lẫn ảnh
     */
    it('TC_QS_CRE_001: Tạo câu hỏi Listening thành công với đầy đủ dữ liệu', async () => {
      // Arrange: Chuẩn bị dữ liệu đầu vào
      const createDto = {
        QuestionText: 'What is the man doing in the picture?',
        Media: {
          Skill: 'LISTENING',
          Type: 'PHOTO_DESCRIPTION',
          Section: '1',
          AudioUrl: 'https://example.com/audio/q1.mp3',
          ImageUrl: 'https://example.com/images/q1.jpg',
          Script: 'A man is standing near the door.',
        },
        Choices: [
          { Attribute: 'A', Content: 'He is opening the door.', IsCorrect: true },
          { Attribute: 'B', Content: 'He is sitting at a desk.', IsCorrect: false },
          { Attribute: 'C', Content: 'He is reading a book.', IsCorrect: false },
          { Attribute: 'D', Content: 'He is talking on the phone.', IsCorrect: false },
        ],
      };
      const userId = 1;

      // Mock repository response
      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      // Act: Thực hiện hành động
      const result = await questionService.createQuestion(createDto, userId);

      // Assert: Kiểm tra kết quả
      expect(result).toBeDefined();
      expect(result.ID).toBe(1);
      expect(mockQuestionRepo.create).toHaveBeenCalledTimes(1);
      expect(mockQuestionRepo.create).toHaveBeenCalledWith(
        { QuestionText: 'What is the man doing in the picture?', UserID: 1 },
        expect.objectContaining({
          Skill: 'LISTENING',
          Type: 'PHOTO_DESCRIPTION',
          Section: '1',
        }),
        expect.arrayContaining([
          expect.objectContaining({ Attribute: 'A', IsCorrect: true }),
        ])
      );
    });

    /**
     * TC_QS_CRE_002: Tạo câu hỏi Reading thành công
     * 
     * Test Objective: Kiểm tra tạo câu hỏi Reading (không cần audio)
     * Input: QuestionText, Media (Skill=READING, Script), 4 Choices
     * Expected Output: Question object được tạo thành công
     * Notes: Câu hỏi Reading chỉ cần script/passage, không cần AudioUrl
     */
    it('TC_QS_CRE_002: Tạo câu hỏi Reading thành công', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'What is the main topic?',
        Media: {
          Skill: 'READING',
          Type: 'READING_COMPREHENSION',
          Section: '7',
          Script: 'The quarterly report shows...',
        },
        Choices: [
          { Attribute: 'A', Content: 'Revenue', IsCorrect: true },
          { Attribute: 'B', Content: 'Benefits', IsCorrect: false },
          { Attribute: 'C', Content: 'Products', IsCorrect: false },
          { Attribute: 'D', Content: 'Trends', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestionReading);

      // Act
      const result = await questionService.createQuestion(createDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(result.ID).toBe(2);
      expect(mockQuestionRepo.create).toHaveBeenCalledTimes(1);
    });

    /**
     * TC_QS_CRE_003: Thất bại khi chỉ có 1 choice (ít hơn 2)
     * 
     * Test Objective: Kiểm tra validation - câu hỏi phải có ít nhất 2 choices
     * Input: Choices chỉ có 1 phần tử
     * Expected Output: Error "Question must have at least 2 choices"
     * Notes: Business rule - câu hỏi TOEIC tối thiểu có 2 lựa chọn
     */
    it('TC_QS_CRE_003: Thất bại khi choices ít hơn 2', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Only one choice', IsCorrect: true },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Question must have at least 2 choices');
      
      // Verify: Không gọi repository khi validation thất bại
      expect(mockQuestionRepo.create).not.toHaveBeenCalled();
    });

    /**
     * TC_QS_CRE_004: Thất bại khi không có đáp án đúng
     * 
     * Test Objective: Kiểm tra validation - phải có đúng 1 đáp án đúng
     * Input: 4 Choices nhưng tất cả IsCorrect = false
     * Expected Output: Error "Question must have exactly one correct answer"
     * Notes: Business rule - mỗi câu hỏi TOEIC chỉ có duy nhất 1 đáp án đúng
     */
    it('TC_QS_CRE_004: Thất bại khi không có đáp án đúng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Choice A', IsCorrect: false },
          { Attribute: 'B', Content: 'Choice B', IsCorrect: false },
          { Attribute: 'C', Content: 'Choice C', IsCorrect: false },
          { Attribute: 'D', Content: 'Choice D', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Question must have exactly one correct answer');
    });

    /**
     * TC_QS_CRE_005: Thất bại khi có nhiều đáp án đúng
     * 
     * Test Objective: Kiểm tra validation - chỉ được có 1 đáp án đúng
     * Input: 4 Choices với 2 IsCorrect = true
     * Expected Output: Error "Question must have exactly one correct answer"
     * Notes: TOEIC là trắc nghiệm đơn - không có multi-correct
     */
    it('TC_QS_CRE_005: Thất bại khi có nhiều hơn 1 đáp án đúng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Choice A', IsCorrect: true },
          { Attribute: 'B', Content: 'Choice B', IsCorrect: true }, // 2 đáp án đúng
          { Attribute: 'C', Content: 'Choice C', IsCorrect: false },
          { Attribute: 'D', Content: 'Choice D', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Question must have exactly one correct answer');
    });

    /**
     * TC_QS_CRE_006: Thất bại khi choice attributes bị trùng
     * 
     * Test Objective: Kiểm tra validation - attribute (A/B/C/D) phải unique
     * Input: 2 Choices đều có Attribute = "A"
     * Expected Output: Error "Choice attributes must be unique"
     * Notes: Tránh confusion - mỗi lựa chọn phải có label riêng
     */
    it('TC_QS_CRE_006: Thất bại khi choice attributes trùng lặp', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Choice 1', IsCorrect: true },
          { Attribute: 'A', Content: 'Choice 2', IsCorrect: false }, // Trùng attribute A
          { Attribute: 'C', Content: 'Choice 3', IsCorrect: false },
          { Attribute: 'D', Content: 'Choice 4', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Choice attributes must be unique');
    });

    /**
     * TC_QS_CRE_007: Thất bại khi choice content rỗng
     * 
     * Test Objective: Kiểm tra validation - tất cả choices phải có nội dung
     * Input: 1 Choice có Content = "" (rỗng)
     * Expected Output: Error "Choice content cannot be empty"
     * Notes: CỐ TÌNH LÀM FAIL - Error message trong test không khớp với code thực tế
     */
    it('TC_QS_CRE_007: Thất bại khi choice có content rỗng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Choice A', IsCorrect: true },
          { Attribute: 'B', Content: '', IsCorrect: false }, // Content rỗng
          { Attribute: 'C', Content: 'Choice C', IsCorrect: false },
          { Attribute: 'D', Content: 'Choice D', IsCorrect: false },
        ],
      };

      // Act & Assert
      // CỐ TÌNH FAIL: Test expect "Choice content cannot be empty"
      // nhưng code thực tế throw "All choices must have content"
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Choice content cannot be empty');
    });

    /**
     * TC_QS_CRE_008: Thất bại khi Listening question thiếu AudioUrl
     * 
     * Test Objective: Kiểm tra validation - câu Listening phải có audio
     * Input: Media.Skill = "LISTENING" nhưng không có AudioUrl
     * Expected Output: Error "Listening questions must have audio URL"
     * Notes: Business rule - câu Listening bắt buộc phải có file audio
     */
    it('TC_QS_CRE_008: Thất bại khi câu Listening thiếu AudioUrl', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'What did the woman say?',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_CONVERSATION',
          Section: '3',
          // Thiếu AudioUrl
        },
        Choices: [
          { Attribute: 'A', Content: 'She said hello.', IsCorrect: true },
          { Attribute: 'B', Content: 'She said goodbye.', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Listening questions must have audio URL');
    });

    /**
     * TC_QS_CRE_009: Thất bại khi Part 1 thiếu ImageUrl
     * 
     * Test Objective: Kiểm tra validation - Part 1 (Photo Description) phải có ảnh
     * Input: Media.Section = "1" nhưng không có ImageUrl
     * Expected Output: Error "Part 1 questions must have an image"
     * Notes: Part 1 TOEIC yêu cầu sinh viên mô tả ảnh nên phải có ảnh
     */
    it('TC_QS_CRE_009: Thất bại khi câu Part 1 thiếu ImageUrl', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Look at the picture.',
        Media: {
          Skill: 'LISTENING',
          Type: 'PHOTO_DESCRIPTION',
          Section: '1',
          AudioUrl: 'https://example.com/audio.mp3',
          // Thiếu ImageUrl
        },
        Choices: [
          { Attribute: 'A', Content: 'He is standing.', IsCorrect: true },
          { Attribute: 'B', Content: 'He is sitting.', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Part 1 questions must have an image');
    });

    /**
     * TC_QS_CRE_010: Thất bại khi AudioUrl không hợp lệ
     * 
     * Test Objective: Kiểm tra validation - URL audio phải có format hợp lệ
     * Input: AudioUrl = "invalid-url" (không bắt đầu bằng http/https hoặc /)
     * Expected Output: Error "Invalid audio URL format"
     * Notes: Chấp nhận cả full URL (https://) và relative path (/)
     */
    it('TC_QS_CRE_010: Thất bại khi AudioUrl có format không hợp lệ', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test Listening question',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_CONVERSATION',
          Section: '3',
          AudioUrl: 'invalid-url-format',  // URL không hợp lệ
        },
        Choices: [
          { Attribute: 'A', Content: 'Choice A', IsCorrect: true },
          { Attribute: 'B', Content: 'Choice B', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Invalid audio URL format');
    });

    /**
     * TC_QS_CRE_011: Thất bại khi ImageUrl không hợp lệ
     * 
     * Test Objective: Kiểm tra validation - URL ảnh phải có format hợp lệ
     * Input: ImageUrl = "no-protocol-image.jpg"
     * Expected Output: Error "Invalid image URL format"
     * Notes: Tương tự AudioUrl validation
     */
    it('TC_QS_CRE_011: Thất bại khi ImageUrl có format không hợp lệ', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Look at the picture.',
        Media: {
          Skill: 'LISTENING',
          Type: 'PHOTO_DESCRIPTION',
          Section: '1',
          AudioUrl: 'https://example.com/audio.mp3',
          ImageUrl: 'no-protocol-image.jpg', // Không hợp lệ
        },
        Choices: [
          { Attribute: 'A', Content: 'He is walking.', IsCorrect: true },
          { Attribute: 'B', Content: 'He is running.', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Invalid image URL format');
    });

    /**
     * TC_QS_CRE_012: Tạo câu hỏi thành công với AudioUrl dạng relative path
     * 
     * Test Objective: Kiểm tra URL validation chấp nhận relative path (/)
     * Input: AudioUrl = "/uploads/audio/file.mp3" (relative path hợp lệ)
     * Expected Output: Tạo câu hỏi thành công
     * Notes: Local development dùng relative paths, production dùng full URLs
     */
    it('TC_QS_CRE_012: Tạo thành công với AudioUrl dạng relative path', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'What did the speaker say?',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_TALK',
          Section: '4',
          AudioUrl: '/uploads/audio/part4_q1.mp3', // Relative path hợp lệ
        },
        Choices: [
          { Attribute: 'A', Content: 'Answer A', IsCorrect: true },
          { Attribute: 'B', Content: 'Answer B', IsCorrect: false },
          { Attribute: 'C', Content: 'Answer C', IsCorrect: false },
          { Attribute: 'D', Content: 'Answer D', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      // Act
      const result = await questionService.createQuestion(createDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockQuestionRepo.create).toHaveBeenCalledTimes(1);
    });

    /**
     * TC_QS_CRE_013: Tạo câu hỏi thành công với choice content chỉ có whitespace
     * 
     * Test Objective: Kiểm tra validation trim - whitespace-only không hợp lệ
     * Input: 1 Choice có Content = "   " (chỉ khoảng trắng)
     * Expected Output: Error "All choices must have content"
     * Notes: Trim phải xảy ra trước khi kiểm tra empty
     */
    it('TC_QS_CRE_013: Thất bại khi choice content chỉ chứa khoảng trắng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Valid choice', IsCorrect: true },
          { Attribute: 'B', Content: '   ', IsCorrect: false }, // Chỉ khoảng trắng
          { Attribute: 'C', Content: 'Valid C', IsCorrect: false },
          { Attribute: 'D', Content: 'Valid D', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('All choices must have content');
    });

    /**
     * TC_QS_CRE_014: Tạo câu hỏi thành công chỉ với 2 choices (minimum)
     * 
     * Test Objective: Kiểm tra boundary - tối thiểu 2 choices là hợp lệ
     * Input: Chỉ 2 Choices
     * Expected Output: Tạo thành công
     * Notes: Boundary testing - Part 2 TOEIC có 3 choices, một số biến thể có 2
     */
    it('TC_QS_CRE_014: Tạo thành công với đúng 2 choices (biên dưới)', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Is this correct?',
        Media: { Skill: 'READING', Type: 'TEXT_COMPLETION', Section: '6' },
        Choices: [
          { Attribute: 'A', Content: 'Yes', IsCorrect: true },
          { Attribute: 'B', Content: 'No', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue({
        ...mockQuestion,
        choices: createDto.Choices,
      });

      // Act
      const result = await questionService.createQuestion(createDto, 1);

      // Assert
      expect(result).toBeDefined();
      expect(mockQuestionRepo.create).toHaveBeenCalledTimes(1);
    });
  });

  // ==================================================================
  // 2. TEST SUITE: getQuestionById - Lấy câu hỏi theo ID
  // ==================================================================
  describe('getQuestionById - Lấy câu hỏi theo ID', () => {

    /**
     * TC_QS_GET_001: Lấy câu hỏi thành công
     * 
     * Test Objective: Kiểm tra lấy câu hỏi với đầy đủ relations
     * Input: questionId = 1
     * Expected Output: Question object với media, choices (bao gồm IsCorrect)
     * Notes: Method này dành cho admin/teacher nên HIỂN THỊ IsCorrect
     */
    it('TC_QS_GET_001: Lấy câu hỏi thành công với đầy đủ thông tin', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);

      // Act
      const result = await questionService.getQuestionById(1);

      // Assert
      expect(result).toBeDefined();
      expect(result.ID).toBe(1);
      expect(result.QuestionText).toBe('What is the man doing in the picture?');
      expect(result.choices).toHaveLength(4);
      expect(result.mediaQuestion.Skill).toBe('LISTENING');
      expect(mockQuestionRepo.findById).toHaveBeenCalledWith(1);
    });

    /**
     * TC_QS_GET_002: Thất bại khi câu hỏi không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý khi ID không tìm thấy
     * Input: questionId = 999 (không tồn tại)
     * Expected Output: Error "Question not found"
     * Notes: Phải trả error rõ ràng, không trả null
     */
    it('TC_QS_GET_002: Thất bại khi câu hỏi không tồn tại', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(questionService.getQuestionById(999))
        .rejects.toThrow('Question not found');
    });
  });

  // ==================================================================
  // 3. TEST SUITE: searchQuestions - Tìm kiếm và lọc câu hỏi
  // ==================================================================
  describe('searchQuestions - Tìm kiếm và lọc câu hỏi trong ngân hàng', () => {

    /**
     * TC_QS_SRCH_001: Tìm kiếm thành công với filter Skill
     * 
     * Test Objective: Kiểm tra lọc câu hỏi theo kỹ năng (LISTENING/READING)
     * Input: filters = { Skill: "LISTENING" }
     * Expected Output: PaginatedQuestionsResponseDto với câu hỏi Listening
     * Notes: Dùng khi giáo viên muốn browse câu hỏi Listening
     */
    it('TC_QS_SRCH_001: Tìm kiếm thành công với filter Skill', async () => {
      // Arrange
      const filters = { Skill: 'LISTENING' };
      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: [mockQuestion],
        total: 1,
      });
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats);

      // Act
      const result = await questionService.searchQuestions(filters);

      // Assert
      expect(result).toBeDefined();
      expect(result.Questions).toHaveLength(1);
      expect(result.Pagination.TotalQuestions).toBe(1);
      expect(result.Pagination.CurrentPage).toBe(1);
      expect(mockQuestionRepo.findWithFilters).toHaveBeenCalledWith(
        expect.objectContaining({ Skill: 'LISTENING' })
      );
    });

    /**
     * TC_QS_SRCH_002: Tìm kiếm với pagination
     * 
     * Test Objective: Kiểm tra phân trang kết quả
     * Input: filters = { Page: 2, Limit: 10 }
     * Expected Output: Trang 2, Limit 10, TotalPages tính đúng
     * Notes: Với 25 câu hỏi, Limit 10 → TotalPages = 3
     */
    it('TC_QS_SRCH_002: Tìm kiếm với pagination chính xác', async () => {
      // Arrange
      const filters = { Page: 2, Limit: 10 };
      const mockQuestionsList = Array(10).fill(null).map((_, i) => ({
        ...mockQuestion,
        ID: i + 11,
      }));

      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: mockQuestionsList,
        total: 25,
      });
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats);

      // Act
      const result = await questionService.searchQuestions(filters);

      // Assert
      expect(result.Pagination.CurrentPage).toBe(2);
      expect(result.Pagination.Limit).toBe(10);
      expect(result.Pagination.TotalPages).toBe(3); // ceil(25/10) = 3
      expect(result.Pagination.TotalQuestions).toBe(25);
    });

    /**
     * TC_QS_SRCH_003: Tìm kiếm trả về kết quả rỗng
     * 
     * Test Objective: Kiểm tra trường hợp không tìm thấy kết quả
     * Input: filters = { SearchText: "nonexistent" }
     * Expected Output: Questions = [], TotalQuestions = 0
     * Notes: Trả mảng rỗng, không null
     */
    it('TC_QS_SRCH_003: Trả về kết quả rỗng khi không tìm thấy', async () => {
      // Arrange
      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: [],
        total: 0,
      });

      // Act
      const result = await questionService.searchQuestions({ SearchText: 'nonexistent' });

      // Assert
      expect(result.Questions).toHaveLength(0);
      expect(result.Pagination.TotalQuestions).toBe(0);
      expect(result.Pagination.TotalPages).toBe(0);
    });

    /**
     * TC_QS_SRCH_004: Tìm kiếm với default pagination
     * 
     * Test Objective: Kiểm tra giá trị pagination mặc định khi không truyền
     * Input: filters = {} (không chỉ định Page, Limit)
     * Expected Output: Page = 1, Limit = 20 (defaults)
     * Notes: Đảm bảo default values hoạt động đúng
     */
    it('TC_QS_SRCH_004: Sử dụng default pagination khi không truyền', async () => {
      // Arrange
      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: [mockQuestion],
        total: 1,
      });
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats);

      // Act
      const result = await questionService.searchQuestions({});

      // Assert
      expect(result.Pagination.CurrentPage).toBe(1);
      expect(result.Pagination.Limit).toBe(20);
    });

    /**
     * TC_QS_SRCH_005: Response DTO chứa UsageCount đúng
     * 
     * Test Objective: Kiểm tra transform có bao gồm usage statistics
     * Input: Question data + usageStats (usedInExams = 3)
     * Expected Output: QuestionListResponseDto.UsageCount = 3
     * Notes: UsageCount giúp admin biết câu hỏi nào phổ biến
     */
    it('TC_QS_SRCH_005: Response chứa UsageCount chính xác', async () => {
      // Arrange
      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: [mockQuestion],
        total: 1,
      });
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue({ usedInExams: 5 });

      // Act
      const result = await questionService.searchQuestions({});

      // Assert
      expect(result.Questions[0].UsageCount).toBe(5);
    });

    /**
     * TC_QS_SRCH_006: Response DTO chứa UsageCount = 0 khi không có stats
     * 
     * Test Objective: Kiểm tra handle null usageStats
     * Input: Question data + getUsageStats trả null
     * Expected Output: UsageCount = 0
     * Notes: Defensive coding - xử lý câu hỏi mới chưa có stats
     */
    it('TC_QS_SRCH_006: UsageCount = 0 khi không có thống kê', async () => {
      // Arrange
      (mockQuestionRepo.findWithFilters as jest.Mock).mockResolvedValue({
        questions: [mockQuestion],
        total: 1,
      });
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(null);

      // Act
      const result = await questionService.searchQuestions({});

      // Assert
      expect(result.Questions[0].UsageCount).toBe(0);
    });
  });

  // ==================================================================
  // 4. TEST SUITE: updateQuestion - Cập nhật câu hỏi
  // ==================================================================
  describe('updateQuestion - Cập nhật câu hỏi trong ngân hàng', () => {

    /**
     * TC_QS_UPD_001: Cập nhật QuestionText thành công
     * 
     * Test Objective: Kiểm tra cập nhật chỉ text của câu hỏi
     * Input: questionId = 1, updateData = { QuestionText: "Updated text" }
     * Expected Output: Question được cập nhật với text mới
     * Notes: Partial update - chỉ sửa text, giữ nguyên media và choices
     */
    it('TC_QS_UPD_001: Cập nhật QuestionText thành công', async () => {
      // Arrange
      const updateData = { QuestionText: 'Updated question text?' };
      const updatedQuestion = { ...mockQuestion, QuestionText: 'Updated question text?' };

      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats);
      (mockQuestionRepo.update as jest.Mock).mockResolvedValue(updatedQuestion);

      // Act
      const result = await questionService.updateQuestion(1, updateData, 1);

      // Assert
      expect(result.QuestionText).toBe('Updated question text?');
      expect(mockQuestionRepo.update).toHaveBeenCalledWith(
        1,
        { QuestionText: 'Updated question text?' },
        undefined,
        undefined
      );
    });

    /**
     * TC_QS_UPD_002: Thất bại khi câu hỏi không tồn tại
     * 
     * Test Objective: Kiểm tra validation - question phải tồn tại
     * Input: questionId = 999 (không tồn tại)
     * Expected Output: Error "Question not found"
     */
    it('TC_QS_UPD_002: Thất bại khi câu hỏi không tồn tại', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(questionService.updateQuestion(999, { QuestionText: 'New' }, 1))
        .rejects.toThrow('Question not found');
    });

    /**
     * TC_QS_UPD_003: Cập nhật kèm Choices - validation phải chạy
     * 
     * Test Objective: Kiểm tra validation choices cũng áp dụng khi update
     * Input: updateData.Choices = [...] với 0 correct answers
     * Expected Output: Error "Question must have exactly one correct answer"
     * Notes: Validation phải nhất quán cho cả create và update
     */
    it('TC_QS_UPD_003: Thất bại khi update choices không hợp lệ', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUnusedStats);

      const updateData = {
        Choices: [
          { Attribute: 'A', Content: 'Choice A', IsCorrect: false },
          { Attribute: 'B', Content: 'Choice B', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.updateQuestion(1, updateData, 1))
        .rejects.toThrow('Question must have exactly one correct answer');
    });

    /**
     * TC_QS_UPD_004: Cập nhật kèm Media - validation phải chạy
     * 
     * Test Objective: Kiểm tra validation media cũng áp dụng khi update
     * Input: updateData.Media = { Skill: "LISTENING" } nhưng không có AudioUrl
     * Expected Output: Error "Listening questions must have audio URL"
     */
    it('TC_QS_UPD_004: Thất bại khi update media Listening thiếu audio', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUnusedStats);

      const updateData = {
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_CONVERSATION',
          Section: '3',
          // Thiếu AudioUrl
        },
      };

      // Act & Assert
      await expect(questionService.updateQuestion(1, updateData, 1))
        .rejects.toThrow('Listening questions must have audio URL');
    });

    /**
     * TC_QS_UPD_005: Thất bại khi repository update trả null
     * 
     * Test Objective: Kiểm tra xử lý khi DB update thất bại
     * Input: Valid data nhưng repo.update trả null
     * Expected Output: Error "Failed to update question"
     * Notes: Edge case - DB write failure
     */
    it('TC_QS_UPD_005: Thất bại khi repository update trả null', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUnusedStats);
      (mockQuestionRepo.update as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(questionService.updateQuestion(1, { QuestionText: 'New' }, 1))
        .rejects.toThrow('Failed to update question');
    });

    /**
     * TC_QS_UPD_006: Cảnh báo khi update câu hỏi được sử dụng nhiều
     * 
     * Test Objective: Kiểm tra warning khi câu hỏi dùng trong nhiều đề (>5)
     * Input: Question được sử dụng trong 10 exams
     * Expected Output: Update thành công (chỉ console.warn, không block)
     * Notes: Trong production có thể yêu cầu special permission
     */
    it('TC_QS_UPD_006: Cảnh báo khi update câu hỏi dùng trong nhiều đề', async () => {
      // Arrange
      const widelyUsedStats = { ...mockUsageStats, usedInExams: 10 };
      const updatedQuestion = { ...mockQuestion, QuestionText: 'Updated' };

      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(widelyUsedStats);
      (mockQuestionRepo.update as jest.Mock).mockResolvedValue(updatedQuestion);

      // Spy on console.warn
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation();

      // Act
      const result = await questionService.updateQuestion(1, { QuestionText: 'Updated' }, 1);

      // Assert - vẫn update thành công nhưng có warning
      expect(result).toBeDefined();
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Warning: Updating question 1 which is used in 10 exams')
      );

      warnSpy.mockRestore();
    });
  });

  // ==================================================================
  // 5. TEST SUITE: deleteQuestion - Xóa câu hỏi
  // ==================================================================
  describe('deleteQuestion - Xóa câu hỏi khỏi ngân hàng', () => {

    /**
     * TC_QS_DEL_001: Xóa câu hỏi thành công (không dùng trong đề nào)
     * 
     * Test Objective: Kiểm tra xóa câu hỏi chưa được sử dụng
     * Input: questionId = 3 (usedInExams = 0)
     * Expected Output: true
     * Notes: Chỉ cho phép xóa câu hỏi chưa được dùng trong exam nào
     */
    it('TC_QS_DEL_001: Xóa câu hỏi thành công khi chưa được sử dụng', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUnusedStats);
      (mockQuestionRepo.delete as jest.Mock).mockResolvedValue(true);

      // Act
      const result = await questionService.deleteQuestion(3, 1);

      // Assert
      expect(result).toBe(true);
      expect(mockQuestionRepo.delete).toHaveBeenCalledWith(3);
    });

    /**
     * TC_QS_DEL_002: Thất bại khi câu hỏi không tồn tại
     * 
     * Test Objective: Kiểm tra xử lý xóa câu hỏi không tồn tại
     * Input: questionId = 999
     * Expected Output: Error "Question not found"
     */
    it('TC_QS_DEL_002: Thất bại khi câu hỏi không tồn tại', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(questionService.deleteQuestion(999, 1))
        .rejects.toThrow('Question not found');
    });

    /**
     * TC_QS_DEL_003: Thất bại khi câu hỏi đang được sử dụng trong exam
     * 
     * Test Objective: Kiểm tra business rule - không xóa câu hỏi đang dùng
     * Input: questionId = 1 (usedInExams = 3)
     * Expected Output: Error "Question is still referenced by exams"
     * Notes: CỐ TÌNH LÀM FAIL - Error message trong test không khớp với code thực tế
     */
    it('TC_QS_DEL_003: Thất bại khi câu hỏi đang được sử dụng trong đề thi', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats); // usedInExams = 3

      // Act & Assert
      // CỐ TÌNH FAIL: Test expect "Question is still referenced by exams"
      // nhưng code thực tế throw "Cannot delete question that is used in 3 exam(s). Remove it from all exams first."
      await expect(questionService.deleteQuestion(1, 1))
        .rejects.toThrow('Question is still referenced by exams');
      
      // Verify: Không gọi delete khi validation thất bại  
      expect(mockQuestionRepo.delete).not.toHaveBeenCalled();
    });

    /**
     * TC_QS_DEL_004: Thất bại khi câu hỏi dùng trong 1 exam
     * 
     * Test Objective: Kiểm tra boundary - kể cả 1 exam cũng không cho xóa
     * Input: usedInExams = 1
     * Expected Output: Error chứa "1 exam(s)"
     * Notes: Boundary case - ngay cả 1 exam reference cũng ngăn xóa
     */
    it('TC_QS_DEL_004: Thất bại khi câu hỏi dùng trong ít nhất 1 đề', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue({
        ...mockUnusedStats,
        usedInExams: 1,
      });

      // Act & Assert
      await expect(questionService.deleteQuestion(1, 1))
        .rejects.toThrow('Cannot delete question that is used in 1 exam(s)');
    });
  });

  // ==================================================================
  // 6. TEST SUITE: getQuestionStatistics - Lấy thống kê câu hỏi
  // ==================================================================
  describe('getQuestionStatistics - Lấy thống kê sử dụng câu hỏi', () => {

    /**
     * TC_QS_STAT_001: Lấy thống kê thành công
     * 
     * Test Objective: Kiểm tra lấy usage stats cho câu hỏi
     * Input: questionId = 1
     * Expected Output: Object với usedInExams, totalAttempts, correctPercentage, difficulty
     * Notes: Dùng cho dashboard phân tích chất lượng câu hỏi
     */
    it('TC_QS_STAT_001: Lấy thống kê câu hỏi thành công', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(mockQuestion);
      (mockQuestionRepo.getUsageStats as jest.Mock).mockResolvedValue(mockUsageStats);

      // Act
      const result = await questionService.getQuestionStatistics(1);

      // Assert
      expect(result).toBeDefined();
      expect(result.usedInExams).toBe(3);
      expect(result.totalAttempts).toBe(149); // CỐ TÌNH LÀM FAIL: Expected 149 nhưng kết quả thực tế trả về 150
      expect(result.correctPercentage).toBe(70);
      expect(result.difficulty).toBe('MEDIUM');
    });

    /**
     * TC_QS_STAT_002: Thất bại khi câu hỏi không tồn tại
     * 
     * Test Objective: Kiểm tra validation question tồn tại
     * Input: questionId = 999
     * Expected Output: Error "Question not found"
     */
    it('TC_QS_STAT_002: Thất bại khi câu hỏi không tồn tại', async () => {
      // Arrange
      (mockQuestionRepo.findById as jest.Mock).mockResolvedValue(null);

      // Act & Assert
      await expect(questionService.getQuestionStatistics(999))
        .rejects.toThrow('Question not found');
    });
  });

  // ==================================================================
  // 7. TEST SUITE: getQuestionsBySection - Lấy câu hỏi theo section
  // ==================================================================
  describe('getQuestionsBySection - Lấy câu hỏi theo Part cho luyện tập', () => {

    /**
     * TC_QS_SEC_001: Lấy câu hỏi theo section thành công
     * 
     * Test Objective: Kiểm tra lọc câu hỏi theo Part
     * Input: sections = ["5"], limit = 10
     * Expected Output: Array câu hỏi thuộc Part 5
     * Notes: Dùng cho practice mode - sinh viên chọn part muốn luyện
     */
    it('TC_QS_SEC_001: Lấy câu hỏi theo section thành công', async () => {
      // Arrange
      const questions = [mockQuestion, { ...mockQuestion, ID: 2 }];
      (mockQuestionRepo.getQuestionsBySection as jest.Mock).mockResolvedValue(questions);

      // Act
      const result = await questionService.getQuestionsBySection(['5'], 10);

      // Assert
      expect(result).toHaveLength(2);
      expect(mockQuestionRepo.getQuestionsBySection).toHaveBeenCalledWith(['5'], 10);
    });

    /**
     * TC_QS_SEC_002: Lấy câu hỏi từ nhiều sections
     * 
     * Test Objective: Kiểm tra lọc nhiều sections cùng lúc
     * Input: sections = ["5", "6", "7"]
     * Expected Output: Câu hỏi từ Part 5, 6, 7
     * Notes: Sinh viên có thể chọn practice nhiều parts cùng lúc
     */
    it('TC_QS_SEC_002: Lấy câu hỏi từ nhiều sections cùng lúc', async () => {
      // Arrange
      (mockQuestionRepo.getQuestionsBySection as jest.Mock).mockResolvedValue([
        mockQuestion,
        mockQuestionReading,
      ]);

      // Act
      const result = await questionService.getQuestionsBySection(['5', '6', '7']);

      // Assert
      expect(result).toHaveLength(2);
      expect(mockQuestionRepo.getQuestionsBySection).toHaveBeenCalledWith(
        ['5', '6', '7'],
        undefined
      );
    });

    /**
     * TC_QS_SEC_003: Thất bại khi sections rỗng
     * 
     * Test Objective: Kiểm tra validation - phải chỉ định ít nhất 1 section
     * Input: sections = []
     * Expected Output: Error "At least one section must be specified"
     * Notes: Tránh query toàn bộ DB
     */
    it('TC_QS_SEC_003: Thất bại khi mảng sections rỗng', async () => {
      // Act & Assert
      await expect(questionService.getQuestionsBySection([]))
        .rejects.toThrow('At least one section must be specified');
    });

    /**
     * TC_QS_SEC_004: Thất bại khi sections là null/undefined
     * 
     * Test Objective: Kiểm tra xử lý null input
     * Input: sections = null
     * Expected Output: Error "At least one section must be specified"
     */
    it('TC_QS_SEC_004: Thất bại khi sections là null', async () => {
      // Act & Assert
      await expect(questionService.getQuestionsBySection(null as any))
        .rejects.toThrow('At least one section must be specified');
    });
  });

  // ==================================================================
  // 8. TEST SUITE: performBulkOperation - Thao tác hàng loạt
  // ==================================================================
  describe('performBulkOperation - Thao tác hàng loạt trên câu hỏi', () => {

    /**
     * TC_QS_BULK_001: Bulk DELETE thành công
     * 
     * Test Objective: Kiểm tra xóa nhiều câu hỏi cùng lúc
     * Input: Operation = "DELETE", QuestionIDs = [1, 2, 3]
     * Expected Output: { success: 3, failed: 0, errors: [] }
     * Notes: Hiệu quả hơn xóa từng câu một
     */
    it('TC_QS_BULK_001: Bulk DELETE thành công', async () => {
      // Arrange
      const operation = {
        Operation: 'DELETE',
        QuestionIDs: [1, 2, 3],
      };

      (mockQuestionRepo.bulkDelete as jest.Mock).mockResolvedValue(3);

      // Act
      const result = await questionService.performBulkOperation(operation, 1);

      // Assert
      expect(result.success).toBe(3);
      expect(result.failed).toBe(0);
      expect(result.errors).toHaveLength(0);
    });

    /**
     * TC_QS_BULK_002: Bulk DELETE thất bại - repository ném lỗi
     * 
     * Test Objective: Kiểm tra xử lý lỗi khi bulk delete fails
     * Input: Operation = "DELETE", QuestionIDs = [1, 2]
     * Expected Output: { success: 0, failed: 2, errors: ["Bulk delete failed: ..."] }
     * Notes: Handle database errors gracefully
     */
    it('TC_QS_BULK_002: Xử lý lỗi khi bulk DELETE thất bại', async () => {
      // Arrange
      const operation = {
        Operation: 'DELETE',
        QuestionIDs: [1, 2],
      };

      (mockQuestionRepo.bulkDelete as jest.Mock).mockRejectedValue(
        new Error('Foreign key constraint violation')
      );

      // Act
      const result = await questionService.performBulkOperation(operation, 1);

      // Assert
      expect(result.success).toBe(0);
      expect(result.failed).toBe(2);
      expect(result.errors[0]).toContain('Bulk delete failed');
    });

    /**
     * TC_QS_BULK_003: ADD_TO_EXAM - Trả not implemented
     * 
     * Test Objective: Kiểm tra operation ADD_TO_EXAM được redirect sang ExamService
     * Input: Operation = "ADD_TO_EXAM", QuestionIDs = [1, 2]
     * Expected Output: { failed: 2, errors: ["should be handled by ExamService"] }
     * Notes: Separation of concerns - thêm vào exam xử lý bởi ExamService
     */
    it('TC_QS_BULK_003: ADD_TO_EXAM redirect sang ExamService', async () => {
      // Arrange
      const operation = {
        Operation: 'ADD_TO_EXAM',
        QuestionIDs: [1, 2],
      };

      // Act
      const result = await questionService.performBulkOperation(operation, 1);

      // Assert
      expect(result.failed).toBe(2);
      expect(result.errors[0]).toContain('should be handled by ExamService');
    });

    /**
     * TC_QS_BULK_004: Operation không hợp lệ
     * 
     * Test Objective: Kiểm tra xử lý operation type không hợp lệ
     * Input: Operation = "INVALID_OP"
     * Expected Output: { failed: N, errors: ["Unknown operation: INVALID_OP"] }
     * Notes: Defensive programming - xử lý gracefully thay vì crash
     */
    it('TC_QS_BULK_004: Xử lý operation không hợp lệ', async () => {
      // Arrange
      const operation = {
        Operation: 'INVALID_OP',
        QuestionIDs: [1],
      };

      // Act
      const result = await questionService.performBulkOperation(operation, 1);

      // Assert
      expect(result.failed).toBe(1);
      expect(result.errors[0]).toContain('Unknown operation: INVALID_OP');
    });
  });

  // ==================================================================
  // 9. TEST SUITE: validateChoices (Private) - Kiểm tra qua createQuestion
  // ==================================================================
  describe('validateChoices - Validation logic cho choices (kiểm tra gián tiếp)', () => {

    /**
     * TC_QS_VC_001: Validate choices với chính xác 1 đáp án đúng
     * 
     * Test Objective: Kiểm tra validation chấp nhận đúng 1 correct
     * Input: 4 choices, 1 IsCorrect = true
     * Expected Output: Không ném lỗi, tạo thành công
     * Notes: Happy path cho trường hợp standard TOEIC
     */
    it('TC_QS_VC_001: Chấp nhận choices hợp lệ với 1 đáp án đúng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Valid question?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Option A', IsCorrect: false },
          { Attribute: 'B', Content: 'Option B', IsCorrect: true },
          { Attribute: 'C', Content: 'Option C', IsCorrect: false },
          { Attribute: 'D', Content: 'Option D', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      // Act & Assert - Không ném lỗi
      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });

    /**
     * TC_QS_VC_002: Reject 3 choices đều correct
     * 
     * Test Objective: Kiểm tra reject khi quá nhiều correct answers
     * Input: 4 choices, 3 IsCorrect = true
     * Expected Output: Error "exactly one correct answer"
     * Notes: Edge case - nhiều đáp án đúng
     */
    it('TC_QS_VC_002: Reject khi có 3 đáp án đúng', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test?',
        Media: { Skill: 'READING', Type: 'INCOMPLETE_SENTENCE', Section: '5' },
        Choices: [
          { Attribute: 'A', Content: 'Opt A', IsCorrect: true },
          { Attribute: 'B', Content: 'Opt B', IsCorrect: true },
          { Attribute: 'C', Content: 'Opt C', IsCorrect: true },
          { Attribute: 'D', Content: 'Opt D', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Question must have exactly one correct answer');
    });
  });

  // ==================================================================
  // 10. TEST SUITE: validateMediaRequirements (Private) - Media validation
  // ==================================================================
  describe('validateMediaRequirements - Validation media theo loại câu hỏi', () => {

    /**
     * TC_QS_VM_001: Reading question không cần audio - pass
     * 
     * Test Objective: Kiểm tra Reading không bắt buộc AudioUrl
     * Input: Skill = "READING", không có AudioUrl
     * Expected Output: Tạo thành công (không lỗi)
     * Notes: Chỉ LISTENING mới bắt buộc audio
     */
    it('TC_QS_VM_001: Reading question không cần AudioUrl', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Reading question',
        Media: { Skill: 'READING', Type: 'READING_COMPREHENSION', Section: '7' },
        Choices: [
          { Attribute: 'A', Content: 'Opt A', IsCorrect: true },
          { Attribute: 'B', Content: 'Opt B', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestionReading);

      // Act & Assert
      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });

    /**
     * TC_QS_VM_002: Part 2+ Listening không cần ImageUrl - pass  
     * 
     * Test Objective: Kiểm tra Part 2-4 Listening không bắt buộc ảnh
     * Input: Section = "3", có AudioUrl, KHÔNG có ImageUrl
     * Expected Output: Tạo thành công
     * Notes: Chỉ Part 1 mới bắt buộc ảnh
     */
    it('TC_QS_VM_002: Listening Part 3 không cần ImageUrl', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'What does the woman suggest?',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_CONVERSATION',
          Section: '3',
          AudioUrl: 'https://example.com/audio.mp3',
          // Không có ImageUrl - OK cho Part 3
        },
        Choices: [
          { Attribute: 'A', Content: 'Opt A', IsCorrect: true },
          { Attribute: 'B', Content: 'Opt B', IsCorrect: false },
          { Attribute: 'C', Content: 'Opt C', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      // Act
      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });
  });

  // ==================================================================
  // 11. TEST SUITE: isValidUrl (Private) - URL validation
  // ==================================================================
  describe('isValidUrl - Kiểm tra format URL (kiểm tra gián tiếp)', () => {

    /**
     * TC_QS_URL_001: Chấp nhận URL https
     * 
     * Test Objective: Kiểm tra URL bắt đầu bằng https:// là hợp lệ
     * Input: AudioUrl = "https://cdn.example.com/audio.mp3"
     * Expected Output: Tạo thành công
     */
    it('TC_QS_URL_001: Chấp nhận URL https', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_TALK',
          Section: '4',
          AudioUrl: 'https://cdn.example.com/audio.mp3',
        },
        Choices: [
          { Attribute: 'A', Content: 'A', IsCorrect: true },
          { Attribute: 'B', Content: 'B', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });

    /**
     * TC_QS_URL_002: Chấp nhận URL http
     * 
     * Test Objective: Kiểm tra URL bắt đầu bằng http:// là hợp lệ
     * Input: AudioUrl = "http://localhost:3001/audio.mp3"
     * Expected Output: Tạo thành công
     * Notes: Development environment thường dùng http
     */
    it('TC_QS_URL_002: Chấp nhận URL http', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_TALK',
          Section: '4',
          AudioUrl: 'http://localhost:3001/audio.mp3',
        },
        Choices: [
          { Attribute: 'A', Content: 'A', IsCorrect: true },
          { Attribute: 'B', Content: 'B', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });

    /**
     * TC_QS_URL_003: Chấp nhận relative path
     * 
     * Test Objective: Kiểm tra path bắt đầu bằng / là hợp lệ
     * Input: AudioUrl = "/uploads/audio/file.mp3"
     * Expected Output: Tạo thành công
     * Notes: Local uploads sử dụng relative paths
     */
    it('TC_QS_URL_003: Chấp nhận relative path', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_TALK',
          Section: '4',
          AudioUrl: '/uploads/audio/file.mp3',
        },
        Choices: [
          { Attribute: 'A', Content: 'A', IsCorrect: true },
          { Attribute: 'B', Content: 'B', IsCorrect: false },
        ],
      };

      (mockQuestionRepo.create as jest.Mock).mockResolvedValue(mockQuestion);

      const result = await questionService.createQuestion(createDto, 1);
      expect(result).toBeDefined();
    });

    /**
     * TC_QS_URL_004: Reject URL không hợp lệ
     * 
     * Test Objective: Kiểm tra URL không bắt đầu bằng http/https/slash bị reject
     * Input: AudioUrl = "ftp://server/file.mp3"
     * Expected Output: Error "Invalid audio URL format"
     * Notes: Chỉ chấp nhận http, https, hoặc /
     */
    it('TC_QS_URL_004: Reject URL không bắt đầu bằng http/https hoặc /', async () => {
      // Arrange
      const createDto = {
        QuestionText: 'Test',
        Media: {
          Skill: 'LISTENING',
          Type: 'SHORT_TALK',
          Section: '4',
          AudioUrl: 'ftp://server/file.mp3', // FTP không hợp lệ
        },
        Choices: [
          { Attribute: 'A', Content: 'A', IsCorrect: true },
          { Attribute: 'B', Content: 'B', IsCorrect: false },
        ],
      };

      // Act & Assert
      await expect(questionService.createQuestion(createDto, 1))
        .rejects.toThrow('Invalid audio URL format');
    });
  });
});
