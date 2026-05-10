package com.mxhieu.doantotnghiep.service.impl;

import com.mxhieu.doantotnghiep.converter.StudyPlanConverter;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.LessonService;
import com.mxhieu.doantotnghiep.service.TestService;
import com.mxhieu.doantotnghiep.service.TrackService;
import com.mxhieu.doantotnghiep.utils.ModuleType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho StudyPlanServiceImpl.
 *
 * Chức năng kiểm thử:
 *   - checkExistStudyPlan(): kiểm tra study plan có tồn tại và đang active không
 *
 * Framework: JUnit 5 + Mockito
 * Rollback: Toàn bộ repository được mock — không thay đổi DB thật.
 *
 * Lưu ý: createStudyPlan() và verifyInformation() phụ thuộc nhiều vào
 * các phép tính thuần túy (logic phân bổ ngày học). Các test tập trung vào
 * hành vi observable qua public method và mock.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudyPlanServiceImpl Tests")
class StudyPlanServiceImplTest {

    // ==================== Mocks ====================
    @Mock private TrackRepository trackRepository;
    @Mock private EnrollmentCourseRepository enrollmentCourseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private TestRepository testRepository;
    @Mock private StudyPlanRepository studyPlanRepository;
    @Mock private StudyPlanConverter studyPlanConverter;
    @Mock private LessonService lessonService;
    @Mock private TestService testService;
    @Mock private TrackService trackService;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private TestProgressRepository testProgressRepository;

    @InjectMocks
    private StudyPlanServiceImpl studyPlanService;

    // ==================== Dữ liệu dùng chung ====================
    private StudentProfileEntity student;
    private TrackEntity track;
    private StudyPlanEntity activeStudyPlan;
    private StudyPlanEntity inactiveStudyPlan;

    /**
     * Thiết lập dữ liệu mẫu dùng chung.
     */
    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder().id(1).fullName("Le Van C").build();
        track = TrackEntity.builder().id(1).name("Track 0-300").code("0-300").build();

        activeStudyPlan = StudyPlanEntity.builder()
                .id(10)
                .status(0)       // 0 = active
                .studentProfile(student)
                .track(track)
                .soLuongNgayHoc(30)
                .ngayHocTrongTuan(List.of(2, 4, 6)) // Thứ 2, 4, 6
                .startDate(LocalDate.now())
                .studyPlanItems(new ArrayList<>())
                .build();

