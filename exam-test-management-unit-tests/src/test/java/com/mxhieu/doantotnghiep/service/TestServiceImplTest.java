package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.converter.TestConverter;
import com.mxhieu.doantotnghiep.dto.request.TestRequest;
import com.mxhieu.doantotnghiep.dto.response.TestResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.impl.TestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho TestServiceImpl - quản lý bài test đầu vào (First Test) và Mini Test.
 * Sử dụng Mockito để mock các dependency (repository, converter).
 * Mỗi test case có comment ghi rõ Test Case ID tương ứng.
 */
@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestConverter testConverter;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private EnrollmentCourseRepository enrollmentcourseRepository;

    @Mock
    private TestProgressRepository testProgressRepository;

    @InjectMocks
    private TestServiceImpl testService;

    // ======================== createTest ========================

    /**
     * TC: TS_01 - Tạo test thành công khi module tồn tại.
     * Input: TestRequest hợp lệ với moduleId = 1
     * Expected: testRepository.save() được gọi đúng 1 lần, entity có module đúng
     */
    @Test
    @DisplayName("TS_01: createTest - tạo test thành công khi module tồn tại")
    void createTest_shouldSaveTest_whenModuleExists() {
        // Arrange - chuẩn bị dữ liệu đầu vào
        TestRequest request = TestRequest.builder()
                .type("MINI_TEST")
                .name("Test Module 1")
                .moduleId(1)
                .build();

        ModuleEntity moduleEntity = ModuleEntity.builder().id(1).title("Module 1").build();
        TestEntity testEntity = new TestEntity();

        when(testConverter.toEntity(request, TestEntity.class)).thenReturn(testEntity);
        when(moduleRepository.findById(1)).thenReturn(Optional.of(moduleEntity));

        // Act - thực thi hàm cần test
        testService.createTest(request);

        // Assert - kiểm tra kết quả
        // CheckDB: verify entity được lưu với module đúng
        verify(testRepository, times(1)).save(testEntity);
        assertEquals(moduleEntity, testEntity.getModule());
    }

    /**
     * TC: TS_02 - Throw AppException khi module không tồn tại.
     * Input: TestRequest với moduleId = 999 (không tồn tại trong DB)
     * Expected: Throw AppException với ErrorCode.MODULE_NOT_FOUND
     */
    @Test
    @DisplayName("TS_02: createTest - throw exception khi module không tồn tại")
    void createTest_shouldThrowException_whenModuleNotFound() {
        // Arrange
        TestRequest request = TestRequest.builder().moduleId(999).build();
        TestEntity testEntity = new TestEntity();

        when(testConverter.toEntity(request, TestEntity.class)).thenReturn(testEntity);
        when(moduleRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> testService.createTest(request));
        assertEquals(ErrorCode.MODULE_NOT_FOUND, exception.getErrorCode());

        // Verify: không có test nào được lưu vào DB
        verify(testRepository, never()).save(any());
    }

    // ======================== createFirstTest ========================

    /**
     * TC: TS_03 - Tạo First Test (bài test đầu vào) thành công, không gắn module.
     * Input: TestRequest với type = "FIRST_TEST"
     * Expected: testRepository.save() được gọi, entity KHÔNG có module
     */
    @Test
    @DisplayName("TS_03: createFirstTest - tạo bài test đầu vào thành công")
    void createFirstTest_shouldSaveWithoutModule() {
        // Arrange
        TestRequest request = TestRequest.builder()
                .type("FIRST_TEST")
                .name("Bài test đầu vào 1")
                .build();
        TestEntity testEntity = new TestEntity();

        when(testConverter.toEntity(request, TestEntity.class)).thenReturn(testEntity);

        // Act
        testService.createFirstTest(request);

        // Assert
        // CheckDB: verify entity được lưu, không cần module
        ArgumentCaptor<TestEntity> captor = ArgumentCaptor.forClass(TestEntity.class);
        verify(testRepository).save(captor.capture());
        assertNull(captor.getValue().getModule());
    }

    // ======================== commpletedStar ========================

    /**
     * TC: TS_04 - Trả về 3 sao khi điểm cao nhất = 100.
     * Input: testId=1, studentProfileId=1, attempt có totalScore = 100
     * Expected: return 3
     */
    @Test
    @DisplayName("TS_04: commpletedStar - trả về 3 sao khi điểm = 100")
    void commpletedStar_shouldReturn3Stars_whenScoreIs100() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder().totalScore(100f).build();
        when(testAttemptRepository.findByTestIdAndStudentProfileId(1, 1))
                .thenReturn(List.of(attempt));

        // Act
        int result = testService.commpletedStar(1, 1);

        // Assert
        assertEquals(3, result);
    }

    /**
     * TC: TS_05 - Trả về 2 sao khi điểm cao nhất >= 70 và < 100.
     * Input: testId=1, studentProfileId=1, attempts có totalScore = 60 và 85
     * Expected: return 2 (lấy max = 85, nằm trong [70, 100))
     */
    @Test
    @DisplayName("TS_05: commpletedStar - trả về 2 sao khi điểm tốt nhất = 85")
    void commpletedStar_shouldReturn2Stars_whenBestScoreIs85() {
        // Arrange - 2 lượt làm, điểm lần lượt 60 và 85
        TestAttemptEntity attempt1 = TestAttemptEntity.builder().totalScore(60f).build();
        TestAttemptEntity attempt2 = TestAttemptEntity.builder().totalScore(85f).build();
        when(testAttemptRepository.findByTestIdAndStudentProfileId(1, 1))
                .thenReturn(List.of(attempt1, attempt2));

        // Act
        int result = testService.commpletedStar(1, 1);

        // Assert
        assertEquals(2, result);
    }

    /**
     * TC: TS_06 - Trả về 1 sao khi điểm cao nhất < 70.
     * Input: testId=1, studentProfileId=1, attempt có totalScore = 45
     * Expected: return 1
     */
    @Test
    @DisplayName("TS_06: commpletedStar - trả về 1 sao khi điểm < 70")
    void commpletedStar_shouldReturn1Star_whenScoreBelow70() {
        // Arrange
        TestAttemptEntity attempt = TestAttemptEntity.builder().totalScore(45f).build();
        when(testAttemptRepository.findByTestIdAndStudentProfileId(1, 1))
                .thenReturn(List.of(attempt));

        // Act
        int result = testService.commpletedStar(1, 1);

        // Assert
        assertEquals(1, result);
    }

    /**
     * TC: TS_07 - Trả về 0 sao khi học sinh chưa làm bài (không có attempt).
     * Input: testId=1, studentProfileId=1, không có attempt nào
     * Expected: return 0
     */
    @Test
    @DisplayName("TS_07: commpletedStar - trả về 0 khi chưa có attempt")
    void commpletedStar_shouldReturn0_whenNoAttempts() {
        // Arrange
        when(testAttemptRepository.findByTestIdAndStudentProfileId(1, 1))
                .thenReturn(Collections.emptyList());

        // Act
        int result = testService.commpletedStar(1, 1);

        // Assert
        assertEquals(0, result);
    }

    // ======================== getMaxScore ========================

    /**
     * TC: TS_08 - Lấy điểm cao nhất từ nhiều lượt làm.
     * Input: testId=1, studentProfileId=1, 3 attempts (50, 90, 70)
     * Expected: return 90.0 (điểm cao nhất)
     */
    @Test
    @DisplayName("TS_08: getMaxScore - trả về điểm cao nhất từ nhiều lần làm bài")
    void getMaxScore_shouldReturnHighestScore() {
        // Arrange - 3 lượt làm bài với điểm khác nhau
        TestAttemptEntity a1 = TestAttemptEntity.builder().totalScore(50f).build();
        TestAttemptEntity a2 = TestAttemptEntity.builder().totalScore(90f).build();
        TestAttemptEntity a3 = TestAttemptEntity.builder().totalScore(70f).build();
        when(testAttemptRepository.findByTestIdAndStudentProfileId(1, 1))
                .thenReturn(List.of(a1, a2, a3));

        // Act
        float result = testService.getMaxScore(1, 1);

        // Assert
        assertEquals(90f, result);
    }

    // ======================== isLock - BUG DETECTION ========================

    /**
     * TC: TS_09 - BUG FOUND: isLock throw NullPointerException khi hoc sinh chua ghi danh.
     * Source code bug: dong 157 trong TestServiceImpl.java
     *   statusOfCourse.equals("LOCK") - khong kiem tra null truoc khi goi .equals()
     * Khi findStatus() tra ve null (hoc sinh chua dang ky khoa hoc), code se throw NPE.
     *
     * Input: testId=1, studentId=1, enrollmentcourseRepository.findStatus() tra ve null
     * Expected: Nen tra ve true (khoa bai test) hoac throw AppException co y nghia
     * Actual: Throw NullPointerException (BUG)
     */
    @Test
    @DisplayName("TS_09: [BUG] isLock - NullPointerException khi hoc sinh chua ghi danh")
    void isLock_shouldHandleNull_whenStudentNotEnrolled() {
        // Arrange - hoc sinh chua ghi danh, findStatus tra ve null
        CourseEntity course = CourseEntity.builder().id(1).build();
        ModuleEntity module = ModuleEntity.builder().id(1).course(course).build();
        TestEntity test = TestEntity.builder().id(1).module(module).build();

        when(testRepository.findById(1)).thenReturn(Optional.of(test));
        // findStatus tra ve null vi hoc sinh chua co enrollment cho khoa hoc nay
        when(enrollmentcourseRepository.findStatus(1, 1)).thenReturn(null);

        // Act & Assert
        // Test FAIL: Code throw NullPointerException thay vi xu ly null dung cach
        // Bug: statusOfCourse.equals("LOCK") khi statusOfCourse = null => NPE
        // Fix de xuat: dung "LOCK".equals(statusOfCourse) hoac kiem tra null truoc
        assertDoesNotThrow(() -> testService.isLock(1, 1));
    }

    // ======================== isCompletedTest - BUG DETECTION ========================

    /**
     * TC: TS_10 - BUG FOUND: isCompletedTest throw NullPointerException khi process la null.
     * Source code bug: dong 175 trong TestServiceImpl.java
     *   testProgressEntities.get(0).getProcess() == 0
     *   getProcess() tra ve Integer (boxed), khi so sanh voi int (0), Java auto-unbox
     *   null.intValue() => NullPointerException
     *
     * Input: testId=1, studentProfileId=1, progress ton tai nhung process = null
     * Expected: Nen tra ve false (chua hoan thanh)
     * Actual: Throw NullPointerException (BUG)
     */
    @Test
    @DisplayName("TS_10: [BUG] isCompletedTest - NPE khi process la null trong DB")
    void isCompletedTest_shouldHandleNullProcess() {
        // Arrange - progress ton tai nhung truong process chua duoc set (null trong DB)
        TestProgressEntity progress = new TestProgressEntity();
        progress.setId(1);
        progress.setProcess(null); // process chua duoc set trong DB

        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(1, 1))
                .thenReturn(List.of(progress));

        // Act & Assert
        // Test FAIL: Code throw NullPointerException do auto-unboxing null Integer
        // Bug: getProcess() tra ve null, so sanh == 0 auto-unbox null => NPE
        // Fix de xuat: kiem tra null truoc: process != null && (process == 0 || process == 1)
        assertDoesNotThrow(() -> testService.isCompletedTest(1, 1));
    }
}
