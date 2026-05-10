package com.mxhieu.doantotnghiep.service.impl;

import com.mxhieu.doantotnghiep.dto.request.LessonProgressRequest;
import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.LessonService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit Test cho LessonProgressServiceImpl.
 *
 * Chức năng kiểm thử:
 *   - checkCompletionCondition(): kiểm tra điều kiện hoàn thành bài học
 *   - unLockNextCourse(): mở khóa khóa học tiếp theo
 *   - unLockNextTrack(): mở khóa track tiếp theo
 *
 * Framework: JUnit 5 + Mockito
 * Chiến lược: Pure unit test (không cần DB, không cần Spring context)
 * Rollback: Không có trạng thái DB thật — mock toàn bộ repository
 *           → DB không bị thay đổi trong quá trình test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LessonProgressServiceImpl Tests")
class LessonProgressServiceImplTest {

    // ==================== Mocks ====================
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private TestRepository testRepository;
    @Mock private TestProgressRepository testProgressRepository;
    @Mock private EnrollmentCourseRepository enrollmentCourseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private TrackRepository trackRepository;
    @Mock private LessonService lessonService;

    @InjectMocks
    private LessonProgressServiceImpl lessonProgressService;

    // ==================== Dữ liệu dùng chung ====================
    private StudentProfileEntity student;
    private LessonEntity lesson;
    private LessonProgressEntity lessonProgress;
    private LessonProgressRequest request;

    /**
     * Khởi tạo dữ liệu trước mỗi test case.
     * Tạo đối tượng giả lập không liên quan đến DB thật.
     */
    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder().id(1).fullName("Nguyen Van A").build();

        lesson = LessonEntity.builder()
                .id(10)
                .title("Lesson 1")
                .gatingRules(80)           // phải xem >= 80% video
                .exercises(new ArrayList<>())
                .build();

        lessonProgress = LessonProgressEntity.builder()
                .id(100)
                .lesson(lesson)
                .studentProfile(student)
                .percentageWatched(0)
                .process(0)
                .build();