        inactiveStudyPlan = StudyPlanEntity.builder()
                .id(9)
                .status(1)       // 1 = inactive (đã hủy)
                .studentProfile(student)
                .track(track)
                .build();
    }

    // =========================================================
    // TC-SP-01: checkExistStudyPlan - tìm thấy study plan đang active
    // =========================================================
    /**
     * TC-SP-01
     * Objective: Trả về đúng studyPlanId khi có plan với status = 0 (active)
     * Input: studentId=1, có 1 plan active (id=10)
     * Expected: Trả về 10
     */
    @Test
    @DisplayName("TC-SP-01: checkExistStudyPlan trả về id khi có plan active")
    void checkExistStudyPlan_WhenActivePlanExists_ShouldReturnId() {
        // Arrange
        when(studyPlanRepository.findByStudentProfile_Id(1))
                .thenReturn(List.of(activeStudyPlan));

        // Act
        Integer result = studyPlanService.checkExistStudyPlan(1);

        // Assert
        assertThat(result).isEqualTo(10);
    }

    // =========================================================
    // TC-SP-02: checkExistStudyPlan - không có plan nào
    // =========================================================
    /**
     * TC-SP-02
     * Objective: Ném AppException khi học viên chưa có study plan nào
     * Input: studentId=1, không có plan nào
     * Expected: AppException với ErrorCode.STUDYPLAN_NOT_FOUND
     */
    @Test
    @DisplayName("TC-SP-02: checkExistStudyPlan ném exception khi không có plan nào")
    void checkExistStudyPlan_WhenNoPlanExists_ShouldThrowStudyPlanNotFound() {
        // Arrange
        when(studyPlanRepository.findByStudentProfile_Id(1))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> studyPlanService.checkExistStudyPlan(1),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDYPLAN_NOT_FOUND);
    }

    // =========================================================
    // TC-SP-03: checkExistStudyPlan - chỉ có plan inactive
    // =========================================================
    /**
     * TC-SP-03
     * Objective: Ném AppException khi tất cả plan đều có status != 0 (đã hủy/hết hạn)
     * Input: studentId=1, chỉ có plan inactive (status=1)
     * Expected: AppException với ErrorCode.STUDYPLAN_NOT_ACTIVE
     */
    @Test
    @DisplayName("TC-SP-03: checkExistStudyPlan ném exception khi chỉ có plan inactive")
    void checkExistStudyPlan_WhenOnlyInactivePlanExists_ShouldThrowStudyPlanNotActive() {
        // Arrange
        when(studyPlanRepository.findByStudentProfile_Id(1))
                .thenReturn(List.of(inactiveStudyPlan));

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> studyPlanService.checkExistStudyPlan(1),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDYPLAN_NOT_ACTIVE);
    }

    // =========================================================
    // TC-SP-05: createStudyPlan - đánh dấu plan cũ là inactive trước khi tạo mới
    // =========================================================
    /**
     * TC-SP-05
     * Objective: Khi tạo kế hoạch học mới, các plan cũ phải được set status=1 (inactive)
     * Input: Có 1 plan cũ (status=0), request tạo plan mới
     * Expected: saveAll() được gọi với plan cũ có status=1
     *           save() được gọi cho plan mới
     * CheckDB: Xác minh saveAll() và save() được gọi đúng
     * Rollback: Mock repository — DB không bị thay đổi thật
     */
    @Test
    @DisplayName("TC-SP-05: createStudyPlan đánh dấu plan cũ thành inactive trước khi tạo mới")
    void createStudyPlan_ShouldDeactivateOldPlansBeforeCreatingNew() {
        // Arrange
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2025, 6, 2)) // Thứ Hai
                        .soLuongNgayHoc(10)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 2, 3, 4, 5))) // T2-T6
                        .build();

        StudyPlanEntity oldPlan = StudyPlanEntity.builder().id(5).status(0).build();

        // Build khóa học với 1 module lesson, 1 lesson
        ModuleEntity moduleLesson = ModuleEntity.builder().id(1).type(ModuleType.LESSON)
                .orderIndex(1L).build();
        LessonEntity lesson1 = LessonEntity.builder().id(100).title("L1")
                .orderIndex(1).mediaassets(new ArrayList<>()).build();
        moduleLesson.setLessons(List.of(lesson1));
        moduleLesson.setTests(new ArrayList<>());
        CourseEntity course = CourseEntity.builder().id(5).title("Course A")
                .modules(List.of(moduleLesson)).build();
        moduleLesson.setCourse(course);

        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(1).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).track(track)
                .studentProfile(student)
                .enrollmentCourses(List.of(ec)).build();
        ec.setEnrollment(enrollment);

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(oldPlan));
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonRepository.findById(100)).thenReturn(Optional.of(lesson1));

        // Act
        studyPlanService.createStudyPlan(request);

        // Assert: plan cũ được set inactive
        assertThat(oldPlan.getStatus()).isEqualTo(1);
        verify(studyPlanRepository).saveAll(List.of(oldPlan));
        // Plan mới được lưu
        verify(studyPlanRepository).save(any(StudyPlanEntity.class));
    }



    // =========================================================
    // TC-SP-07: soNgayHocToiThieu - track không tồn tại
    // =========================================================
    /**
     * TC-SP-07
     * Objective: Ném RuntimeException khi trackId không tồn tại
     * Input: trackId = 999 (không có trong DB)
     * Expected: RuntimeException với message "Track not found"
     */
    @Test
    @DisplayName("TC-SP-07: soNgayHocToiThieu ném exception khi track không tồn tại")
    void soNgayHocToiThieu_WhenTrackNotFound_ShouldThrowException() {
        // Arrange
        when(trackRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studyPlanService.soNgayHocToiThieu(999, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Track not found");
    }

    // =========================================================
    // TC-SP-08: getStudyPlanDetail - ném exception khi plan không tồn tại
    // =========================================================
    /**
     * TC-SP-08
     * Objective: Ném AppException khi studyPlanId không tồn tại
     * Input: studyPlanId = 999
     * Expected: AppException với ErrorCode.STUDYPLAN_NOT_FOUND
     */
    @Test
    @DisplayName("TC-SP-08: getStudyPlanDetail ném exception khi plan không tồn tại")
    void getStudyPlanDetail_WhenPlanNotFound_ShouldThrowException() {
        // Arrange
        when(studyPlanRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException ex = catchThrowableOfType(
                () -> studyPlanService.getStudyPlanDetail(999),
                AppException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDYPLAN_NOT_FOUND);
    }

    // =========================================================
    // TC-SP-09: soNgayHocToiThieu - khóa học không có module
    // =========================================================
    /**
     * TC-SP-09
     * Objective: Trả về 0 khi khóa học không có module nào (tổng thời gian = 0)
     * Input: 1 course với modules = [] (rỗng)
     * Expected: 0
     */
    @Test
    @DisplayName("TC-SP-09: soNgayHocToiThieu trả về 0 khi không có module")
    void soNgayHocToiThieu_WhenCourseHasNoModules_ShouldReturn0() {
        // Arrange
        CourseEntity emptyCourse = CourseEntity.builder().id(5)
                .modules(new ArrayList<>()).build();
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder()
                .id(1).course(emptyCourse).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1)
                .enrollmentCourses(List.of(ec)).build();

        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(enrollment));

        // Act
        int result = studyPlanService.soNgayHocToiThieu(1, 1);

        // Assert
        assertThat(result).isEqualTo(0);
    }

    // =========================================================
    // TC-SP-10: createStudyPlan - ném exception khi student không tồn tại
    // =========================================================
    /**
     * TC-SP-10
     * Objective: Ném AppException khi student profile không tồn tại trong DB
     * Input: Request tạo study plan với studentProfileId=999
     * Expected: AppException với ErrorCode.STUDENT_PROFILE_NOT_FOUND
     */
    @Test
    @DisplayName("TC-SP-10: createStudyPlan ném STUDENT_PROFILE_NOT_FOUND khi student không tồn tại")
    void createStudyPlan_WhenStudentNotFound_ShouldThrowException() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(999)
                        .startDate(LocalDate.of(2025, 6, 2))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();

        ModuleEntity moduleLesson = ModuleEntity.builder().id(1).type(ModuleType.LESSON).build();
        LessonEntity lesson1 = LessonEntity.builder().id(100).mediaassets(new ArrayList<>()).build();
        moduleLesson.setLessons(List.of(lesson1));
        moduleLesson.setTests(new ArrayList<>());
        CourseEntity course = CourseEntity.builder().id(5).modules(List.of(moduleLesson)).build();
        moduleLesson.setCourse(course);
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(1).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).enrollmentCourses(List.of(ec)).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 999)).thenReturn(Collections.emptyList());
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 999)).thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = catchThrowableOfType(
                () -> studyPlanService.createStudyPlan(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDENT_PROFILE_NOT_FOUND);
    }

    // =========================================================
    // TC-SP-11: createStudyPlan - ném exception khi lesson không tồn tại
    // =========================================================
    /**
     * TC-SP-11
     * Objective: Ném AppException khi lesson id trong khóa học không tồn tại trong DB
     * Input: Request tạo study plan, course có lessonId=100, nhưng findById(100) = empty
     * Expected: AppException với ErrorCode.LESSON_NOT_FOUND
     */
    @Test
    @DisplayName("TC-SP-11: createStudyPlan ném LESSON_NOT_FOUND khi item lesson không tồn tại")
    void createStudyPlan_WhenLessonNotFound_ShouldThrowException() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2025, 6, 2))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();

        ModuleEntity moduleLesson = ModuleEntity.builder().id(1).type(ModuleType.LESSON).build();
        LessonEntity lesson1 = LessonEntity.builder().id(100).mediaassets(new ArrayList<>()).build();
        moduleLesson.setLessons(List.of(lesson1));
        moduleLesson.setTests(new ArrayList<>());
        CourseEntity course = CourseEntity.builder().id(5).modules(List.of(moduleLesson)).build();
        moduleLesson.setCourse(course);
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(1).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(1).enrollmentCourses(List.of(ec)).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 1)).thenReturn(Collections.emptyList());
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1)).thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonRepository.findById(100)).thenReturn(Optional.empty());

        AppException ex = catchThrowableOfType(
                () -> studyPlanService.createStudyPlan(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LESSON_NOT_FOUND);
    }

    // =========================================================
    // TC-SP-12: getOverviewData - ném exception khi track không tồn tại
    // =========================================================
    /**
     * TC-SP-12
     * Objective: Ném RuntimeException khi track không tồn tại
     * Input: trackId=1 nhưng trackRepository.findById(1) = empty
     * Expected: RuntimeException với message "Track not found"
     */
    @Test
    @DisplayName("TC-SP-12: getOverviewData ném RuntimeException khi track không tồn tại")
    void getOverviewData_WhenTrackNotFound_ShouldThrowException() {
        when(trackService.trackDauTienChuaHoanThanhVaMoKhoa(1)).thenReturn(1);
        when(trackRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.getOverviewData(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Track not found");
    }

    // =========================================================
    // TC-SP-13: getOverviewData - track không có course
    // =========================================================
    /**
     * TC-SP-13
     * Objective: Trả về dữ liệu overview mặc định khi track chưa có khóa học nào
     * Input: enrollmentRepository trả về empty list
     * Expected: overview chứa "Chưa có khóa học", thoiGianHocTieuChuan chứa "Chưa có thời gian"
     */
    @Test
    @DisplayName("TC-SP-13: getOverviewData trả về overview mặc định khi track không có course")
    void getOverviewData_WhenNoCourses_ShouldReturnDefaultOverview() {
        track.setDescription("Track căn bản");
        when(trackService.trackDauTienChuaHoanThanhVaMoKhoa(1)).thenReturn(1);
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(Collections.emptyList());

        var response = studyPlanService.getOverviewData(1);

        assertThat(response.getTrackId()).isEqualTo(1);
        assertThat(response.getOverview()).contains("Chưa có khóa học");
        assertThat(response.getMucTieuDauRa()).isEqualTo("TOEIC LR 0-300");
        assertThat(response.getThoiGianHocTieuChuan()).contains("Chưa có thời gian học tiêu chuẩn");
    }

    // =========================================================
    // TC-SP-14: getStudyPlanDetail - gom nhóm item theo ngày
    // =========================================================
    /**
     * TC-SP-14
     * Objective: Xác nhận API getStudyPlanDetail gom đúng các item vào cùng 1 ngày
     * Input: 3 StudyPlanItemEntity (2 cái cùng ngày d1, 1 cái ngày d2)
     * Expected: Danh sách response có size=2 (2 ngày), ngày d1 chứa cả lesson và test
     */
    @Test
    @DisplayName("TC-SP-14: getStudyPlanDetail gom nhóm item theo ngày và map trạng thái lesson/test")
    void getStudyPlanDetail_WhenPlanExists_ShouldGroupItemsAndMapStatus() {
        LessonEntity lesson = LessonEntity.builder().id(10).title("Lesson A").build();
        TestEntity test = TestEntity.builder().id(20).name("Test A").type("MINI_TEST").build();
        LocalDate d1 = LocalDate.of(2026, 5, 1);
        LocalDate d2 = LocalDate.of(2026, 5, 2);

        StudyPlanItemEntity item1 = StudyPlanItemEntity.builder().date(d1).lesson(lesson).build();
        StudyPlanItemEntity item2 = StudyPlanItemEntity.builder().date(d1).test(test).build();
        StudyPlanItemEntity item3 = StudyPlanItemEntity.builder().date(d2).lesson(lesson).build();

        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(100)
                .track(track)
                .studentProfile(student)
                .startDate(d1)
                .soLuongNgayHoc(2)
                .ngayHocTrongTuan(List.of(5, 6))
                .studyPlanItems(List.of(item1, item2, item3))
                .build();

        when(studyPlanRepository.findById(100)).thenReturn(Optional.of(plan));
        when(studyPlanConverter.toResponseSummery(plan)).thenReturn(
                com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse.builder()
                        .trackId(1).studentProfileId(1).startDate(d1).build()
        );
        when(lessonService.isLockLesson(10, 1)).thenReturn(false);
        when(lessonService.isCompletedLesson(10, 1)).thenReturn(true);
        when(lessonService.completedStar(10, 1)).thenReturn(3);
        when(testService.isLock(20, 1)).thenReturn(false);
        when(testService.isCompletedTest(20, 1)).thenReturn(false);

        var response = studyPlanService.getStudyPlanDetail(100);

        assertThat(response.getStudyPlanItems()).hasSize(2);
        assertThat(response.getStudyPlanItems().get(0).getLessons()).isNotEmpty();
        assertThat(response.getStudyPlanItems().get(0).getTests()).isNotEmpty();
    }

    // =========================================================
    // TC-SP-15: getInformation - thống kê item đến ngày hiện tại
    // =========================================================
    /**
     * TC-SP-15
     * Objective: Tính toán số item đã hoàn thành và số item cần học đúng theo ngày
     * Input: Plan có 4 items (1 done quá khứ, 1 lesson hiện tại, 1 test hiện tại, 1 tương lai)
     * Expected: SoUnitDaHoanThanh=1, SoUnitTheoKeHoach=3, TongSoUnit=4, UnitsCanHoanThanh size=2
     */
    @Test
    @DisplayName("TC-SP-15: getInformation chỉ tính item đến ngày hiện tại và tách completed/uncompleted")
    void getInformation_WhenMixedProgress_ShouldCalculateCorrectly() {
        LessonEntity completedLesson = LessonEntity.builder().id(10).title("L done").build();
        LessonEntity unlockLesson = LessonEntity.builder().id(11).title("L unlock").build();
        TestEntity unlockTest = TestEntity.builder().id(20).name("T unlock").build();

        StudyPlanItemEntity doneItem = StudyPlanItemEntity.builder()
                .date(LocalDate.now().minusDays(1))
                .lesson(completedLesson)
                .build();
        StudyPlanItemEntity unlockLessonItem = StudyPlanItemEntity.builder()
                .date(LocalDate.now())
                .lesson(unlockLesson)
                .build();
        StudyPlanItemEntity unlockTestItem = StudyPlanItemEntity.builder()
                .date(LocalDate.now())
                .test(unlockTest)
                .build();
        StudyPlanItemEntity futureItem = StudyPlanItemEntity.builder()
                .date(LocalDate.now().plusDays(3))
                .lesson(unlockLesson)
                .build();

        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(200)
                .studentProfile(student)
                .startDate(LocalDate.now().minusDays(7))
                .soLuongNgayHoc(10)
                .ngayHocTrongTuan(List.of(1, 2, 3, 4, 5, 6, 7))
                .studyPlanItems(List.of(doneItem, unlockLessonItem, unlockTestItem, futureItem))
                .build();

        when(studyPlanRepository.findById(200)).thenReturn(Optional.of(plan));
        when(lessonService.isCompletedLesson(10, 1)).thenReturn(true);
        when(lessonService.completedStar(10, 1)).thenReturn(2);
        when(lessonService.isCompletedLesson(11, 1)).thenReturn(false);
        when(lessonService.isLockLesson(11, 1)).thenReturn(false);
        when(testService.isCompletedTest(20, 1)).thenReturn(false);
        when(testService.isLock(20, 1)).thenReturn(false);

        var response = studyPlanService.getInformation(200);

        assertThat(response.getSoUnitDaHoanThanh()).isEqualTo(1);
        assertThat(response.getSoUnitTheoKeHoach()).isEqualTo(3);
        assertThat(response.getTongSoUnit()).isEqualTo(4);
        assertThat(response.getUnitsCanHoanThanh()).hasSize(2);
    }


    // =========================================================
    // TC-SP-17: verifyInformation - số buổi học tính toán đúng
    // =========================================================
    /**
     * TC-SP-17
     * Objective: Kiểm tra thuật toán phân bổ tổng số buổi học khi số units > số ngày yêu cầu
     * Input: Request yêu cầu 1 ngày học, nhưng tổng khóa học có 2 units
     * Expected: TongSoUnits=2, TongSoBuoiHoc=1, SoUnitsTrenBuoi được tính toán hiển thị
     */
    @Test
    @DisplayName("TC-SP-17: verifyInformation nhánh soLuongLesson > soNgayHoc")
    void verifyInformation_WhenTotalUnitsGreaterThanDays_ShouldReturnExpectedSummary() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(1)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7)))
                        .build();

        LessonEntity l1 = LessonEntity.builder().id(201).mediaassets(new ArrayList<>()).build();
        LessonEntity l2 = LessonEntity.builder().id(202).mediaassets(new ArrayList<>()).build();
        ModuleEntity module = ModuleEntity.builder().id(2).type(ModuleType.LESSON).lessons(List.of(l1, l2)).build();
        CourseEntity course = CourseEntity.builder().id(6).modules(List.of(module)).build();
        module.setCourse(course);
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(2).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(2).enrollmentCourses(List.of(ec)).build();

        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1)).thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonRepository.findById(201)).thenReturn(Optional.of(l1));
        when(lessonRepository.findById(202)).thenReturn(Optional.of(l2));

        var response = studyPlanService.verifyInformation(request);

        assertThat(response.getTongSoUnits()).isEqualTo(2);
        assertThat(response.getTongSoBuoiHoc()).isEqualTo(1);
        assertThat(response.getSoUnitsTrenBuoi()).isNotBlank();
    }

    // =========================================================
    // TC-SP-18: createStudyPlan - ném exception khi test không tồn tại
    // =========================================================
    /**
     * TC-SP-18
     * Objective: Ném AppException khi test id trong module không tồn tại trong DB
     * Input: course có testId=301, nhưng testRepository.findById(301) = empty
     * Expected: AppException với ErrorCode.TEST_NOT_FOUND
     */
    @Test
    @DisplayName("TC-SP-18: createStudyPlan ném TEST_NOT_FOUND khi item test không tồn tại")
    void createStudyPlan_WhenTestNotFound_ShouldThrowException() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(3)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();

        TestEntity test = TestEntity.builder().id(301).build();
        ModuleEntity testModule = ModuleEntity.builder().id(3).type(ModuleType.TEST).tests(List.of(test)).build();
        CourseEntity course = CourseEntity.builder().id(7).modules(List.of(testModule)).build();
        testModule.setCourse(course);
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(3).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(3).enrollmentCourses(List.of(ec)).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 1)).thenReturn(Collections.emptyList());
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1)).thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testRepository.findById(301)).thenReturn(Optional.empty());

        AppException ex = catchThrowableOfType(
                () -> studyPlanService.createStudyPlan(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TEST_NOT_FOUND);
    }

    // =========================================================
    // TC-SP-19: getStudyPlanDetail - plan không có item
    // =========================================================
    /**
     * TC-SP-19
     * Objective: Trả về danh sách item rỗng khi plan không có studyPlanItems
     * Input: plan có studyPlanItems = rỗng
     * Expected: response.getStudyPlanItems() isEmpty
     */
    @Test
    @DisplayName("TC-SP-19: getStudyPlanDetail trả về danh sách rỗng khi plan không có items")
    void getStudyPlanDetail_WhenPlanHasNoItems_ShouldReturnEmptyItems() {
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(300)
                .track(track)
                .studentProfile(student)
                .studyPlanItems(new ArrayList<>())
                .build();

        when(studyPlanRepository.findById(300)).thenReturn(Optional.of(plan));
        when(studyPlanConverter.toResponseSummery(plan)).thenReturn(
                com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse.builder().trackId(1).build()
        );

        var response = studyPlanService.getStudyPlanDetail(300);

        assertThat(response.getStudyPlanItems()).isEmpty();
    }

    // =========================================================
    // TC-SP-20: getStudyPlanDetail - trạng thái test bị LOCK
    // =========================================================
    /**
     * TC-SP-20
     * Objective: Test item trong response phải có status="LOCK" khi testService.isLock=true
     * Input: test bị khóa
     * Expected: item response phần tests phải có status="LOCK"
     */
    @Test
    @DisplayName("TC-SP-20: getStudyPlanDetail map trạng thái TEST = LOCK khi test bị khóa")
    void getStudyPlanDetail_WhenFirstItemIsTestAndLocked_ShouldMapLockStatus() {
        TestEntity test = TestEntity.builder().id(500).name("Locked Test").type("FINAL").build();
        LessonEntity lesson = LessonEntity.builder().id(501).title("L2").build();
        StudyPlanItemEntity item = StudyPlanItemEntity.builder()
                .date(LocalDate.of(2026, 6, 1))
                .test(test)
                .build();
        StudyPlanItemEntity item2 = StudyPlanItemEntity.builder()
                .date(LocalDate.of(2026, 6, 2))
                .lesson(lesson)
                .build();
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(301)
                .track(track)
                .studentProfile(student)
                .studyPlanItems(List.of(item, item2))
                .build();

        when(studyPlanRepository.findById(301)).thenReturn(Optional.of(plan));
        when(studyPlanConverter.toResponseSummery(plan)).thenReturn(
                com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse.builder().trackId(1).build()
        );
        when(testService.isLock(500, 1)).thenReturn(true);
        when(testService.isCompletedTest(500, 1)).thenReturn(false);
        when(lessonService.isLockLesson(501, 1)).thenReturn(false);
        when(lessonService.isCompletedLesson(501, 1)).thenReturn(false);
        when(lessonService.completedStar(501, 1)).thenReturn(0);

        var response = studyPlanService.getStudyPlanDetail(301);

        assertThat(response.getStudyPlanItems()).hasSize(2);
        assertThat(response.getStudyPlanItems().get(0).getTests()).hasSize(1);
        assertThat(response.getStudyPlanItems().get(0).getTests().get(0).getStatus()).isEqualTo("LOCK");
    }

    // =========================================================
    // TC-SP-21: getStudyPlanDetail - 1 item TEST duy nhất
    // =========================================================
    /**
     * TC-SP-21
     * Objective: Trả về chi tiết plan khi item cuối (và duy nhất) là TEST
     * Input: Plan có đúng 1 item TEST
     * Expected: getTests() không null và có size=1, status là UNLOCK
     */
    @Test
    @DisplayName("TC-SP-21: getStudyPlanDetail với 1 TEST duy nhất vẫn phải có tests trong item cuối")
    void getStudyPlanDetail_WhenOnlyOneTestItem_ShouldKeepTestsInLastItem() {
        TestEntity test = TestEntity.builder().id(600).name("Single Test").type("MINI").build();
        StudyPlanItemEntity onlyItem = StudyPlanItemEntity.builder()
                .date(LocalDate.of(2026, 6, 3))
                .test(test)
                .build();
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(400)
                .track(track)
                .studentProfile(student)
                .studyPlanItems(List.of(onlyItem))
                .build();

        when(studyPlanRepository.findById(400)).thenReturn(Optional.of(plan));
        when(studyPlanConverter.toResponseSummery(plan)).thenReturn(
                com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse.builder().trackId(1).build()
        );
        when(testService.isLock(600, 1)).thenReturn(false);
        when(testService.isCompletedTest(600, 1)).thenReturn(false);

        var response = studyPlanService.getStudyPlanDetail(400);

        assertThat(response.getStudyPlanItems()).hasSize(1);
        assertThat(response.getStudyPlanItems().get(0).getTests()).isNotNull();
        assertThat(response.getStudyPlanItems().get(0).getTests()).hasSize(1);
        assertThat(response.getStudyPlanItems().get(0).getTests().get(0).getStatus()).isEqualTo("UNLOCK");
    }

    // =========================================================
    // TC-SP-22: createStudyPlan - track không tồn tại 
    // =========================================================
    /**
     * TC-SP-22
     * Objective: Không được đánh dấu plan cũ là inactive (status=1) khi track không tồn tại
     * Input: request tạo plan, trackId không tồn tại
     * Expected: Ném RuntimeException, plan cũ giữ nguyên status=0, không gọi saveAll()
     */
    @Test
    @DisplayName("TC-SP-22: createStudyPlan không được deactivate plan cũ khi track không tồn tại")
    void createStudyPlan_WhenTrackNotFound_ShouldNotDeactivateOldPlans() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(999)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();
        StudyPlanEntity oldActivePlan = StudyPlanEntity.builder().id(10).status(0).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(999, 1))
                .thenReturn(List.of(oldActivePlan));
        when(trackRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.createStudyPlan(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Track not found");

        assertThat(oldActivePlan.getStatus()).isEqualTo(0);
        verify(studyPlanRepository, never()).saveAll(anyList());
    }

    // =========================================================
    // TC-SP-23: createStudyPlan - student không tồn tại 
    // =========================================================
    /**
     * TC-SP-23
     * Objective: Không được đánh dấu plan cũ là inactive khi student không tồn tại
     * Input: request tạo plan, student profile không tồn tại
     * Expected: Ném AppException, plan cũ giữ nguyên status=0, không gọi saveAll()
     */
    @Test
    @DisplayName("TC-SP-23: createStudyPlan không được deactivate plan cũ khi student không tồn tại")
    void createStudyPlan_WhenStudentNotFound_ShouldNotDeactivateOldPlans() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(999)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();
        StudyPlanEntity oldActivePlan = StudyPlanEntity.builder().id(10).status(0).build();
        LessonEntity lesson = LessonEntity.builder().id(700).mediaassets(new ArrayList<>()).build();
        ModuleEntity module = ModuleEntity.builder().id(70).type(ModuleType.LESSON).lessons(List.of(lesson)).build();
        CourseEntity course = CourseEntity.builder().id(70).modules(List.of(module)).build();
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(70).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(70).enrollmentCourses(List.of(ec)).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 999))
                .thenReturn(List.of(oldActivePlan));
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 999))
                .thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = catchThrowableOfType(
                () -> studyPlanService.createStudyPlan(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDENT_PROFILE_NOT_FOUND);
        assertThat(oldActivePlan.getStatus()).isEqualTo(0);
        verify(studyPlanRepository, never()).saveAll(anyList());
    }

    // =========================================================
    // TC-SP-24: createStudyPlan - lesson không tồn tại 
    // =========================================================
    /**
     * TC-SP-24
     * Objective: Không được đánh dấu plan cũ là inactive khi lesson trong track không tồn tại
     * Input: request tạo plan, lesson id không tồn tại
     * Expected: Ném AppException(LESSON_NOT_FOUND), plan cũ giữ nguyên status=0
     */
    @Test
    @DisplayName("TC-SP-24: createStudyPlan không được deactivate plan cũ khi lesson trong course không tồn tại")
    void createStudyPlan_WhenLessonNotFound_ShouldNotDeactivateOldPlans() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();
        StudyPlanEntity oldActivePlan = StudyPlanEntity.builder().id(10).status(0).build();
        LessonEntity missingLesson = LessonEntity.builder().id(701).mediaassets(new ArrayList<>()).build();
        ModuleEntity module = ModuleEntity.builder().id(71).type(ModuleType.LESSON).lessons(List.of(missingLesson)).build();
        CourseEntity course = CourseEntity.builder().id(71).modules(List.of(module)).build();
        EnrollmentCourseEntity ec = EnrollmentCourseEntity.builder().id(71).course(course).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().id(71).enrollmentCourses(List.of(ec)).build();

        when(studyPlanRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(oldActivePlan));
        when(trackRepository.findById(1)).thenReturn(Optional.of(track));
        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(enrollment));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(lessonRepository.findById(701)).thenReturn(Optional.empty());

        AppException ex = catchThrowableOfType(
                () -> studyPlanService.createStudyPlan(request),
                AppException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LESSON_NOT_FOUND);
        assertThat(oldActivePlan.getStatus()).isEqualTo(0);
        verify(studyPlanRepository, never()).saveAll(anyList());
    }

    // =========================================================
    // TC-SP-25: verifyInformation - track không có unit 
    // =========================================================
    /**
     * TC-SP-25 
     * Objective: Khi track không có khóa học nào, hệ thống trả về summary rỗng thay vì crash
     * Input: enrollmentRepository trả về empty
     * Expected: getTongSoUnits=0, getTongSoBuoiHoc=0, getNgayHoanThanh=null
     */
    @Test
    @DisplayName("TC-SP-25: verifyInformation với track không có unit phải trả summary rỗng, không crash")
    void verifyInformation_WhenTrackHasNoUnits_ShouldReturnEmptySummary() {
        com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest request =
                com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest.builder()
                        .trackId(1)
                        .studentProfileId(1)
                        .startDate(LocalDate.of(2026, 5, 11))
                        .soLuongNgayHoc(5)
                        .ngayHocTrongTuan(new ArrayList<>(List.of(1, 3, 5)))
                        .build();

        when(enrollmentRepository.findByTrack_IdAndStudentProfile_Id(1, 1))
                .thenReturn(Collections.emptyList());
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));

        var response = studyPlanService.verifyInformation(request);

        assertThat(response.getTongSoUnits()).isZero();
        assertThat(response.getTongSoBuoiHoc()).isZero();
        assertThat(response.getNgayHoanThanh()).isNull();
    }

    // =========================================================
    // TC-SP-26: getInformation - clamp số ngày còn lại (RED test)
    // =========================================================
    /**
     * TC-SP-26 (RED)
     * Objective: Số ngày học còn lại không được phép nhỏ hơn 0 (clamp về 0)
     * Input: startDate từ quá khứ (đã quá thời hạn soLuongNgayHoc)
     * Expected: getSoNgayHocConLai trả về 0 (thay vì số âm)
     */
    @Test
    @DisplayName("TC-SP-26 RED: getInformation không được trả số ngày học còn lại âm")
    void getInformation_WhenStudyDaysAlreadyExceeded_ShouldClampRemainingDaysToZero() {
        StudyPlanItemEntity oldItem = StudyPlanItemEntity.builder()
                .date(LocalDate.now().minusDays(30))
                .lesson(LessonEntity.builder().id(800).title("Old lesson").build())
                .build();
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .id(800)
                .studentProfile(student)
                .startDate(LocalDate.now().minusDays(30))
                .soLuongNgayHoc(2)
                .ngayHocTrongTuan(List.of(1, 2, 3, 4, 5, 6, 7))
                .studyPlanItems(List.of(oldItem))
                .build();

        when(studyPlanRepository.findById(800)).thenReturn(Optional.of(plan));
        when(lessonService.isCompletedLesson(800, 1)).thenReturn(false);
        when(lessonService.isLockLesson(800, 1)).thenReturn(false);

        var response = studyPlanService.getInformation(800);

        assertThat(response.getSoNgayHocConLai()).isZero();
    }

    // =========================================================
    // TC-SP-27: chooseStudyDate - target hợp lệ
    // =========================================================
    /**
     * TC-SP-27
     * Objective: chooseStudyDate trả về đúng ngày target khi target hợp lệ và chưa dùng
     * Input: currentDate, gapDays=2, target thuộc studyDays và chưa dùng
     * Expected: Trả về đúng ngày target
     */
    @Test
    @DisplayName("TC-SP-27: chooseStudyDate trả về target khi target hợp lệ và chưa dùng")
    void chooseStudyDate_WhenTargetValidAndUnused_ShouldReturnTarget() {
        LocalDate currentDate = LocalDate.of(2026, 5, 11); // Monday
        List<Integer> studyDays = List.of(1, 3, 5);
        Set<LocalDate> usedDates = new HashSet<>();
        LocalDate startDate = LocalDate.of(2026, 5, 11);

        LocalDate result = invokeChooseStudyDate(currentDate, 2, studyDays, usedDates, startDate);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 13)); // Wednesday
    }

    // =========================================================
    // TC-SP-28: chooseStudyDate - before < startDate
    // =========================================================
    /**
     * TC-SP-28
     * Objective: Nếu ngày before < startDate thì hàm phải fallback sang after
     * Input: currentDate, gapDays dẫn tới target trước startDate
     * Expected: Trả về ngày hợp lệ bằng hoặc sau startDate
     */
    @Test
    @DisplayName("TC-SP-28: chooseStudyDate nếu before < startDate thì buộc chọn after")
    void chooseStudyDate_WhenBeforeIsBeforeStartDate_ShouldReturnAfter() {
        LocalDate currentDate = LocalDate.of(2026, 5, 12); // Tuesday
        List<Integer> studyDays = List.of(2, 4); // Tue Thu
        Set<LocalDate> usedDates = new HashSet<>();
        LocalDate startDate = LocalDate.of(2026, 5, 12);

        LocalDate result = invokeChooseStudyDate(currentDate, -2, studyDays, usedDates, startDate);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 12)); // Tuesday
    }

    // =========================================================
    // TC-SP-29: chooseStudyDate - ưu tiên before
    // =========================================================
    /**
     * TC-SP-29
     * Objective: Nếu before có khoảng cách (dist) <= after và chưa dùng, ưu tiên before
     * Input: gapDays=0, target đã dùng, before khả dụng và gần target hơn
     * Expected: Trả về before
     */
    @Test
    @DisplayName("TC-SP-29: chooseStudyDate ưu tiên before khi distBefore <= distAfter và before chưa dùng")
    void chooseStudyDate_WhenBeforeCloserOrEqualAndUnused_ShouldReturnBefore() {
        LocalDate currentDate = LocalDate.of(2026, 5, 12); // Tuesday
        List<Integer> studyDays = List.of(1, 3, 5); // Mon Wed Fri
        Set<LocalDate> usedDates = new HashSet<>(Set.of(LocalDate.of(2026, 5, 12))); // force target used
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        LocalDate result = invokeChooseStudyDate(currentDate, 0, studyDays, usedDates, startDate);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 11)); // Monday
    }

    // =========================================================
    // TC-SP-30: chooseStudyDate - fallback sau khi before đã dùng
    // =========================================================
    /**
     * TC-SP-30
     * Objective: Nếu both target và before đã dùng, hàm phải trả về after
     * Input: target và before đều nằm trong usedDates
     * Expected: Trả về after
     */
    @Test
    @DisplayName("TC-SP-30: chooseStudyDate fallback sang after khi before đã dùng")
    void chooseStudyDate_WhenBeforeUsed_ShouldReturnAfter() {
        LocalDate currentDate = LocalDate.of(2026, 5, 12); // Tuesday
        List<Integer> studyDays = List.of(1, 3, 5); // Mon Wed Fri
        Set<LocalDate> usedDates = new HashSet<>(Set.of(
                LocalDate.of(2026, 5, 12), // target used
                LocalDate.of(2026, 5, 11)  // before used
        ));
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        LocalDate result = invokeChooseStudyDate(currentDate, 0, studyDays, usedDates, startDate);

        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 13)); // Wednesday
    }

    // =========================================================
    // TC-SP-31: sapXepLaiBaiHoc - cân bằng thời lượng
    // =========================================================
    /**
     * TC-SP-31
     * Objective: sapXepLaiBaiHoc cân bằng thời gian học giữa các buổi khi chênh lệch
     * Input: 1 buổi nhiều bài học (thời gian cao), 1 buổi ít bài học (thời gian thấp)
     * Expected: Độ chênh lệch thời gian sau khi sắp xếp giảm so với ban đầu
     */
    @Test
    @DisplayName("TC-SP-31: sapXepLaiBaiHoc cân bằng lại khi một buổi lệch thời lượng")
    void sapXepLaiBaiHoc_WhenTimeIsSkewed_ShouldRebalance() {
        StudyPlanServiceImpl.ItemWrapperLessonAndTest heavy1 = new StudyPlanServiceImpl.ItemWrapperLessonAndTest(1, "lesson", 100);
        StudyPlanServiceImpl.ItemWrapperLessonAndTest heavy2 = new StudyPlanServiceImpl.ItemWrapperLessonAndTest(2, "lesson", 80);
        StudyPlanServiceImpl.ItemWrapperLessonAndTest light1 = new StudyPlanServiceImpl.ItemWrapperLessonAndTest(3, "lesson", 20);

        Deque<StudyPlanServiceImpl.ItemWrapperLessonAndTest> day0 = new ArrayDeque<>();
        day0.addLast(heavy1);
        day0.addLast(heavy2);
        Deque<StudyPlanServiceImpl.ItemWrapperLessonAndTest> day1 = new ArrayDeque<>();
        day1.addLast(light1);

        StudyPlanServiceImpl.ItemWrapperForNgayHoc buoi0 = StudyPlanServiceImpl.ItemWrapperForNgayHoc.builder()
                .sttBuoiHoc(0).thoiGianDenBaiHocTiepTheo(1).listItemLessonAndTest(day0).build();
        StudyPlanServiceImpl.ItemWrapperForNgayHoc buoi1 = StudyPlanServiceImpl.ItemWrapperForNgayHoc.builder()
                .sttBuoiHoc(1).thoiGianDenBaiHocTiepTheo(1).listItemLessonAndTest(day1).build();

        List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> plan = new ArrayList<>(List.of(buoi0, buoi1));

        int beforeDiff = Math.abs(totalTime(day0) - totalTime(day1));
        invokeSapXepLaiBaiHoc(plan);
        int afterDiff = Math.abs(
                totalTime(plan.get(0).getListItemLessonAndTest()) -
                        totalTime(plan.get(1).getListItemLessonAndTest())
        );

        assertThat(afterDiff).isLessThan(beforeDiff);
    }

    // =========================================================
    // TC-SP-32: sapXepLaiBaiHoc - rỗng
    // =========================================================
    /**
     * TC-SP-32
     * Objective: sapXepLaiBaiHoc không đổi nếu tất cả các buổi đều rỗng
     * Input: Plan có 2 buổi, list bài học đều empty
     * Expected: Giữ nguyên empty list
     */
    @Test
    @DisplayName("TC-SP-32: sapXepLaiBaiHoc không đổi khi cả hai buổi đều rỗng")
    void sapXepLaiBaiHoc_WhenBothDaysEmpty_ShouldKeepEmpty() {
        StudyPlanServiceImpl.ItemWrapperForNgayHoc buoi0 = StudyPlanServiceImpl.ItemWrapperForNgayHoc.builder()
                .sttBuoiHoc(0).thoiGianDenBaiHocTiepTheo(1).listItemLessonAndTest(new ArrayDeque<>()).build();
        StudyPlanServiceImpl.ItemWrapperForNgayHoc buoi1 = StudyPlanServiceImpl.ItemWrapperForNgayHoc.builder()
                .sttBuoiHoc(1).thoiGianDenBaiHocTiepTheo(1).listItemLessonAndTest(new ArrayDeque<>()).build();
        List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> plan = new ArrayList<>(List.of(buoi0, buoi1));

        invokeSapXepLaiBaiHoc(plan);

        assertThat(plan.get(0).getListItemLessonAndTest()).isEmpty();
        assertThat(plan.get(1).getListItemLessonAndTest()).isEmpty();
    }

    // =========================================================
    // TC-SP-33: createDanhSachBaiHoc - soLuongLesson <= soNgayHoc
    // =========================================================
    /**
     * TC-SP-33
     * Objective: Mỗi buổi học có đúng 1 item khi số lượng item <= số ngày học
     * Input: 2 items, 5 ngày học
     * Expected: result có size=2 buổi, mỗi buổi có 1 item
     */
    @Test
    @DisplayName("TC-SP-33: createDanhSachBaiHoc nhánh <= tạo mỗi buổi 1 item")
    void createDanhSachBaiHoc_WhenTotalUnitsLessOrEqualDays_ShouldCreateOneItemPerSession() {
        List<StudyPlanServiceImpl.ItemWrapperLessonAndTest> items = List.of(
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(1, "lesson", 1000),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(2, "test", 1800)
        );

        List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> result =
                invokeCreateDanhSachBaiHoc(items, Collections.emptyList(), 2, 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getListItemLessonAndTest()).hasSize(1);
        assertThat(result.get(1).getListItemLessonAndTest()).hasSize(1);
        assertThat(result.get(0).getThoiGianDenBaiHocTiepTheo()).isGreaterThanOrEqualTo(0);
    }

    // =========================================================
    // TC-SP-34: createDanhSachBaiHoc - soLuongLesson > soNgayHoc
    // =========================================================
    /**
     * TC-SP-34
     * Objective: Các buổi học được gom nhóm item hợp lý khi số lượng item > số ngày học
     * Input: 4 items, 2 ngày học
     * Expected: result có size=2 buổi, mỗi buổi chứa >0 item (gom nhóm)
     */
    @Test
    @DisplayName("TC-SP-34: createDanhSachBaiHoc nhánh > gom nhóm theo thời gian học trung bình")
    void createDanhSachBaiHoc_WhenTotalUnitsGreaterThanDays_ShouldGroupByAverageTime() {
        List<StudyPlanServiceImpl.ItemWrapperLessonAndTest> items = List.of(
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(10, "lesson", 1800),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(11, "lesson", 1800),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(12, "lesson", 1800),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(13, "lesson", 1800)
        );

        LessonEntity l1 = LessonEntity.builder().id(10).mediaassets(new ArrayList<>()).build();
        LessonEntity l2 = LessonEntity.builder().id(11).mediaassets(new ArrayList<>()).build();
        LessonEntity l3 = LessonEntity.builder().id(12).mediaassets(new ArrayList<>()).build();
        LessonEntity l4 = LessonEntity.builder().id(13).mediaassets(new ArrayList<>()).build();
        ModuleEntity module = ModuleEntity.builder().id(90).type(ModuleType.LESSON).lessons(List.of(l1, l2, l3, l4)).build();
        CourseEntity course = CourseEntity.builder().id(90).modules(List.of(module)).build();
        module.setCourse(course);

        List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> result =
                invokeCreateDanhSachBaiHoc(items, List.of(course), 4, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getListItemLessonAndTest()).isNotEmpty();
        assertThat(result.get(1).getListItemLessonAndTest()).isNotEmpty();
    }

    // =========================================================
    // TC-SP-35: createDanhSachBaiHoc - thiếu ngày
    // =========================================================
    /**
     * TC-SP-35
     * Objective: Nếu tổng số buổi sinh ra ít hơn số ngày target thì phải thêm buổi rỗng
     * Input: 3 items (soLuongLesson=6), số ngày học mong đợi = 5
     * Expected: result có size=5, có ít nhất 1 buổi trống
     */
    @Test
    @DisplayName("TC-SP-35: createDanhSachBaiHoc nhánh > thêm buổi rỗng khi thiếu số ngày")
    void createDanhSachBaiHoc_WhenGeneratedSessionsLessThanTargetDays_ShouldPadEmptySessions() {
        List<StudyPlanServiceImpl.ItemWrapperLessonAndTest> items = List.of(
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(21, "lesson", 1800),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(22, "lesson", 1800),
                new StudyPlanServiceImpl.ItemWrapperLessonAndTest(23, "lesson", 1800)
        );

        LessonEntity l1 = LessonEntity.builder().id(21).mediaassets(new ArrayList<>()).build();
        LessonEntity l2 = LessonEntity.builder().id(22).mediaassets(new ArrayList<>()).build();
        LessonEntity l3 = LessonEntity.builder().id(23).mediaassets(new ArrayList<>()).build();
        ModuleEntity module = ModuleEntity.builder().id(91).type(ModuleType.LESSON).lessons(List.of(l1, l2, l3)).build();
        CourseEntity course = CourseEntity.builder().id(91).modules(List.of(module)).build();
        module.setCourse(course);

        List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> result =
                invokeCreateDanhSachBaiHoc(items, List.of(course), 6, 5);

        assertThat(result).hasSize(5);
        long emptySessions = result.stream()
                .filter(s -> s.getListItemLessonAndTest().isEmpty())
                .count();
        assertThat(emptySessions).isGreaterThan(0);
    }

    private LocalDate invokeChooseStudyDate(
            LocalDate currentDate,
            int gapDays,
            List<Integer> studyDays,
            Set<LocalDate> usedDates,
            LocalDate startDate
    ) {
        try {
            Method method = StudyPlanServiceImpl.class.getDeclaredMethod(
                    "chooseStudyDate",
                    LocalDate.class,
                    int.class,
                    List.class,
                    Set.class,
                    LocalDate.class
            );
            method.setAccessible(true);
            return (LocalDate) method.invoke(studyPlanService, currentDate, gapDays, studyDays, usedDates, startDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeSapXepLaiBaiHoc(List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> plan) {
        try {
            Method method = StudyPlanServiceImpl.class.getDeclaredMethod("sapXepLaiBaiHoc", List.class);
            method.setAccessible(true);
            method.invoke(studyPlanService, plan);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<StudyPlanServiceImpl.ItemWrapperForNgayHoc> invokeCreateDanhSachBaiHoc(
            List<StudyPlanServiceImpl.ItemWrapperLessonAndTest> items,
            List<CourseEntity> courses,
            int soLuongLesson,
            int soNgayHoc
    ) {
        try {
            Method method = StudyPlanServiceImpl.class.getDeclaredMethod(
                    "createDanhSachBaiHoc",
                    List.class,
                    List.class,
                    int.class,
                    int.class
            );
            method.setAccessible(true);
            return (List<StudyPlanServiceImpl.ItemWrapperForNgayHoc>) method.invoke(
                    studyPlanService, items, courses, soLuongLesson, soNgayHoc
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int totalTime(Deque<StudyPlanServiceImpl.ItemWrapperLessonAndTest> items) {
        int total = 0;
        for (StudyPlanServiceImpl.ItemWrapperLessonAndTest item : items) {
            total += item.getThoiGianHoc();
        }
        return total;
    }
}
