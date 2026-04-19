package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.converter.TestAttemptConverter;
import com.mxhieu.doantotnghiep.dto.request.AssessmentAnswerRequest;
import com.mxhieu.doantotnghiep.dto.request.AssessmentAttemptRequest;
import com.mxhieu.doantotnghiep.dto.request.TestAttemptRequest;
import com.mxhieu.doantotnghiep.dto.response.TestAttemptResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.impl.TestAttemptServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestAttemptServiceImplTest {

    @Mock private TestRepository testRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private TestAttemptRepository testAttemptRepository;
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock private EnrollmentServece enrollmentServece;
    @Mock private TestAttemptConverter testAttemptConverter;
    @Mock private AssessmentOptionRepository assessmentOptionRepository;
    @Mock private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private TestAttemptServiceImpl testAttemptService;

    private AssessmentQuestionEntity buildQ(int qId, int optId) {
        return AssessmentQuestionEntity.builder().id(qId)
                .assessmentOptions(List.of(
                        AssessmentOptionEntity.builder().id(optId).build()
                )).build();
    }

    // TA_01
    @Test
    @DisplayName("TA_01 - saveResultFirstTest: lưu kết quả thành công, firstLogin → false, enrollment được gọi")
    void saveResultFirstTest_success() {
        AssessmentAnswerRequest ans1 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(1).assessmentOptionId(10).isCorrect(true).build();
        AssessmentAnswerRequest ans2 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(2).assessmentOptionId(20).isCorrect(false).build();

        AssessmentAttemptRequest attemptReq = AssessmentAttemptRequest.builder()
                .assessmentId(10)
                .assessmentAnswerRequests(List.of(ans1, ans2))
                .build();

        TestAttemptRequest request = TestAttemptRequest.builder()
                .testId(1)
                .studentProfileId(1)
                .assessmentAttemptRequests(List.of(attemptReq))
                .build();

        StudentProfileEntity student = StudentProfileEntity.builder().id(1).firstLogin(true).build();
        TestEntity testEntity = TestEntity.builder().id(1).build();
        AssessmentEntity assessment = AssessmentEntity.builder().id(10).build();

        AssessmentQuestionEntity q1 = buildQ(1, 10);
        AssessmentQuestionEntity q2 = buildQ(2, 20);

        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(testRepository.findById(1)).thenReturn(Optional.of(testEntity));
        when(assessmentRepository.findById(10)).thenReturn(Optional.of(assessment));
        when(assessmentQuestionRepository.findById(1)).thenReturn(Optional.of(q1));
        when(assessmentQuestionRepository.findById(2)).thenReturn(Optional.of(q2));

        testAttemptService.saveResultFirstTest(request);

        assertFalse(student.getFirstLogin());
        verify(studentProfileRepository).save(student);
        verify(enrollmentServece).saveEnrollment(argThat(e -> e.getScore() == 50.0f));

        ArgumentCaptor<TestAttemptEntity> captor = ArgumentCaptor.forClass(TestAttemptEntity.class);
        verify(testAttemptRepository).save(captor.capture());
        assertEquals(50.0f, captor.getValue().getTotalScore());
    }

    // TA_02
    @Test
    @DisplayName("TA_02 - saveResultFirstTest: student không tồn tại → STUDENT_PROFILE_NOT_FOUND")
    void saveResultFirstTest_studentNotFound() {
        TestAttemptRequest request = TestAttemptRequest.builder()
                .testId(1)
                .studentProfileId(999)
                .assessmentAttemptRequests(List.of())
                .build();

        when(studentProfileRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> testAttemptService.saveResultFirstTest(request));
        assertEquals(ErrorCode.STUDENT_PROFILE_NOT_FOUND, ex.getErrorCode());
    }

    // TA_03
    @Test
    @DisplayName("TA_03 - saveResultMiniTest: không gọi enrollmentServece.saveEnrollment")
    void saveResultMiniTest_noEnrollmentCall() {
        AssessmentAnswerRequest ans = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(1).assessmentOptionId(10).isCorrect(true).build();

        AssessmentAttemptRequest attemptReq = AssessmentAttemptRequest.builder()
                .assessmentId(10)
                .assessmentAnswerRequests(List.of(ans))
                .build();

        TestAttemptRequest request = TestAttemptRequest.builder()
                .testId(1)
                .studentProfileId(1)
                .assessmentAttemptRequests(List.of(attemptReq))
                .build();

        TestEntity testEntity = TestEntity.builder().id(1).build();
        StudentProfileEntity student = StudentProfileEntity.builder().id(1).build();
        AssessmentEntity assessment = AssessmentEntity.builder().id(10).build();
        AssessmentQuestionEntity q1 = buildQ(1, 10);

        when(testRepository.findById(1)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(assessmentRepository.findById(10)).thenReturn(Optional.of(assessment));
        when(assessmentQuestionRepository.findById(1)).thenReturn(Optional.of(q1));

        testAttemptService.saveResultMiniTest(request);

        verify(testAttemptRepository).save(any(TestAttemptEntity.class));
        verify(enrollmentServece, never()).saveEnrollment(any());
    }

    // TA_04
    @Test
    @DisplayName("TA_04 - tinhDiem: tất cả đúng → 100 điểm")
    void tinhDiem_allCorrect_100() {
        AssessmentAnswerRequest a1 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(1).assessmentOptionId(10).isCorrect(true).build();
        AssessmentAnswerRequest a2 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(2).assessmentOptionId(20).isCorrect(true).build();
        AssessmentAnswerRequest a3 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(3).assessmentOptionId(30).isCorrect(true).build();

        AssessmentAttemptRequest attemptReq = AssessmentAttemptRequest.builder()
                .assessmentId(10)
                .assessmentAnswerRequests(List.of(a1, a2, a3))
                .build();

        TestAttemptRequest request = TestAttemptRequest.builder()
                .testId(1)
                .studentProfileId(1)
                .assessmentAttemptRequests(List.of(attemptReq))
                .build();

        TestEntity testEntity = TestEntity.builder().id(1).build();
        StudentProfileEntity student = StudentProfileEntity.builder().id(1).build();
        AssessmentEntity assessment = AssessmentEntity.builder().id(10).build();

        when(testRepository.findById(1)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(assessmentRepository.findById(10)).thenReturn(Optional.of(assessment));
        when(assessmentQuestionRepository.findById(1)).thenReturn(Optional.of(buildQ(1, 10)));
        when(assessmentQuestionRepository.findById(2)).thenReturn(Optional.of(buildQ(2, 20)));
        when(assessmentQuestionRepository.findById(3)).thenReturn(Optional.of(buildQ(3, 30)));

        testAttemptService.saveResultMiniTest(request);

        ArgumentCaptor<TestAttemptEntity> captor = ArgumentCaptor.forClass(TestAttemptEntity.class);
        verify(testAttemptRepository).save(captor.capture());
        assertEquals(100.0f, captor.getValue().getTotalScore());
    }

    // TA_05
    @Test
    @DisplayName("TA_05 - tinhDiem: tất cả sai → 0 điểm")
    void tinhDiem_allWrong_0() {
        AssessmentAnswerRequest a1 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(1).assessmentOptionId(10).isCorrect(false).build();
        AssessmentAnswerRequest a2 = AssessmentAnswerRequest.builder()
                .assessmentQuestionId(2).assessmentOptionId(20).isCorrect(false).build();

        AssessmentAttemptRequest attemptReq = AssessmentAttemptRequest.builder()
                .assessmentId(10)
                .assessmentAnswerRequests(List.of(a1, a2))
                .build();

        TestAttemptRequest request = TestAttemptRequest.builder()
                .testId(1)
                .studentProfileId(1)
                .assessmentAttemptRequests(List.of(attemptReq))
                .build();

        TestEntity testEntity = TestEntity.builder().id(1).build();
        StudentProfileEntity student = StudentProfileEntity.builder().id(1).build();
        AssessmentEntity assessment = AssessmentEntity.builder().id(10).build();

        when(testRepository.findById(1)).thenReturn(Optional.of(testEntity));
        when(studentProfileRepository.findById(1)).thenReturn(Optional.of(student));
        when(assessmentRepository.findById(10)).thenReturn(Optional.of(assessment));
        when(assessmentQuestionRepository.findById(1)).thenReturn(Optional.of(buildQ(1, 10)));
        when(assessmentQuestionRepository.findById(2)).thenReturn(Optional.of(buildQ(2, 20)));

        testAttemptService.saveResultMiniTest(request);

        ArgumentCaptor<TestAttemptEntity> captor = ArgumentCaptor.forClass(TestAttemptEntity.class);
        verify(testAttemptRepository).save(captor.capture());
        assertEquals(0.0f, captor.getValue().getTotalScore());
    }

    // TA_06
    @Test
    @DisplayName("TA_06 - getTestAttemptDetailById: trả về response thành công")
    void getTestAttemptDetailById_success() {
        TestAttemptEntity entity = TestAttemptEntity.builder()
                .id(1)
                .totalScore(80f)
                .assessmentAttempts(new ArrayList<>())
                .build();

        TestAttemptResponse mockResponse = TestAttemptResponse.builder()
                .id(1)
                .totalScore(80f)
                .build();

        when(testAttemptRepository.findById(1)).thenReturn(Optional.of(entity));
        when(testAttemptConverter.toResponseSummery(entity)).thenReturn(mockResponse);

        TestAttemptResponse result = testAttemptService.getTestAttemptDetailById(1);

        assertNotNull(result);
        assertEquals(80f, result.getTotalScore());
    }

    // TA_07
    @Test
    @DisplayName("TA_07 - getTestAttemptDetailById: không tìm thấy → TEST_ATTEMPT_NOT_FOUND")
    void getTestAttemptDetailById_notFound() {
        when(testAttemptRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> testAttemptService.getTestAttemptDetailById(999));
        assertEquals(ErrorCode.TEST_ATTEMPT_NOT_FOUND, ex.getErrorCode());
    }
}