        request = LessonProgressRequest.builder()
                .lessonId(10)
                .studentProfileId(1)
                .percentageWatched(90)
                .build();
    }

    // =========================================================
    // TC-LP-01: Hoàn thành bài học khi đủ % xem và không có exercise
    // =========================================================
    /**
     * TC-LP-01
     * Objective: Xác nhận trả về true khi học viên xem đủ % và bài học không có exercise
     * Input: percentageWatched=90, gatingRules=80, exercises=[]
     * Expected: true, process được set = 2 (DONE), repository.save() được gọi
     */
    @Test
    @DisplayName("TC-LP-01: Hoan thanh khi du % xem va khong co exercise")
    void checkCompletionCondition_WhenWatchedEnoughAndNoExercise_ShouldReturnTrue() {
        lesson.setExercises(new ArrayList<>());
        lessonProgress.setPercentageWatched(90);
        request.setPercentageWatched(90);

        LessonOrTestAroundResponse nextItem = LessonOrTestAroundResponse.builder()
                .Id(11).type("LESSON").build();
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(nextItem);
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        Boolean result = lessonProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(lessonProgress.getProcess()).isEqualTo(2);
        verify(lessonProgressRepository).save(lessonProgress);
    }
    // =========================================================
    // TC-LP-02: Chưa hoàn thành khi % xem chưa đủ
    // =========================================================
    /**
     * TC-LP-02
     * Objective: Xác nhận trả về false khi % xem nhỏ hơn ngưỡng yêu cầu
     * Input: percentageWatched=50, gatingRules=80
     * Expected: false, process được set = 1 (IN_PROGRESS)
     */
    @Test
    @DisplayName("TC-LP-02: Chưa hoàn thành khi % xem chưa đủ ngưỡng (50 < gatingRules=80)")
    void checkCompletionCondition_WhenWatchedNotEnough_ShouldReturnFalse() {
        // Arrange
        // currentWatched=50, newWatched=50: 50 < 50 = false → không update
        // checkCompleted: 50 < gatingRules(80) = true → return false
        lessonProgress.setPercentageWatched(50);
        request.setPercentageWatched(50);
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert: chưa đủ % → false, process=IN_PROGRESS(1), percentageWatched không tăng
        assertThat(result).isFalse();
        assertThat(lessonProgress.getProcess()).isEqualTo(1);
        assertThat(lessonProgress.getPercentageWatched()).isEqualTo(50); // không bị thay đổi
        verify(lessonProgressRepository).save(lessonProgress);
    }

    // =========================================================
    // TC-LP-03: Ném exception khi chưa có lesson progress record
    // =========================================================
    /**
     * TC-LP-03
     * Objective: Xác nhận ném AppException khi không tồn tại lesson progress
     * Input: Không có bản ghi nào trong DB cho cặp (lessonId, studentId)
     * Expected: AppException với ErrorCode.LESSON_PROGRESS_NOT_EXISTS
     */
    @Test
    @DisplayName("TC-LP-03: Ném AppException khi chưa có lesson progress record")
    void checkCompletionCondition_WhenProgressNotExists_ShouldThrowAppException() {
        // Arrange
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> lessonProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LESSON_PROGRESS_NOT_EXISTS);
    }

    // =========================================================
    // TC-LP-04: Hoàn thành khi đủ % xem và tất cả exercise đã làm
    // =========================================================
    /**
     * TC-LP-04
     * Objective: Trả về true khi xem đủ % VÀ tất cả exercise đã hoàn thành
     * Input: percentageWatched=90, gatingRules=80, 1 exercise đã làm
     * Expected: true, process=2
     * CheckDB: Xác minh save() được gọi với process=2
     */
    @Test
    @DisplayName("TC-LP-04: Hoàn thành khi đủ % và tất cả exercise đã hoàn thành")
    void checkCompletionCondition_WhenWatchedEnoughAndAllExerciseDone_ShouldReturnTrue() {
        // Arrange
        ExerciseEntity exercise = ExerciseEntity.builder().id(1).build();
        lesson.setExercises(List.of(exercise));
        lessonProgress.setPercentageWatched(90);
        // Mock getNextLessonOrTest để unLockNextLesson không bị NPE
        LessonOrTestAroundResponse nextItem = LessonOrTestAroundResponse.builder()
                .Id(11).type("LESSON").build();
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(exerciseRepository.isExerciseCompletedByStudent(1, 1)).thenReturn(true);
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(nextItem);
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isTrue();
        assertThat(lessonProgress.getProcess()).isEqualTo(2);
        verify(lessonProgressRepository).save(lessonProgress);
    }

    // =========================================================
    // TC-LP-05: Chưa hoàn thành khi còn exercise chưa làm
    // =========================================================
    /**
     * TC-LP-05
     * Objective: Trả về false khi đủ % xem nhưng có exercise chưa hoàn thành
     * Input: percentageWatched=90, 1 exercise chưa làm
     * Expected: false, process=1
     */
    @Test
    @DisplayName("TC-LP-05: Chưa hoàn thành khi còn exercise chưa làm (dù % xem đủ)")
    void checkCompletionCondition_WhenExerciseNotDone_ShouldReturnFalse() {
        // Arrange
        // % xem đủ (90>=80) nhưng exercise chưa làm → vẫn false
        ExerciseEntity exercise = ExerciseEntity.builder().id(1).build();
        lesson.setExercises(List.of(exercise));
        lessonProgress.setPercentageWatched(90);

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(exerciseRepository.isExerciseCompletedByStudent(1, 1)).thenReturn(false);

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert: exercise chưa xong → false, process=IN_PROGRESS(1)
        assertThat(result).isFalse();
        assertThat(lessonProgress.getProcess()).isEqualTo(1);
        // Quan trọng: process KHÔNG được set = 2 dù % xem đủ
        assertThat(lessonProgress.getProcess()).isNotEqualTo(2);
        verify(lessonProgressRepository).save(lessonProgress);
    }

    // =========================================================
    // TC-LP-06: Ném exception khi không tìm thấy lesson
    // =========================================================
    /**
     * TC-LP-06
     * Objective: Xác nhận ném AppException khi lessonId không tồn tại trong DB
     * Input: lessonId=999 (không tồn tại)
     * Expected: AppException với ErrorCode.LESSON_NOT_FOUND
     */
    @Test
    @DisplayName("TC-LP-06: Ném AppException khi không tìm thấy lesson")
    void checkCompletionCondition_WhenLessonNotFound_ShouldThrowAppException() {
        // Arrange
        request.setLessonId(999);
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> lessonProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LESSON_NOT_FOUND);
    }

    // =========================================================
    // TC-LP-07: percentageWatched mới > cũ → cập nhật giá trị
    // =========================================================
    /**
     * TC-LP-07
     * Objective: Xác nhận percentageWatched được cập nhật khi giá trị mới lớn hơn cũ
     * Input: currentPercentage=30, newPercentage=60 (chưa đủ gatingRules=80)
     * Expected: percentageWatched cập nhật thành 60, trả về false
     */

    // =========================================================
    // TC-LP-08: percentageWatched mới < cũ → KHÔNG cập nhật
    // =========================================================
    /**
     * TC-LP-08
     * Objective: Xác nhận percentageWatched KHÔNG bị giảm xuống khi giá trị mới nhỏ hơn cũ
     * Input: currentPercentage=70, newPercentage=40
     * Expected: percentageWatched vẫn giữ nguyên là 70
     */
    @Test
    @DisplayName("TC-LP-08: Không cập nhật percentageWatched khi giá trị mới nhỏ hơn cũ (70→40)")
    void checkCompletionCondition_WhenNewPercentageLower_ShouldNotUpdatePercentage() {
        // Arrange
        // currentWatched=70, newWatched=40: 70 < 40 = false → KHÔNG update
        // checkCompleted: 70 < gatingRules(80) → false
        lessonProgress.setPercentageWatched(70);
        request.setPercentageWatched(40);

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(lessonProgress.getPercentageWatched()).isEqualTo(70); // GIỮ nguyên 70, KHÔNG giảm xuống 40
        assertThat(result).isFalse();       // 70 < 80 → chưa hoàn thành
        assertThat(lessonProgress.getProcess()).isEqualTo(1); // IN_PROGRESS
    }

    // =========================================================
    // TC-LP-09: unLockNextTrack - mở track tiếp theo thành công
    // =========================================================
    /**
     * TC-LP-09
     * Objective: Xác nhận track hiện tại được set status=2 (DONE) và track kế tiếp set status=1 (ACTIVE)
     * Input: trackEntity có id=1, track kế tiếp có id=2
     * Expected: enrollmentRepository.save() được gọi 2 lần (1 cho current, 1 cho next)
     * CheckDB: Verify save() được gọi đúng số lần
     */
    @Test
    @DisplayName("TC-LP-09: unLockNextTrack set current=DONE(2) và next=ACTIVE(1)")
    void unLockNextTrack_WhenNextTrackExists_ShouldSetBothStatuses() {
        // Arrange
        // Lưu ý: impl dùng trackId+1 để tìm track tiếp theo (id=1 → findById(2))
        TrackEntity currentTrack = TrackEntity.builder().id(1).name("Track 0-300").build();
        TrackEntity nextTrack = TrackEntity.builder().id(2).name("Track 300-600").build();

        // status=1 (không phải 2) → điều kiện !equals(2) = true → save() sẽ được gọi
        EnrollmentEntity currentEnrollment = EnrollmentEntity.builder()
                .id(1).track(currentTrack).studentProfile(student).status(1).build();
        EnrollmentEntity nextEnrollment = EnrollmentEntity.builder()
                .id(2).track(nextTrack).studentProfile(student).status(0).build();

        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(currentEnrollment));
        when(trackRepository.findById(2)).thenReturn(Optional.of(nextTrack));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(2, 1))
                .thenReturn(List.of(nextEnrollment));

        // Act
        lessonProgressService.unLockNextTrack(currentTrack, student);

        // Assert từng giá trị rõ ràng
        assertThat(currentEnrollment.getStatus()).isEqualTo(2); // DONE
        assertThat(nextEnrollment.getStatus()).isEqualTo(1);    // ACTIVE
        // Verify save() được gọi đúng object:
        verify(enrollmentRepository).save(currentEnrollment);
        verify(enrollmentRepository).save(nextEnrollment);
    }

    // =========================================================
    // TC-LP-10: unLockNextTrack - không có track tiếp theo
    // =========================================================
    /**
     * TC-LP-10
     * Objective: Xác nhận không ném exception khi không có track tiếp theo (track cuối)
     * Input: trackId+1 không tồn tại trong DB
     * Expected: Chỉ set current track = DONE, không lỗi
     */

    // =========================================================
    // TC-LP-11: Boundary - đúng bằng gatingRules (80%) → hoàn thành
    // =========================================================
    /**
     * TC-LP-11
     * Objective: Kiểm tra điều kiện biên — đúng bằng gatingRules=80 thì PASS
     * Input: percentageWatched=80, gatingRules=80 (boundary value)
     * Expected: true (80 KHÔNG nhỏ hơn 80)
     * Notes: impl dùng < (stricty less than), vậy 80 >= 80 → pass
     */
    @Test
    @DisplayName("TC-LP-11: Boundary - đúng 80% = gatingRules → hoàn thành (trả về true)")
    void checkCompletionCondition_WhenWatchedExactlyAtGatingRules_ShouldReturnTrue() {
        // Arrange
        // percentageWatched=80 == gatingRules=80: 80 < 80 = false → pass checkCompleted
        lessonProgress.setPercentageWatched(80);
        request.setPercentageWatched(80);
        // exercises = [] (không có exercise)

        LessonOrTestAroundResponse nextItem = LessonOrTestAroundResponse.builder()
                .Id(11).type("LESSON").build();
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(nextItem);
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert: 80% == gatingRules → TRUE (impl dùng <, không phải <=)
        assertThat(result).isTrue();
        assertThat(lessonProgress.getProcess()).isEqualTo(2);
    }

    // =========================================================
    // TC-LP-12: Boundary - 79% (dưới gatingRules) → chưa hoàn thành
    // =========================================================
    /**
     * TC-LP-12
     * Objective: Kiểm tra điều kiện biên — 79% < gatingRules=80 → FAIL
     * Input: percentageWatched=79, gatingRules=80
     * Expected: false (79 < 80)
     */
    @Test
    @DisplayName("TC-LP-12: Boundary - 79% < gatingRules(80) → chưa hoàn thành")
    void checkCompletionCondition_WhenWatched79_ShouldReturnFalse() {
        // Arrange
        lessonProgress.setPercentageWatched(79);
        request.setPercentageWatched(79);

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert: 79 < 80 → false
        assertThat(result).isFalse();
        assertThat(lessonProgress.getProcess()).isEqualTo(1);
        // Phân biệt với TC-LP-11: 79 FAIL còn 80 PASS
        assertThat(lessonProgress.getProcess()).isNotEqualTo(2);
    }

    // =========================================================
    // TC-LP-13: unLockNextTrack - enrollment đã DONE không được save lại
    // =========================================================
    /**
     * TC-LP-13
     * Objective: Xác nhận enrollment có status=2 (DONE) không bị ghi đè và KHÔNG save lại
     * Input: currentEnrollment.status = 2 (đã DONE)
     * Expected: save() KHÔNG được gọi (vì !status.equals(2) = false)
     * Notes: Đây là bảo vệ tránh ghi đè trạng thái DONE của enrollment
     */
    @Test
    @DisplayName("TC-LP-13: unLockNextTrack không save enrollment đã ở trạng thái DONE(2)")
    void unLockNextTrack_WhenEnrollmentAlreadyDone_ShouldNotSave() {
        // Arrange
        TrackEntity currentTrack = TrackEntity.builder().id(1).build();
        // enrollment đã có status=2 (DONE) từ trước
        EnrollmentEntity alreadyDoneEnrollment = EnrollmentEntity.builder()
                .id(1).track(currentTrack).studentProfile(student).status(2).build();

        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(alreadyDoneEnrollment));
        // track tiếp theo không tồn tại
        when(trackRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        lessonProgressService.unLockNextTrack(currentTrack, student);

        // Assert: status KHÔNG thay đổi (vẫn là 2), save() KHÔNG được gọi
        assertThat(alreadyDoneEnrollment.getStatus()).isEqualTo(2);
        verify(enrollmentRepository, never()).save(any(EnrollmentEntity.class));
    }

    // =========================================================
    // TC-LP-14: Ném exception khi không tìm thấy student profile
    // =========================================================
    /**
     * TC-LP-14
     * Objective: Xác nhận ném AppException khi studentProfileId không tồn tại
     * Input: lessonId=10 (có tồn tại), studentProfileId=999 (không tồn tại)
     * Expected: AppException với ErrorCode.STUDENT_PROFILE_NOT_FOUND
     */
    @Test
    @DisplayName("TC-LP-14: Ném AppException khi không tìm thấy student profile")
    void checkCompletionCondition_WhenStudentNotFound_ShouldThrowAppException() {
        // Arrange
        request.setStudentProfileId(999);
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> lessonProgressService.checkCompletionCondition(request),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDENT_PROFILE_NOT_FOUND);
    }

    // =========================================================
    // TC-LP-15: unLockNextLesson khi next là TEST (nhánh TEST)
    // =========================================================
    /**
     * TC-LP-15
     * Objective: Khi lesson pass và item tiếp theo là TEST, tạo TestProgressEntity mới
     * Input: getNextLessonOrTest trả về type="TEST", test chưa có progress
     * Expected: testProgressRepository.save() được gọi với process=0 (UNLOCK)
     */
    @Test
    @DisplayName("TC-LP-15: unLockNextLesson tạo TestProgress mới khi next item là TEST")
    void checkCompletionCondition_WhenNextIsTest_ShouldCreateTestProgress() {
        // Arrange
        lessonProgress.setPercentageWatched(90);
        TestEntity nextTest = TestEntity.builder().id(55).name("Mid-term Test").build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        // next là TEST
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(55).type("TEST").build());
        when(testRepository.findById(55)).thenReturn(Optional.of(nextTest));
        // test chưa có progress
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(55, 1))
                .thenReturn(Collections.emptyList());

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert: hoàn thành và tạo TestProgress mới cho test tiếp theo
        assertThat(result).isTrue();
        verify(testProgressRepository).save(argThat(tp ->
                tp.getTest().equals(nextTest)
                && tp.getStudentProfile().equals(student)
                && tp.getProcess() == 0
        ));
    }

    // =========================================================
    // TC-LP-18: fallback unLockNextCourse khi next LESSON không tìm thấy trong DB
    // =========================================================
    /**
     * TC-LP-18
     * Objective: Khi getNextLessonOrTest trả về LESSON id nhưng findById() = empty,
     *            hệ thống phải fallback sang unLockNextCourse (đánh dấu course DONE)
     * Input: next item là LESSON id=999, nhưng findById(999) = Optional.empty()
     * Expected: enrollmentCourseRepository.save() được gọi với status="DONE"
     */
    @Test
    @DisplayName("TC-LP-18: fallback unLockNextCourse khi next LESSON không tồn tại trong DB")
    void checkCompletionCondition_WhenNextLessonNotFound_ShouldFallbackUnlockCourse() {
        lessonProgress.setPercentageWatched(90);
        CourseEntity course = CourseEntity.builder().id(5).build();
        ModuleEntity module = ModuleEntity.builder().id(3).course(course).build();
        lesson.setModule(module);

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(999).type("LESSON").build());
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());
        when(enrollmentCourseRepository.findByCourse_IdAndEnrollment_StudentProfile_Id(5, 1))
                .thenReturn(List.of(EnrollmentCourseEntity.builder()
                        .id(1)
                        .course(course)
                        .status("UNLOCK")
                        .enrollment(EnrollmentEntity.builder().id(1).build())
                        .build()));
        when(enrollmentCourseRepository.findTopByIdAfterAndEnrollment_Id(anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        course.setTrack(TrackEntity.builder().id(1).build());
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(trackRepository.findById(anyInt())).thenReturn(Optional.empty());

        Boolean result = lessonProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        verify(enrollmentCourseRepository).save(argThat(ec -> "DONE".equals(ec.getStatus())));
    }


    // =========================================================
    // TC-LP-16: unLockNextLesson khi không có next → gọi unLockNextCourse
    // =========================================================
    /**
     * TC-LP-16
     * Objective: Khi lesson là item cuối, getNextLessonOrTest ném AppException
     *            → unLockNextLesson gọi unLockNextCourse để đánh dấu course DONE
     * Input: getNextLessonOrTest ném AppException(LESSON_NOT_HAS_NEXT)
     * Expected: enrollmentCourseRepository.save() gọi với status="DONE"
     */
    @Test
    @DisplayName("TC-LP-16: unLockNextLesson gọi unLockNextCourse khi lesson là item cuối")
    void checkCompletionCondition_WhenNoNextItem_ShouldCallUnLockNextCourse() {
        // Arrange
        lessonProgress.setPercentageWatched(90);
        CourseEntity course = CourseEntity.builder().id(5).title("TOEIC Listening").build();
        ModuleEntity module = ModuleEntity.builder().id(3).course(course).build();
        lesson.setModule(module);

        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).build();
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder()
                .id(1).course(course).status("UNLOCK").enrollment(enrollment).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        // getNextLessonOrTest ném exception → catch → unLockNextCourse
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenThrow(new AppException(ErrorCode.LESSON_NOT_HAS_NEXT));
        // unLockNextCourse → setStatusCourse(course, student, "DONE")
        when(enrollmentCourseRepository.findByCourse_IdAndEnrollment_StudentProfile_Id(5, 1))
                .thenReturn(List.of(ec));
        // Không có course tiếp theo → ném AppException → unLockNextTrack
        when(enrollmentCourseRepository.findTopByIdAfterAndEnrollment_Id(anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        TrackEntity track = TrackEntity.builder().id(1).build();
        course.setTrack(track);
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(trackRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Boolean result = lessonProgressService.checkCompletionCondition(request);

        // Assert
        assertThat(result).isTrue();
        verify(enrollmentCourseRepository).save(argThat(e -> e.getStatus().equals("DONE")));
    }

    // =========================================================
    // TC-LP-17: unLockNextCourse mở khóa course tiếp theo
    // =========================================================
    /**
     * TC-LP-17
     * Objective: Xác nhận unLockNextCourse set current course=DONE và unlock course kế
     * Input: course hiện tại, có course tiếp theo trong enrollment
     * Expected: enrollmentCourseRepository.save() gọi 2 lần (DONE + UNLOCK)
     */
    @Test
    @DisplayName("TC-LP-17: unLockNextCourse set current=DONE và mở khóa course kế tiếp")
    void unLockNextCourse_WhenNextCourseExists_ShouldSetDoneAndUnlock() {
        // Arrange
        CourseEntity currentCourse = CourseEntity.builder().id(5).title("Course A").build();
        CourseEntity nextCourse = CourseEntity.builder().id(6).title("Course B").build();

        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).build();
        EnrollmentCourseEntity currentEC = EnrollmentCourseEntity.builder()
                .id(10).course(currentCourse).status("UNLOCK").enrollment(enrollment).build();
        EnrollmentCourseEntity nextEC = EnrollmentCourseEntity.builder()
                .id(11).course(nextCourse).status("LOCK").enrollment(enrollment).build();

        // setStatusCourse(currentCourse, student, "DONE")
        when(enrollmentCourseRepository.findByCourse_IdAndEnrollment_StudentProfile_Id(5, 1))
                .thenReturn(List.of(currentEC));
        // findTopByIdAfterAndEnrollment_Id → nextEC
        when(enrollmentCourseRepository.findTopByIdAfterAndEnrollment_Id(10, 1))
                .thenReturn(Optional.of(nextEC));
        // setStatusCourse(nextCourse, student, "UNLOCK")
        when(enrollmentCourseRepository.findByCourse_IdAndEnrollment_StudentProfile_Id(6, 1))
                .thenReturn(List.of(nextEC));

        // Act
        lessonProgressService.unLockNextCourse(currentCourse, student);

        // Assert
        assertThat(currentEC.getStatus()).isEqualTo("DONE");
        assertThat(nextEC.getStatus()).isEqualTo("UNLOCK");
        verify(enrollmentCourseRepository, times(2)).save(any(EnrollmentCourseEntity.class));
    }

    // =========================================================
    // TC-LP-20: exercises=null không gây NullPointerException 
    // =========================================================
    /**
     * TC-LP-20 
     * Objective: Khi lesson.exercises=null, hệ thống KHÔNG được ném NullPointerException
     *            mà phải xử lý như trường hợp không có exercise
     * Input: lesson.exercises = null, percentageWatched=90 >= gatingRules=80
     * Expected: trả về true, process=2 (DONE)
     */
    @Test
    @DisplayName("TC-LP-20: lesson có exercises=null phải được xử lý như không có exercise, không crash")
    void checkCompletionCondition_WhenExercisesIsNull_ShouldTreatAsNoExercise() {
        lesson.setExercises(null);
        lessonProgress.setPercentageWatched(90);
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(11).type("LESSON").build());
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        Boolean result = lessonProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(lessonProgress.getProcess()).isEqualTo(2);
    }

    // =========================================================
    // TC-LP-21: gatingRules=null dùng mặc định 0 
    // =========================================================
    /**
     * TC-LP-21 
     * Objective: Khi lesson.gatingRules=null, hệ thống KHÔNG ném NullPointerException
     *            mà phải dùng giá trị mặc định 0 khi so sánh
     * Input: lesson.gatingRules = null, percentageWatched=0
     * Expected: trả về true (0 >= 0), process=2 (DONE)
     */
    @Test
    @DisplayName("TC-LP-21: gatingRules=null phải dùng mặc định 0 thay vì crash")
    void checkCompletionCondition_WhenGatingRulesIsNull_ShouldUseZeroAsDefault() {
        lesson.setGatingRules(null);
        lessonProgress.setPercentageWatched(0);
        request.setPercentageWatched(0);
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(11).type("LESSON").build());
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        Boolean result = lessonProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(lessonProgress.getProcess()).isEqualTo(2);
    }

    // =========================================================
    // TC-LP-22: percentageWatched > 100 bị clamp về 100 
    // =========================================================
    /**
     * TC-LP-22 
     * Objective: Khi request gửi percentageWatched=150 (>100),
     *            giá trị lưu vào không được vượt quá 100
     * Input: currentPercentage=20, newPercentage=150
     * Expected: percentageWatched lưu = 100 (clamp), process=2 (DONE vì 100>=80)
     */
    @Test
    @DisplayName("TC-LP-22: percentageWatched > 100 không được lưu vượt quá 100")
    void checkCompletionCondition_WhenPercentageGreaterThan100_ShouldClampTo100() {
        lessonProgress.setPercentageWatched(20);
        request.setPercentageWatched(150);
        LessonEntity nextLesson = LessonEntity.builder().id(11).build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(lessonProgress));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenReturn(LessonOrTestAroundResponse.builder().Id(11).type("LESSON").build());
        when(lessonRepository.findById(11)).thenReturn(Optional.of(nextLesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(11, 1))
                .thenReturn(Collections.emptyList());

        Boolean result = lessonProgressService.checkCompletionCondition(request);

        assertThat(result).isTrue();
        assertThat(lessonProgress.getPercentageWatched()).isEqualTo(100);
    }

    // =========================================================
    // TC-LP-23: enrollmentCourse.status=null vẫn được set DONE 
    // =========================================================
    /**
     * TC-LP-23
     * Objective: Khi enrollmentCourse.status=null, unLockNextCourse KHÔNG crash
     *            mà vẫn set status="DONE" và gọi save()
     * Input: enrollmentCourse.status = null
     * Expected: status được set = "DONE", save() được gọi
     */
    @Test
    @DisplayName("TC-LP-23: unLockNextCourse phải xử lý enrollment course status null")
    void unLockNextCourse_WhenEnrollmentCourseStatusIsNull_ShouldSetDone() {
        CourseEntity course = CourseEntity.builder().id(5).track(TrackEntity.builder().id(1).build()).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).build();
        EnrollmentCourseEntity enrollmentCourse = EnrollmentCourseEntity.builder()
                .id(10)
                .course(course)
                .status(null)
                .enrollment(enrollment)
                .build();

        when(enrollmentCourseRepository.findByCourse_IdAndEnrollment_StudentProfile_Id(5, 1))
                .thenReturn(List.of(enrollmentCourse));
        when(enrollmentCourseRepository.findTopByIdAfterAndEnrollment_Id(10, 1))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(Collections.emptyList());
        when(trackRepository.findById(2)).thenReturn(Optional.empty());

        lessonProgressService.unLockNextCourse(course, student);

        assertThat(enrollmentCourse.getStatus()).isEqualTo("DONE");
        verify(enrollmentCourseRepository).save(enrollmentCourse);
    }

    // =========================================================
    // TC-LP-24: enrollment.status=null vẫn được set DONE
    // =========================================================
    /**
     * TC-LP-24
     * Objective: Khi enrollment.status=null (Integer), unLockNextTrack KHÔNG crash
     *            mà vẫn set status=2 (DONE) và gọi save()
     * Input: enrollment.status = null
     * Expected: status được set = 2, save() được gọi
     */
    @Test
    @DisplayName("TC-LP-24: unLockNextTrack phải xử lý enrollment status null")
    void unLockNextTrack_WhenEnrollmentStatusIsNull_ShouldSetCurrentDone() {
        TrackEntity currentTrack = TrackEntity.builder().id(1).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .id(1)
                .track(currentTrack)
                .studentProfile(student)
                .status(null)
                .build();

        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(enrollment));
        when(trackRepository.findById(2)).thenReturn(Optional.empty());

        lessonProgressService.unLockNextTrack(currentTrack, student);

        assertThat(enrollment.getStatus()).isEqualTo(2);
        verify(enrollmentRepository).save(enrollment);
    }
}
