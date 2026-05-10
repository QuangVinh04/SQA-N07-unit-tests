package com.mxhieu.doantotnghiep.service.impl;

import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.utils.ModuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonServiceImpl Tests")
class LessonServiceImplTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private EnrollmentCourseRepository enrollmentcourseRepository;
    @Mock private TestRepository testRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private LessonEntity lesson;
    private StudentProfileEntity student;
    private ModuleEntity module;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder().id(1).build();

        TrackEntity track = TrackEntity.builder().id(1).name("Track 0-300").build();
        course = CourseEntity.builder().id(5).title("TOEIC Listening").track(track).build();
        module = ModuleEntity.builder().id(20).title("Listening Module")
                .orderIndex(1L).type(ModuleType.LESSON).course(course).build();
        lesson = LessonEntity.builder().id(10).title("Lesson 1")
                .orderIndex(1).module(module).exercises(new ArrayList<>()).build();

        module.setLessons(List.of(lesson));
        module.setTests(new ArrayList<>());
        course.setModules(List.of(module));
    }

    // =========================================================
    // TC-LS-01: isCompletedLesson - Trả về true khi process = 2
    // =========================================================
    /**
     * TC-LS-01
     * Objective: Kiểm tra isCompletedLesson trả về true nếu process = 2
     * Input: lessonProgressRepository trả về process = 2
     * Expected: true
     */
    @Test
    @DisplayName("TC-LS-01: isCompletedLesson trả về true khi process = 2")
    void isCompletedLesson_WhenProcessIs2_ShouldReturnTrue() {
        LessonProgressEntity progress = LessonProgressEntity.builder()
                .process(2).lesson(lesson).studentProfile(student).build();
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(progress));
        assertThat(lessonService.isCompletedLesson(10, 1)).isTrue();
    }

    // =========================================================
    // TC-LS-02: isCompletedLesson - Trả về false khi không có record
    // =========================================================
    /**
     * TC-LS-02
     * Objective: Kiểm tra isCompletedLesson trả về false nếu không có record
     * Input: lessonProgressRepository trả về empty list
     * Expected: false
     */
    @Test
    @DisplayName("TC-LS-02: isCompletedLesson trả về false khi không có record")
    void isCompletedLesson_WhenNoRecord_ShouldReturnFalse() {
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(Collections.emptyList());
        assertThat(lessonService.isCompletedLesson(10, 1)).isFalse();
    }

    // =========================================================
    // TC-LS-03: isLockLesson - Trả về true khi khóa học bị LOCK
    // =========================================================
    /**
     * TC-LS-03
     * Objective: Kiểm tra isLockLesson trả về true nếu status của khóa học là LOCK
     * Input: enrollmentcourseRepository trả về LOCK
     * Expected: true
     */
    @Test
    @DisplayName("TC-LS-03: isLockLesson trả về true khi khóa học bị LOCK")
    void isLockLesson_WhenCourseIsLocked_ShouldReturnTrue() {
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentcourseRepository.findStatus(1, 5)).thenReturn("LOCK");
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(Collections.emptyList());
        assertThat(lessonService.isLockLesson(10, 1)).isTrue();
    }

    // =========================================================
    // TC-LS-04: isLockLesson - Trả về true khi status null (chưa học)
    // =========================================================
    /**
     * TC-LS-04
     * Objective: Kiểm tra isLockLesson trả về true nếu status khóa học là null
     * Input: enrollmentcourseRepository trả về null
     * Expected: true
     */
    @Test
    @DisplayName("TC-LS-04: isLockLesson trả về true khi status khóa học null")
    void isLockLesson_WhenCourseStatusIsNull_ShouldReturnTrue() {
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentcourseRepository.findStatus(1, 5)).thenReturn(null);
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(Collections.emptyList());
        assertThat(lessonService.isLockLesson(10, 1)).isTrue();
    }

    // =========================================================
    // TC-LS-05: isLockLesson - Ném exception khi lesson không tồn tại
    // =========================================================
    /**
     * TC-LS-05
     * Objective: Ném AppException khi lesson không tồn tại trong DB
     * Input: lessonId=999
     * Expected: AppException với ErrorCode.LESSON_NOT_FOUND
     */
    @Test
    @DisplayName("TC-LS-05: isLockLesson ném exception khi lesson không tồn tại")
    void isLockLesson_WhenLessonNotFound_ShouldThrowException() {
        when(lessonRepository.findById(999)).thenReturn(Optional.empty());
        AppException ex = catchThrowableOfType(
                () -> lessonService.isLockLesson(999, 1),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LESSON_NOT_FOUND);
    }

    // =========================================================
    // TC-LS-06: completedStar - Không bài tập & hoàn thành -> 3 sao
    // =========================================================
    /**
     * TC-LS-06
     * Objective: Nếu lesson không có exercises và đã completed, trả về 3 sao
     * Input: exercises=[], process=2
     * Expected: 3 sao
     */
    @Test
    @DisplayName("TC-LS-06: completedStar trả về 3 sao khi không có exercise và đã học xong")
    void completedStar_WhenNoExerciseAndCompleted_ShouldReturn3() {
        lesson.setExercises(Collections.emptyList());
        LessonProgressEntity progress = LessonProgressEntity.builder()
                .process(2).lesson(lesson).studentProfile(student).build();
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(progress));
        assertThat(lessonService.completedStar(10, 1)).isEqualTo(3);
    }

    // =========================================================
    // TC-LS-07: completedStar - Tổng điểm 0 -> 0 sao
    // =========================================================
    /**
     * TC-LS-07
     * Objective: Nếu điểm exercises = 0, số sao đạt được là 0
     * Input: totalScroreOfLesson=0, count=1
     * Expected: 0 sao
     */
    @Test
    @DisplayName("TC-LS-07: completedStar trả về 0 sao khi tổng điểm là 0")
    void completedStar_WhenTotalScoreIsZero_ShouldReturn0() {
        ExerciseEntity exercise = ExerciseEntity.builder().id(1).build();
        lesson.setExercises(List.of(exercise));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(lessonRepository.totalScroreOfLesson(10, 1)).thenReturn(0);
        when(exerciseRepository.countByLessonId(10)).thenReturn(1);
        assertThat(lessonService.completedStar(10, 1)).isEqualTo(0);
    }

    // =========================================================
    // TC-LS-08: completedStar - Điểm > 80% -> 3 sao
    // =========================================================
    /**
     * TC-LS-08
     * Objective: Nếu điểm exercises > 80% (85/100), trả về 3 sao
     * Input: totalScore=85, count=1
     * Expected: 3 sao
     */
    @Test
    @DisplayName("TC-LS-08: completedStar trả về 3 sao khi điểm trung bình > 80%")
    void completedStar_WhenScoreAbove80Percent_ShouldReturn3() {
        ExerciseEntity exercise = ExerciseEntity.builder().id(1).build();
        lesson.setExercises(List.of(exercise));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(lessonRepository.totalScroreOfLesson(10, 1)).thenReturn(85);
        when(exerciseRepository.countByLessonId(10)).thenReturn(1);
        assertThat(lessonService.completedStar(10, 1)).isEqualTo(3);
    }

    // =========================================================
    // TC-LS-09: completedStar - exercises null & hoàn thành -> 3 sao (RED test)
    // =========================================================
    /**
     * TC-LS-09 (RED)
     * Objective: Nếu exercises=null thì xử lý như không có bài tập và không bị crash
     * Input: exercises=null, process=2
     * Expected: 3 sao
     */
    @Test
    @DisplayName("TC-LS-09 RED: completedStar không crash khi exercises=null")
    void completedStar_WhenExercisesIsNullAndCompleted_ShouldReturn3() {
        lesson.setExercises(null);
        LessonProgressEntity progress = LessonProgressEntity.builder()
                .process(2).lesson(lesson).studentProfile(student).build();
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(10, 1))
                .thenReturn(List.of(progress));
        assertThat(lessonService.completedStar(10, 1)).isEqualTo(3);
    }

    // =========================================================
    // TC-LS-10: getNextLessonOrTest - Tồn tại bài học kế tiếp
    // =========================================================
    /**
     * TC-LS-10
     * Objective: Trả về chính xác item kế tiếp nếu item đó tồn tại
     * Input: list modules có 2 lessons, request lesson hiện tại là lesson 1
     * Expected: Trả về id lesson 2, type="LESSON"
     */
    @Test
    @DisplayName("TC-LS-10: getNextLessonOrTest trả về item kế tiếp nếu tồn tại")
    void getNextLessonOrTest_WhenNextLessonExists_ShouldReturnNextLesson() {
        LessonEntity lesson2 = LessonEntity.builder().id(11).title("Lesson 2")
                .orderIndex(2).module(module).exercises(new ArrayList<>()).build();
        module.setLessons(List.of(lesson, lesson2));

        LessonOrTestAroundRequest req = new LessonOrTestAroundRequest(10, "LESSON");
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));

        LessonOrTestAroundResponse response = lessonService.getNextLessonOrTest(req);
        assertThat(response.getId()).isEqualTo(11);
        assertThat(response.getType()).isEqualTo("LESSON");
    }

    // =========================================================
    // TC-LS-11: getPreviousLessonID - Test nằm sau Lesson
    // =========================================================
    /**
     * TC-LS-11
     * Objective: Trả về item trước đó chính xác kể cả khi nhảy sang module khác
     * Input: module 2 là TEST, module 1 là LESSON, request TEST
     * Expected: Trả về lesson của module 1
     */
    @Test
    @DisplayName("TC-LS-11: getPreviousLessonID trả về đúng item trước đó giữa các modules")
    void getPreviousLessonID_WhenTypeIsTest_ShouldReturnPreviousItem() {
        ModuleEntity lessonModule = ModuleEntity.builder().id(20).title("Lesson Module")
                .orderIndex(1L).type(ModuleType.LESSON).course(course).build();
        LessonEntity prevLesson = LessonEntity.builder().id(10).orderIndex(1)
                .module(lessonModule).exercises(new ArrayList<>()).build();
        lessonModule.setLessons(List.of(prevLesson));
        lessonModule.setTests(new ArrayList<>());

        ModuleEntity testModule = ModuleEntity.builder().id(21).title("Test Module")
                .orderIndex(2L).type(ModuleType.TEST).course(course).build();
        TestEntity test = TestEntity.builder().id(50).name("Test 1")
                .module(testModule).build();
        testModule.setTests(List.of(test));
        testModule.setLessons(new ArrayList<>());

        course.setModules(List.of(lessonModule, testModule));

        LessonOrTestAroundRequest req = new LessonOrTestAroundRequest(50, "TEST");
        when(testRepository.findById(50)).thenReturn(Optional.of(test));

        LessonOrTestAroundResponse response = lessonService.getPreviousLessonID(req);
        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.getType()).isEqualTo("LESSON");
    }

    // =========================================================
    // TC-LS-12: getNextLessonOrTest - Type không hợp lệ (RED test)
    // =========================================================
    /**
     * TC-LS-12 (RED)
     * Objective: Ném AppException khi type gửi lên không thuộc 'LESSON' hoặc 'TEST'
     * Input: type="QUIZ"
     * Expected: AppException
     */
    @Test
    @DisplayName("TC-LS-12 RED: getNextLessonOrTest ném exception khi type không hợp lệ")
    void getNextLessonOrTest_WhenTypeInvalid_ShouldThrowAppException() {
        LessonOrTestAroundRequest req = new LessonOrTestAroundRequest(10, "QUIZ");
        AppException ex = catchThrowableOfType(
                () -> lessonService.getNextLessonOrTest(req),
                AppException.class
        );
        assertThat(ex).isNotNull();
    }

    // =========================================================
    // TC-LS-13: getPreviousLessonID - Type null (RED test)
    // =========================================================
    /**
     * TC-LS-13 (RED)
     * Objective: Ném AppException khi type gửi lên null
     * Input: type=null
     * Expected: AppException
     */
    @Test
    @DisplayName("TC-LS-13 RED: getPreviousLessonID ném exception khi type null")
    void getPreviousLessonID_WhenTypeIsNull_ShouldThrowAppException() {
        LessonOrTestAroundRequest req = new LessonOrTestAroundRequest(10, null);
        AppException ex = catchThrowableOfType(
                () -> lessonService.getPreviousLessonID(req),
                AppException.class
        );
        assertThat(ex).isNotNull();
    }

    // =========================================================
    // TC-LS-14: getNextLessonOrTest - Modules null (RED test)
    // =========================================================
    /**
     * TC-LS-14 (RED)
     * Objective: Trả về AppException hoặc xử lý gracefully thay vì ném NPE khi course.modules = null
     * Input: course.modules = null
     * Expected: AppException được ném ra thay vì NullPointerException (cần bắt và throw chuẩn)
     */
    @Test
    @DisplayName("TC-LS-14 RED: getNextLessonOrTest ném exception thay vì NPE khi course modules null")
    void getNextLessonOrTest_WhenCourseModulesIsNull_ShouldThrowAppException() {
        course.setModules(null);
        LessonOrTestAroundRequest req = new LessonOrTestAroundRequest(10, "LESSON");
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        AppException ex = catchThrowableOfType(
                () -> lessonService.getNextLessonOrTest(req),
                AppException.class
        );
        assertThat(ex).isNotNull();
    }
}

