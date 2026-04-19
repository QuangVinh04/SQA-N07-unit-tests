package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.converter.AssessmentConverter;
import com.mxhieu.doantotnghiep.dto.request.AssessmentRequest;
import com.mxhieu.doantotnghiep.dto.response.AssessmentResponse;
import com.mxhieu.doantotnghiep.entity.AssessmentEntity;
import com.mxhieu.doantotnghiep.entity.AssessmentQuestionEntity;
import com.mxhieu.doantotnghiep.entity.ExerciseTypeEntity;
import com.mxhieu.doantotnghiep.entity.TestEntity;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.AssessmentRepository;
import com.mxhieu.doantotnghiep.repository.ExerciseTypeRepository;
import com.mxhieu.doantotnghiep.repository.TestRepository;
import com.mxhieu.doantotnghiep.service.impl.AssessmentServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private AssessmentConverter assessmentConverter;

    @Mock
    private ExerciseTypeRepository exerciseTypeRepository;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    // AS_01: createAssessment - success
    @Test
    @DisplayName("AS_01: createAssessment - tạo assessment thành công")
    void createAssessment_Success() {
        AssessmentRequest request = AssessmentRequest.builder()
                .type("LISTENING_1")
                .testId(1)
                .title("Part 1")
                .build();

        ExerciseTypeEntity exerciseType = ExerciseTypeEntity.builder()
                .id(1)
                .code("LISTENING_1")
                .description("Listening Part 1")
                .build();

        TestEntity testEntity = TestEntity.builder().id(1).build();

        AssessmentEntity assessmentEntity = new AssessmentEntity();

        when(exerciseTypeRepository.findByCode("LISTENING_1")).thenReturn(Optional.of(exerciseType));
        when(testRepository.findById(1)).thenReturn(Optional.of(testEntity));
        when(assessmentConverter.toEntity(eq(request), eq(AssessmentEntity.class))).thenReturn(assessmentEntity);

        assessmentService.createAssessment(request);

        verify(assessmentRepository, times(1)).save(assessmentEntity);
        assertEquals(testEntity, assessmentEntity.getTest());
        assertEquals(exerciseType, assessmentEntity.getExercisetype());
    }

    // AS_02: createAssessment - exercise type not found
    @Test
    @DisplayName("AS_02: createAssessment - không tìm thấy exercise type")
    void createAssessment_ExerciseTypeNotFound() {
        AssessmentRequest request = AssessmentRequest.builder()
                .type("INVALID_TYPE")
                .testId(1)
                .title("Part 1")
                .build();

        when(exerciseTypeRepository.findByCode("INVALID_TYPE")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> assessmentService.createAssessment(request));

        assertEquals(ErrorCode.EXERCISE_TYPE_NOT_FOUND, exception.getErrorCode());
    }

    // AS_03: createAssessment - test not found
    @Test
    @DisplayName("AS_03: createAssessment - không tìm thấy test")
    void createAssessment_TestNotFound() {
        AssessmentRequest request = AssessmentRequest.builder()
                .type("LISTENING_1")
                .testId(999)
                .title("Part 1")
                .build();

        ExerciseTypeEntity exerciseType = ExerciseTypeEntity.builder()
                .id(1)
                .code("LISTENING_1")
                .description("Listening Part 1")
                .build();

        when(exerciseTypeRepository.findByCode("LISTENING_1")).thenReturn(Optional.of(exerciseType));
        when(testRepository.findById(999)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> assessmentService.createAssessment(request));

        assertEquals(ErrorCode.TEST_NOT_FOUND, exception.getErrorCode());
    }

    // AS_04: updateAssessment - success with partial update
    @Test
    @DisplayName("AS_04: updateAssessment - cập nhật thành công với partial update")
    void updateAssessment_SuccessPartialUpdate() {
        AssessmentRequest request = AssessmentRequest.builder()
                .id(1)
                .title("New Title")
                .build();

        AssessmentEntity existingEntity = AssessmentEntity.builder()
                .id(1)
                .title("Old Title")
                .build();

        when(assessmentRepository.findById(1)).thenReturn(Optional.of(existingEntity));

        assessmentService.updateAssessment(request);

        assertEquals("New Title", existingEntity.getTitle());
        verify(assessmentRepository, times(1)).save(existingEntity);
    }

    // AS_05: updateAssessment - not found
    @Test
    @DisplayName("AS_05: updateAssessment - không tìm thấy assessment")
    void updateAssessment_NotFound() {
        AssessmentRequest request = AssessmentRequest.builder()
                .id(999)
                .build();

        when(assessmentRepository.findById(999)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> assessmentService.updateAssessment(request));

        assertEquals(ErrorCode.ASSESSMENT_NOT_FOUND, exception.getErrorCode());
    }

    // AS_06: getAssessmentDetailForFistTest - success
    @Test
    @DisplayName("AS_06: getAssessmentDetailForFistTest - lấy chi tiết assessment thành công")
    void getAssessmentDetailForFistTest_Success() {
        List<AssessmentQuestionEntity> questions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            questions.add(AssessmentQuestionEntity.builder().id(i).build());
        }

        ExerciseTypeEntity exerciseType = ExerciseTypeEntity.builder()
                .id(1)
                .code("LISTENING_1")
                .description("Listening")
                .build();

        AssessmentEntity assessment = AssessmentEntity.builder()
                .id(1)
                .assessmentQuestions(questions)
                .exercisetype(exerciseType)
                .build();

        TestEntity test = TestEntity.builder()
                .id(1)
                .type("FIRST_TEST")
                .assessments(List.of(assessment))
                .build();

        AssessmentResponse mockResponse = AssessmentResponse.builder()
                .id(1)
                .title("Part 1")
                .build();

        when(testRepository.findByType("FIRST_TEST")).thenReturn(List.of(test));
        when(assessmentRepository.findByTestId(1)).thenReturn(List.of(assessment));
        when(assessmentConverter.toAssessmentDetailResponse(assessment)).thenReturn(mockResponse);

        List<AssessmentResponse> result = assessmentService.getAssessmentDetailForFistTest();

        assertFalse(result.isEmpty());
    }

    // AS_07: getAssessmentDetailForFistTest - throw when no first test
    @Test
    @DisplayName("AS_07: getAssessmentDetailForFistTest - ném exception khi không có first test")
    void getAssessmentDetailForFistTest_NoFirstTest() {
        when(testRepository.findByType("FIRST_TEST")).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assessmentService.getAssessmentDetailForFistTest());

        assertEquals("No test found", exception.getMessage());
    }
}
