package com.mxhieu.doantotnghiep.service.impl;

import com.mxhieu.doantotnghiep.dto.request.TestProgressRequest;
import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.LessonProgressService;
import com.mxhieu.doantotnghiep.service.LessonService;
import com.mxhieu.doantotnghiep.service.TrackService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit Test cho TestProgressServiceImpl.
 *
 * Chức năng kiểm thử:
 *   - checkCompletionCondition(): kiểm tra điều kiện hoàn thành bài test
 *     (totalScore >= 50 mới coi là pass)
 *
 * Framework: JUnit 5 + Mockito
 * Rollback: Toàn bộ repository được mock — không có thao tác DB thật.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestProgressServiceImpl Tests")
class TestProgressServiceImplTest {

    // ==================== Mocks ====================
    @Mock private TestProgressRepository testProgressRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private TestRepository testRepository;
    @Mock private TestAttemptRepository testAttemptRepository;
    @Mock private LessonService lessonService;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonProgressService lessonProgressService;
    @Mock private TrackService trackService;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private TestProgressServiceImpl testProgressService;

    // ==================== Dữ liệu dùng chung ====================
    private StudentProfileEntity student;
    private TestEntity testEntity;
    private TestProgressEntity testProgress;
    private TestProgressRequest request;
    private ModuleEntity module;
    private CourseEntity course;

    /**
     * Khởi tạo dữ liệu mẫu trước mỗi test case.
     */
    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder().id(1).fullName("Tran Thi B").build();

        TrackEntity track = TrackEntity.builder().id(1).name("Track 0-300").build();
        course = CourseEntity.builder().id(5).title("TOEIC Listening").track(track).build();
        module = ModuleEntity.builder().id(20).title("Module Test").course(course).build();

        testEntity = TestEntity.builder()
                .id(50)
                .name("Mid-term Test")
                .module(module)
                .build();

        testProgress = TestProgressEntity.builder()
                .id(200)
                .test(testEntity)
                .studentProfile(student)
                .process(0)
                .build();

