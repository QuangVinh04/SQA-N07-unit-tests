package com.mxhieu.doantotnghiep.service.impl;

import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;
import com.mxhieu.doantotnghiep.dto.response.LessonResponse;
import com.mxhieu.doantotnghiep.entity.*;
import com.mxhieu.doantotnghiep.exception.AppException;
import com.mxhieu.doantotnghiep.exception.ErrorCode;
import com.mxhieu.doantotnghiep.repository.*;
import com.mxhieu.doantotnghiep.service.LessonService;
import com.mxhieu.doantotnghiep.utils.ModuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Phiên bản LessonServiceImpl dành cho project unit test.
 * Chỉ bao gồm các method cần kiểm thử, loại bỏ các phụ thuộc không liên quan
 * (LessonConverter, MaterialConverter, MediaAssetConverter, v.v.)
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final EnrollmentCourseRepository enrollmentcourseRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TestRepository testRepository;

    /**
     * Kiểm tra học viên đã hoàn thành bài học chưa.
     * Process = 2 → đã hoàn thành (DONE).
     */
    @Override
    public Boolean isCompletedLesson(Integer lessonId, Integer studentProfileId) {
        List<LessonProgressEntity> lessonProgressEntities =
                lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(lessonId, studentProfileId);
        if (lessonProgressEntities.size() == 0) {
            return false;
        } else if (lessonProgressEntities.get(0).getProcess() == 0
                || lessonProgressEntities.get(0).getProcess() == 1) {
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra bài học có đang bị khóa với học viên không.
     * LOCK  → course bị lock.
     * DONE  → không khóa (đã hoàn thành cả course).
     * Nếu không có progress record → bị khóa (chưa được unlock).
     */
    @Override
    public Boolean isLockLesson(Integer lessonId, Integer studentProfileId) {
        LessonEntity lessonEntity = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        String statusOfCourse = enrollmentcourseRepository.findStatus(
                studentProfileId, lessonEntity.getModule().getCourse().getId());
        List<LessonProgressEntity> lessonProgressEntities =
                lessonProgressRepository.findByLesson_IdAndStudentProfile_Id(
                        lessonEntity.getId(), studentProfileId);
        if ("LOCK".equals(statusOfCourse)) {
            return true;
        } else if ("DONE".equals(statusOfCourse)) {
            return false;
        } else {
            return lessonProgressEntities.isEmpty();
        }
    }

    /**
     * Tính số sao hoàn thành bài học (0-3).
     * Nếu không có exercise và đã hoàn thành → 3 sao.
     * Dựa vào % điểm: 0→0 sao, <50→1 sao, 50-79→2 sao, >=80→3 sao.
     */
    @Override
    public int completedStar(Integer lessonId, Integer userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        if (lesson.getExercises().isEmpty() && isCompletedLesson(lessonId, userId)) {
            return 3;
        }
        int tongDiemDatDuoc = lessonRepository.totalScroreOfLesson(lessonId, userId);
        int diemToiDa = exerciseRepository.countByLessonId(lessonId) * 100;
        float phanTramDiem = (float) tongDiemDatDuoc / diemToiDa * 100;
        if (phanTramDiem == 0 || tongDiemDatDuoc == 0) {
            return 0;
        } else if (phanTramDiem < 50) {
            return 1;
        } else if (phanTramDiem >= 50 && phanTramDiem < 80) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Lấy lesson/test tiếp theo trong khóa học, sắp xếp theo orderIndex của module và item.
     * Ném AppException(LESSON_NOT_HAS_NEXT) nếu đang ở bài cuối.
     */
    @Override
    public LessonOrTestAroundResponse getNextLessonOrTest(LessonOrTestAroundRequest request) {
        CourseEntity course = null;
        ItemWrapper currentWrapper = null;

        if (request.getType().trim().equals("TEST")) {
            TestEntity testEntity = testRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
            course = testEntity.getModule().getCourse();
            currentWrapper = new ItemWrapper(
                    testEntity.getModule().getOrderIndex(), 0, testEntity.getId(), "TEST");
        } else if (request.getType().trim().equals("LESSON")) {
            LessonEntity lessonEntity = lessonRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
            course = lessonEntity.getModule().getCourse();
            currentWrapper = new ItemWrapper(
                    lessonEntity.getModule().getOrderIndex(),
                    lessonEntity.getOrderIndex(),
                    lessonEntity.getId(), "LESSON");
        }

        List<ItemWrapper> items = buildItemList(course);
        items.sort(Comparator.comparing(ItemWrapper::moduleOrder).thenComparing(ItemWrapper::itemOrder));

        final ItemWrapper target = currentWrapper;
        int index = IntStream.range(0, items.size())
                .filter(i -> items.get(i).equals(target))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_HAS_NEXT));

        if (index == items.size() - 1) {
            throw new AppException(ErrorCode.LESSON_NOT_HAS_NEXT);
        }

        ItemWrapper next = items.get(index + 1);
        return LessonOrTestAroundResponse.builder().Id(next.id()).type(next.type()).build();
    }

    /**
     * Lấy lesson/test trước đó trong khóa học.
     * Ném AppException(LESSON_NOT_HAS_PREVIOUS) nếu đang ở bài đầu tiên.
     */
    @Override
    public LessonOrTestAroundResponse getPreviousLessonID(LessonOrTestAroundRequest request) {
        CourseEntity course = null;
        ItemWrapper currentWrapper = null;

        if (request.getType().equals("TEST")) {
            TestEntity testEntity = testRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
            course = testEntity.getModule().getCourse();
            currentWrapper = new ItemWrapper(
                    testEntity.getModule().getOrderIndex(), 0, testEntity.getId(), "TEST");
        } else if (request.getType().equals("LESSON")) {
            LessonEntity lessonEntity = lessonRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
            course = lessonEntity.getModule().getCourse();
            currentWrapper = new ItemWrapper(
                    lessonEntity.getModule().getOrderIndex(),
                    lessonEntity.getOrderIndex(),
                    lessonEntity.getId(), "LESSON");
        }

        List<ItemWrapper> items = buildItemList(course);
        items.sort(Comparator.comparing(ItemWrapper::moduleOrder).thenComparing(ItemWrapper::itemOrder));

        final ItemWrapper target = currentWrapper;
        int index = IntStream.range(0, items.size())
                .filter(i -> items.get(i).equals(target))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_HAS_PREVIOUS));

        if (index == 0) {
            throw new AppException(ErrorCode.LESSON_NOT_HAS_PREVIOUS);
        }

        ItemWrapper previous = items.get(index - 1);
        return LessonOrTestAroundResponse.builder().Id(previous.id()).type(previous.type()).build();
    }

    /** Helper: gom tất cả lesson và test trong khóa học thành danh sách ItemWrapper. */
    private List<ItemWrapper> buildItemList(CourseEntity course) {
        List<ItemWrapper> items = new ArrayList<>();
        for (ModuleEntity module : course.getModules()) {
            Long moduleOrder = module.getOrderIndex();
            if (module.getType() == ModuleType.LESSON) {
                module.getLessons().stream()
                        .sorted(Comparator.comparing(LessonEntity::getOrderIndex))
                        .forEach(lesson -> items.add(
                                new ItemWrapper(moduleOrder, lesson.getOrderIndex(), lesson.getId(), "LESSON")));
            }
            if (module.getType() == ModuleType.TEST && !module.getTests().isEmpty()) {
                TestEntity test = module.getTests().get(0);
                items.add(new ItemWrapper(moduleOrder, 0, test.getId(), "TEST"));
            }
        }
        return items;
    }

    record ItemWrapper(Long moduleOrder, int itemOrder, Integer id, String type) {}
}
