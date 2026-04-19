package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.request.TestProgressRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.impl.TestProgressServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestProgressServiceImplTest {

    @Mock
    private TestProgressRepository testProgressRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private LessonService lessonService;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private LessonProgressService lessonProgressService;

    @Mock
    private TrackService trackService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private TestProgressServiceImpl testProgressService;

    // TP_01
    @Test
    @DisplayName("TP_01: checkCompletionCondition - completed when score >= 50")
    void checkCompletionCondition_completed_whenScoreAbove50() {
        Integer testId = 1;
        Integer studentprofileId = 1;

        TestProgressRequest request = TestProgressRequest.builder()
                .testId(testId)
                .studentprofileId(studentprofileId)
                .build();

        CourseEntity course = CourseEntity.builder().id(1).build();
        ModuleEntity module = ModuleEntity.builder().id(1).course(course).build();
        TestEntity testEntity = TestEntity.builder().id(testId).module(module).build();

        StudentProfileEntity studentProfile = StudentProfileEntity.builder().id(studentprofileId).build();

        TestProgressEntity progressEntity = TestProgressEntity.builder()
                .id(1)
                .process(0)
                .build();

        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .totalScore(80f)
                .build();

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(studentprofileId)).thenReturn(Optional.of(studentProfile));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(testId, studentprofileId))
                .thenReturn(List.of(progressEntity));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(testId, studentprofileId))
                .thenReturn(Optional.of(attempt));
        when(lessonService.getNextLessonOrTest(any(LessonOrTestAroundRequest.class)))
                .thenThrow(new AppException(ErrorCode.LESSON_NOT_HAS_NEXT));

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertTrue(result);

        ArgumentCaptor<TestProgressEntity> captor = ArgumentCaptor.forClass(TestProgressEntity.class);
        verify(testProgressRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getProcess());

        verify(lessonProgressService).unLockNextCourse(course, studentProfile);
    }

    // TP_02
    @Test
    @DisplayName("TP_02: checkCompletionCondition - not completed when score < 50")
    void checkCompletionCondition_notCompleted_whenScoreBelow50() {
        Integer testId = 1;
        Integer studentprofileId = 1;

        TestProgressRequest request = TestProgressRequest.builder()
                .testId(testId)
                .studentprofileId(studentprofileId)
                .build();

        CourseEntity course = CourseEntity.builder().id(1).build();
        ModuleEntity module = ModuleEntity.builder().id(1).course(course).build();
        TestEntity testEntity = TestEntity.builder().id(testId).module(module).build();

        StudentProfileEntity studentProfile = StudentProfileEntity.builder().id(studentprofileId).build();

        TestProgressEntity progressEntity = TestProgressEntity.builder()
                .id(1)
                .process(0)
                .build();

        TestAttemptEntity attempt = TestAttemptEntity.builder()
                .totalScore(30f)
                .build();

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(studentprofileId)).thenReturn(Optional.of(studentProfile));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(testId, studentprofileId))
                .thenReturn(List.of(progressEntity));
        when(testAttemptRepository.findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(testId, studentprofileId))
                .thenReturn(Optional.of(attempt));

        Boolean result = testProgressService.checkCompletionCondition(request);

        assertFalse(result);

        ArgumentCaptor<TestProgressEntity> captor = ArgumentCaptor.forClass(TestProgressEntity.class);
        verify(testProgressRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getProcess());
    }

    // TP_03
    @Test
    @DisplayName("TP_03: checkCompletionCondition - throw when test not found")
    void checkCompletionCondition_throwException_whenTestNotFound() {
        Integer testId = 999;
        Integer studentprofileId = 1;

        TestProgressRequest request = TestProgressRequest.builder()
                .testId(testId)
                .studentprofileId(studentprofileId)
                .build();

        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> testProgressService.checkCompletionCondition(request));

        assertEquals(ErrorCode.TEST_NOT_FOUND, exception.getErrorCode());
    }

    // TP_04
    @Test
    @DisplayName("TP_04: checkCompletionCondition - throw when student not found")
    void checkCompletionCondition_throwException_whenStudentNotFound() {
        Integer testId = 1;
        Integer studentprofileId = 999;

        TestProgressRequest request = TestProgressRequest.builder()
                .testId(testId)
                .studentprofileId(studentprofileId)
                .build();

        TestEntity testEntity = TestEntity.builder().id(testId).build();

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(studentprofileId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> testProgressService.checkCompletionCondition(request));

        assertEquals(ErrorCode.STUDENT_PROFILE_NOT_FOUND, exception.getErrorCode());
    }

    // TP_05
    @Test
    @DisplayName("TP_05: checkCompletionCondition - throw when no progress exists")
    void checkCompletionCondition_throwException_whenNoProgressExists() {
        Integer testId = 1;
        Integer studentprofileId = 1;

        TestProgressRequest request = TestProgressRequest.builder()
                .testId(testId)
                .studentprofileId(studentprofileId)
                .build();

        TestEntity testEntity = TestEntity.builder().id(testId).build();
        StudentProfileEntity studentProfile = StudentProfileEntity.builder().id(studentprofileId).build();

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(studentprofileId)).thenReturn(Optional.of(studentProfile));
        when(testProgressRepository.findByTest_IdAndStudentProfile_Id(testId, studentprofileId))
                .thenReturn(Collections.emptyList());

        AppException exception = assertThrows(AppException.class,
                () -> testProgressService.checkCompletionCondition(request));

        assertEquals(ErrorCode.TEST_PROGRESS_NOT_EXISTS, exception.getErrorCode());
    }
}