        request = TestProgressRequest.builder()
                .testId(50)
                .studentprofileId(1)
                .build();
    }

    // =========================================================
    // TC-TP-02: Chưa hoàn thành khi điểm cao nhất < 50
    // =========================================================
    /**
     * TC-TP-02
     * Objective: Xác nhận trả về false khi điểm cao nhất < 50 (fail)
     * Input: totalScore = 30
     * Expected: false, process được set = 1 (IN_PROGRESS)
     */
    @Test
    @DisplayName("TC-TP-02: Trả về false khi totalScore < 50")
    void checkCompletionCondition_WhenScoreFail_ShouldReturnFalse() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(30).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));

        // Act
        Boolean result = testProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isFalse();
        assertThat(testProgress.getProcess()).isEqualTo(1);
        verify(testProgressRepository).save(testProgress);
        verify(lessonService, never()).getNextLessonOrTest(any(LessonOrTestAroundRequest.class));
        verify(lessonProgressService, never()).unLockNextCourse(any(CourseEntity.class), any(StudentProfileEntity.class));
    }

    // =========================================================
    // TC-TP-03: Điểm đúng bằng 50 → pass (boundary)
    // =========================================================
    /**
     * TC-TP-03
     * Objective: Kiểm tra điều kiện biên — đúng bằng 50 điểm thì pass
     * Input: totalScore = 50
     * Expected: true, process=2
     */
    @Test
    @DisplayName("TC-TP-03: Điểm đúng bằng 50 (boundary) → trả về true")
    void checkCompletionCondition_WhenScoreExactly50_ShouldReturnTrue() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(50).build();
        TestEntity nextTest = TestEntity.builder().id(51).name("Next Test").build();
        TestProgressEntity existingNextProgress = TestProgressEntity.builder()
                .id(201).test(nextTest).studentProfile(student).process(0).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        // Mock unLockNext để không bị NPE
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(51).type("TEST").build());
        when(testRepository.findById(51)).thenReturn(Optional.of(nextTest));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(51, 1))
                .thenReturn(List.of(existingNextProgress));

        // Act
        Boolean result = testProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isTrue();
        assertThat(testProgress.getProcess()).isEqualTo(2);
        verify(testProgressRepository).save(testProgress);
        verify(lessonProgressService, never()).unLockNextCourse(any(CourseEntity.class), any(StudentProfileEntity.class));
    }

    // =========================================================
    // TC-TP-04: Điểm 49 → fail (boundary)
    // =========================================================
    /**
     * TC-TP-04
     * Objective: Kiểm tra điều kiện biên — 49 điểm thì fail
     * Input: totalScore = 49
     * Expected: false, process=1
     */
    @Test
    @DisplayName("TC-TP-04: Điểm 49 (dưới boundary) → trả về false")
    void checkCompletionCondition_WhenScore49_ShouldReturnFalse() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(49).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));

        // Act
        Boolean result = testProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isFalse();
        assertThat(testProgress.getProcess()).isEqualTo(1);
        verify(testProgressRepository).save(testProgress);
    }

    // =========================================================
    // TC-TP-05: Ném exception khi chưa có test progress record
    // =========================================================
    /**
     * TC-TP-05
     * Objective: Xác nhận ném AppException khi học viên chưa có bản ghi test progress
     * Input: testProgressRepository trả về list rỗng
     * Expected: AppException với ErrorCode.TEST_PROGRESS_NOT_EXISTS
     */
    @Test
    @DisplayName("TC-TP-05: Ném AppException khi không có test progress record")
    void checkCompletionCondition_WhenProgressNotExists_ShouldThrowException() {
        // Arrange
        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> testProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TEST_PROGRESS_NOT_EXISTS);
        verify(testProgressRepository, never()).save(any(TestProgressEntity.class));
    }

    // =========================================================
    // TC-TP-06: Ném exception khi chưa có lần thử nào (attempt)
    // =========================================================
    /**
     * TC-TP-06
     * Objective: Xác nhận ném AppException khi học viên chưa làm bài test lần nào
     * Input: testAttemptRepository trả về Optional.empty()
     * Expected: AppException với ErrorCode.TEST_ATTEMPT_NOT_FOUND
     */
    @Test
    @DisplayName("TC-TP-06: Ném AppException khi chưa có attempt nào")
    void checkCompletionCondition_WhenNoAttempt_ShouldThrowException() {
        // Arrange
        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> testProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TEST_ATTEMPT_NOT_FOUND);
        verify(testProgressRepository, never()).save(any(TestProgressEntity.class));
    }

    // =========================================================
    // TC-TP-07: Ném exception khi không tìm thấy test
    // =========================================================
    /**
     * TC-TP-07
     * Objective: Xác nhận ném AppException khi testId không tồn tại
     * Input: testId = 999
     * Expected: AppException với ErrorCode.TEST_NOT_FOUND
     */
    @Test
    @DisplayName("TC-TP-07: Ném AppException khi không tìm thấy test")
    void checkCompletionCondition_WhenTestNotFound_ShouldThrowException() {
        // Arrange
        request.setTestId(999);
        when(testRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> testProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TEST_NOT_FOUND);
    }

    // =========================================================
    // TC-TP-14: unLockNext tạo progress mới cho TEST tiếp theo
    // =========================================================
    /**
     * TC-TP-14
     * Objective: Khi next item là TEST và chưa có progress record, hệ thống phải tạo mới
     * Input: getNextLessonOrTest trả về type="TEST", testProgressRepository trả về empty
     * Expected: testProgressRepository.save() được gọi để tạo mới progress với process=0
     */
    @Test
    @DisplayName("TC-TP-14: unLockNext tạo progress mới cho TEST tiếp theo khi chưa có record")
    void checkCompletionCondition_WhenNextTestHasNoProgress_ShouldCreateNewProgress() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();
        TestEntity nextTest = TestEntity.builder().id(51).name("Next test").build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(51).type("TEST").build());
        when(testRepository.findById(51)).thenReturn(Optional.of(nextTest));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(51, 1))
                .thenReturn(Collections.emptyList());

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        verify(testProgressRepository).save(argThat(tp ->
                tp.getTest().equals(nextTest)
                        && tp.getStudentProfile().equals(student)
                        && tp.getProcess() == 0
        ));
    }

    // =========================================================
    // TC-TP-15: unLockNext fallback khi next LESSON không tồn tại
    // =========================================================
    /**
     * TC-TP-15
     * Objective: Khi next item là LESSON nhưng findById() trả về empty,
     *            hệ thống phải fallback sang unLockNextCourse()
     * Input: next item là LESSON id=999, findById(999) = Optional.empty()
     * Expected: lessonProgressService.unLockNextCourse() được gọi
     */
    @Test
    @DisplayName("TC-TP-15: unLockNext fallback unLockNextCourse khi next LESSON không tồn tại")
    void checkCompletionCondition_WhenNextLessonNotFound_ShouldFallbackUnlockCourse() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(999).type("LESSON").build());
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        verify(lessonProgressService).unLockNextCourse(eq(course), eq(student));
    }

    // =========================================================
    // TC-TP-09: unLockNext mở LESSON tiếp theo (nhánh LESSON)
    // =========================================================
    /**
     * TC-TP-09
     * Objective: Khi test pass và item tiếp theo là LESSON, tạo LessonProgressEntity mới
     * Input: getNextLessonOrTest trả về type="LESSON", lesson chưa có progress
     * Expected: lessonProgressRepository.save() được gọi với new LessonProgressEntity
     */
    @Test
    @DisplayName("TC-TP-09: unLockNext tạo progress mới cho LESSON tiếp theo")
    void checkCompletionCondition_WhenNextIsLesson_ShouldCreateLessonProgress() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();
        LessonEntity nextLesson = LessonEntity.builder().id(20).title("Lesson tiếp theo").build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        // unLockNext: next là LESSON
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(20).type("LESSON").build());
        when(lessonRepository.findById(20)).thenReturn(Optional.of(nextLesson));
        // lesson chưa có progress → tạo mới
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(20, 1))
                .thenReturn(Collections.emptyList());

        // Act
        Boolean result = testProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isTrue();
        // LessonProgressEntity mới được tạo và save
        verify(lessonProgressRepository).save(argThat(lp ->
                lp.getLesson().equals(nextLesson)
                && lp.getStudentProfile().equals(student)
                && lp.getProcess() == 0
        ));
    }

    // =========================================================
    // TC-TP-10: unLockNext khi LESSON tiếp theo đã có progress → không tạo mới
    // =========================================================
    /**
     * TC-TP-10
     * Objective: Khi LESSON tiếp theo đã có progress record → KHÔNG tạo thêm
     * Input: lessonProgressRepository trả về list có sẵn 1 record
     * Expected: lessonProgressRepository.save() KHÔNG được gọi thêm lần nào cho lesson mới
     */
    @Test
    @DisplayName("TC-TP-10: unLockNext không tạo progress mới nếu LESSON tiếp theo đã có progress")
    void checkCompletionCondition_WhenNextLessonAlreadyHasProgress_ShouldNotCreateDuplicate() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();
        LessonEntity nextLesson = LessonEntity.builder().id(20).build();
        LessonProgressEntity existingProgress = LessonProgressEntity.builder()
                .id(99).lesson(nextLesson).studentProfile(student).process(0).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(20).type("LESSON").build());
        when(lessonRepository.findById(20)).thenReturn(Optional.of(nextLesson));
        // lesson ĐÃ có progress → không tạo mới
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(20, 1))
                .thenReturn(List.of(existingProgress));

        // Act
        testProgressService.checkCompletionCondition(request);

        // Assert: lessonProgressRepository.save KHÔNG được gọi cho lesson mới
        verify(lessonProgressRepository, never()).save(any(LessonProgressEntity.class));
    }

    // =========================================================
    // TC-TP-11: unLockNext khi TEST tiếp theo đã có progress → không tạo mới
    // =========================================================
    /**
     * TC-TP-11
     * Objective: Khi TEST tiếp theo đã có progress record → KHÔNG tạo thêm
     * Input: testProgressRepository cho test tiếp theo trả về list có sẵn
     * Expected: testProgressRepository.save() chỉ gọi 1 lần (cho current test, không cho next)
     */
    @Test
    @DisplayName("TC-TP-11: unLockNext không tạo progress mới nếu TEST tiếp theo đã có progress")
    void checkCompletionCondition_WhenNextTestAlreadyHasProgress_ShouldNotCreateDuplicate() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();
        TestEntity nextTest = TestEntity.builder().id(51).name("Test tiếp theo").build();
        TestProgressEntity nextTestProgress = TestProgressEntity.builder()
                .id(201).test(nextTest).studentProfile(student).process(0).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(51).type("TEST").build());
        when(testRepository.findById(51)).thenReturn(Optional.of(nextTest));
        // test tiếp theo ĐÃ có progress
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(51, 1))
                .thenReturn(List.of(nextTestProgress));

        // Act
        testProgressService.checkCompletionCondition(request);

        // Assert: save chỉ 1 lần cho current testProgress, không thêm cho nextTest
        verify(testProgressRepository, times(1)).save(testProgress);
        verify(testProgressRepository, never()).save(nextTestProgress);
    }

    // =========================================================
    // TC-TP-12: unLockNext gọi unLockNextCourse khi không có item tiếp theo
    // =========================================================
    /**
     * TC-TP-12
     * Objective: Khi test là item cuối khóa học, unLockNext bắt AppException
     *            và gọi lessonProgressService.unLockNextCourse()
     * Input: getNextLessonOrTest ném AppException (không có next)
     * Expected: lessonProgressService.unLockNextCourse() được gọi 1 lần
     */
    @Test
    @DisplayName("TC-TP-12: unLockNext gọi unLockNextCourse khi test là item cuối")
    void checkCompletionCondition_WhenNoNextItem_ShouldCallUnLockNextCourse() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(80).build();
        CourseEntity course = CourseEntity.builder().id(5).title("TOEIC Listening").build();
        ModuleEntity moduleWithCourse = ModuleEntity.builder().id(20).course(course).build();
        testEntity.setModule(moduleWithCourse);

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        // Không có next item → ném AppException
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenThrow(new AppException(ErrorCode.LESSON_NOT_HAS_NEXT));

        // Act
        Boolean result = testProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isTrue();
        // unLockNextCourse được gọi do catch AppException
        verify(lessonProgressService).unLockNextCourse(eq(course), eq(student));
    }

    // =========================================================
    // TC-TP-13: Ném exception khi student không tồn tại
    // =========================================================
    /**
     * TC-TP-13
     * Objective: Xác nhận ném AppException khi studentId không tồn tại
     * Input: studentprofileId=999 (không tồn tại)
     * Expected: AppException với ErrorCode.STUDENT_PROFILE_NOT_FOUND
     */
    @Test
    @DisplayName("TC-TP-13: Ném AppException khi không tìm thấy student profile")
    void checkCompletionCondition_WhenStudentNotFound_ShouldThrowException() {
        // Arrange
        request.setStudentprofileId(999);
        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> testProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDENT_PROFILE_NOT_FOUND);
    }

    // =========================================================
    // TC-TP-17: totalScore=null được xử lý như 0 
    // =========================================================
    /**
     * TC-TP-17
     * Objective: Khi totalScore=null, hệ thống KHÔNG crash mà xử lý như 0
     * Input: totalScore = null
     * Expected: Trả về false (do 0 < 50), process = 1 (IN_PROGRESS)
     */
    @Test
    @DisplayName("TC-TP-17: totalScore=null phải được xem là 0 thay vì crash")
    void checkCompletionCondition_WhenAttemptScoreIsNull_ShouldTreatAsZero() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(null).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isFalse();
        assertThat(testProgress.getProcess()).isEqualTo(1);
    }

    // =========================================================
    // TC-TP-18: getNextLessonOrTest trả null 
    // =========================================================
    /**
     * TC-TP-18 
     * Objective: Khi getNextLessonOrTest trả về null, hệ thống KHÔNG ném NPE
     *            mà gọi fallback unLockNextCourse()
     * Input: lessonService.getNextLessonOrTest() = null
     * Expected: lessonProgressService.unLockNextCourse() được gọi
     */
    @Test
    @DisplayName("TC-TP-18: getNextLessonOrTest trả null phải fallback unlock course thay vì NPE")
    void checkCompletionCondition_WhenNextItemResponseIsNull_ShouldFallbackUnlockCourse() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(null);

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        verify(lessonProgressService).unLockNextCourse(eq(course), eq(student));
    }

    // =========================================================
    // TC-TP-19: next item type=null 
    // =========================================================
    /**
     * TC-TP-19
     * Objective: Khi next item có type=null, hệ thống KHÔNG ném NPE
     *            mà gọi fallback unLockNextCourse()
     * Input: getNextLessonOrTest trả về response có type=null
     * Expected: lessonProgressService.unLockNextCourse() được gọi
     */
    @Test
    @DisplayName("TC-TP-19: next item type=null phải fallback unlock course thay vì NPE")
    void checkCompletionCondition_WhenNextItemTypeIsNull_ShouldFallbackUnlockCourse() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(51).type(null).build());

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        verify(lessonProgressService).unLockNextCourse(eq(course), eq(student));
    }

    // =========================================================
    // TC-TP-20: test.module=null không gây crash 
    // =========================================================
    /**
     * TC-TP-20 
     * Objective: Khi fallback unLockNextCourse được gọi nhưng test.module=null,
     *            hệ thống KHÔNG ném NPE
     * Input: test.module=null, không có next item
     * Expected: Trả về true, không gọi unLockNextCourse() vì không có course
     */
    @Test
    @DisplayName("TC-TP-20: fallback không được crash khi test.module=null")
    void checkCompletionCondition_WhenNoNextItemAndModuleIsNull_ShouldReturnTrueWithoutNpe() {
        testEntity.setModule(null);
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenThrow(new AppException(ErrorCode.LESSON_NOT_HAS_NEXT));

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(testProgress.getProcess()).isEqualTo(2);
    }

    // =========================================================
    // TC-TP-21: Xử lý duplicate current progress records 
    // =========================================================
    /**
     * TC-TP-21
     * Objective: Khi có nhiều record progress trùng lặp cho cùng 1 test,
     *            hệ thống cập nhật ĐỒNG BỘ tất cả các record đó
     * Input: findByTest_IdAndStudentProfile_Id trả về 2 records
     * Expected: Cả 2 record đều được cập nhật process=2 (DONE)
     */
    @Test
    @DisplayName("TC-TP-21: duplicate current progress records phải được cập nhật đồng bộ")
    void checkCompletionCondition_WhenDuplicateCurrentProgressRecords_ShouldUpdateAllRecords() {
        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .id(1).test(testEntity).studentProfile(student).totalScore(75).build();
        TestProgressEntity duplicateProgress = TestProgressEntity.builder()
                .id(201).test(testEntity).studentProfile(student).process(0).build();
        TestEntity nextTest = TestEntity.builder().id(51).name("Next Test").build();
        TestProgressEntity existingNextProgress = TestProgressEntity.builder()
                .id(202).test(nextTest).studentProfile(student).process(0).build();

        when(testRepository.findById(50)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(50, 1))
                .thenReturn(List.of(testProgress, duplicateProgress));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(50, 1))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(51).type("TEST").build());
        when(testRepository.findById(51)).thenReturn(Optional.of(nextTest));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(51, 1))
                .thenReturn(List.of(existingNextProgress));

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(testProgress.getProcess()).isEqualTo(2);
        assertThat(duplicateProgress.getProcess()).isEqualTo(2);
        verify(testProgressRepository).save(duplicateProgress);
    }
}
