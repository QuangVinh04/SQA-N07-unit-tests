package com.mxhieu.doantotnghiep.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Xuất ma trận testcase ra file Excel (.xlsx).
 * Chạy: {@code mvn -q exec:java}
 * <p>
 * Cột "Kết quả" được điền tự động nếu đã chạy {@code mvn test} trước đó
 * (đọc {@code target/surefire-reports/TEST-*.xml}).
 * Đường dẫn output mặc định: {@code docs/testcase_matrix.xlsx} (ngoài {@code target/} để {@code mvn clean}
 * không bị lỗi khi file đang mở trong Excel). Ghi đè: {@code -Dexport.path=...}
 */
public final class TestCaseExcelExporter {

    private static final String[] HEADERS = {
            "TestcaseID",
            "Chức năng/use case",
            "Lớp",
            "Phương thức",
            "Mục tiêu kiểm thử",
            "Input (Dữ liệu mock)",
            "Expected output",
            "Kết quả",
            "Ghi chú"
    };

    private record MatrixRow(
            String testcaseId,
            String useCase,
            String classSimple,
            String methodName,
            String junitClass,
            String junitMethod,
            String objective,
            String inputMock,
            String expected,
            String notes
    ) {}

    public static void main(String[] args) throws Exception {
        Path projectDir = Path.of(System.getProperty("user.dir"));
        Path out = Path.of(System.getProperty("export.path",
                projectDir.resolve("docs").resolve("testcase_matrix.xlsx").toString()));

        Map<String, String> outcomes = loadSurefireOutcomes(projectDir.resolve("target/surefire-reports"));

        List<MatrixRow> rows = buildRows();
        Files.createDirectories(out.getParent());

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Test cases");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row h = sh.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = h.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (MatrixRow row : rows) {
                Row excelRow = sh.createRow(r++);
                String key = row.junitClass() + "::" + row.junitMethod();
                String ketQua = outcomes.getOrDefault(key, "");

                excelRow.createCell(0).setCellValue(row.testcaseId());
                excelRow.createCell(1).setCellValue(row.useCase());
                excelRow.createCell(2).setCellValue(row.classSimple());
                excelRow.createCell(3).setCellValue(row.methodName());
                excelRow.createCell(4).setCellValue(row.objective());
                excelRow.createCell(5).setCellValue(row.inputMock());
                excelRow.createCell(6).setCellValue(row.expected());
                excelRow.createCell(7).setCellValue(ketQua);
                excelRow.createCell(8).setCellValue(row.notes());
            }

            for (int c = 0; c < HEADERS.length; c++) {
                sh.autoSizeColumn(c);
            }

            try (var os = Files.newOutputStream(out)) {
                wb.write(os);
            }
        }

        System.out.println("Exported: " + out.toAbsolutePath());
    }

    private static Map<String, String> loadSurefireOutcomes(Path reportsDir) {
        Map<String, String> map = new HashMap<>();
        if (!Files.isDirectory(reportsDir)) {
            return map;
        }
        try (Stream<Path> stream = Files.list(reportsDir)) {
            stream.filter(p -> p.getFileName().toString().startsWith("TEST-") && p.toString().endsWith(".xml"))
                    .forEach(p -> parseSurefireFile(p, map));
        } catch (Exception ignored) {
            // báo cáo không bắt buộc
        }
        return map;
    }

    private static void parseSurefireFile(Path xml, Map<String, String> out) {
        try (InputStream is = Files.newInputStream(xml)) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            NodeList cases = doc.getElementsByTagName("testcase");
            for (int i = 0; i < cases.getLength(); i++) {
                Element el = (Element) cases.item(i);
                String name = el.getAttribute("name");
                String classname = el.getAttribute("classname");
                boolean failed = el.getElementsByTagName("failure").getLength() > 0
                        || el.getElementsByTagName("error").getLength() > 0;
                boolean skipped = el.getElementsByTagName("skipped").getLength() > 0;
                String v = skipped ? "Skipped" : (failed ? "Fail" : "Pass");
                out.put(classname + "::" + name, v);
            }
        } catch (Exception ignored) {
            // bỏ qua file XML lỗi
        }
    }

    private static List<MatrixRow> buildRows() {
        String pkg = "com.mxhieu.doantotnghiep.service.";
        List<MatrixRow> list = new ArrayList<>();

        list.add(new MatrixRow("TS_01", "Quản lý bài test đầu vào / Mini test", "TestServiceImpl", "createTest",
                pkg + "TestServiceImplTest", "createTest_shouldSaveTest_whenModuleExists",
                "Tạo test thành công khi module tồn tại",
                "TestRequest (type=MINI_TEST, moduleId=1); moduleRepository.findById(1) → ModuleEntity; converter → TestEntity rỗng",
                "testRepository.save() 1 lần; entity.getModule() trùng module đã mock",
                ""));

        list.add(new MatrixRow("TS_02", "Quản lý bài test đầu vào / Mini test", "TestServiceImpl", "createTest",
                pkg + "TestServiceImplTest", "createTest_shouldThrowException_whenModuleNotFound",
                "Ném AppException khi module không tồn tại",
                "TestRequest moduleId=999; moduleRepository.findById(999) → empty",
                "AppException MODULE_NOT_FOUND; không gọi testRepository.save",
                ""));

        list.add(new MatrixRow("TS_03", "Quản lý bài test đầu vào", "TestServiceImpl", "createFirstTest",
                pkg + "TestServiceImplTest", "createFirstTest_shouldSaveWithoutModule",
                "Tạo FIRST_TEST không gắn module",
                "TestRequest type=FIRST_TEST; converter → TestEntity",
                "save được gọi; entity.getModule() == null",
                ""));

        list.add(new MatrixRow("TS_04", "Hiển thị sao hoàn thành", "TestServiceImpl", "commpletedStar",
                pkg + "TestServiceImplTest", "commpletedStar_shouldReturn3Stars_whenScoreIs100",
                "3 sao khi điểm cao nhất = 100",
                "testAttemptRepository.findByTestIdAndStudentProfileId(1,1) → 1 attempt totalScore=100",
                "return 3",
                ""));

        list.add(new MatrixRow("TS_05", "Hiển thị sao hoàn thành", "TestServiceImpl", "commpletedStar",
                pkg + "TestServiceImplTest", "commpletedStar_shouldReturn2Stars_whenBestScoreIs85",
                "2 sao khi max điểm trong [70, 100)",
                "2 attempt 60 và 85",
                "return 2",
                ""));

        list.add(new MatrixRow("TS_06", "Hiển thị sao hoàn thành", "TestServiceImpl", "commpletedStar",
                pkg + "TestServiceImplTest", "commpletedStar_shouldReturn1Star_whenScoreBelow70",
                "1 sao khi max < 70",
                "1 attempt totalScore=45",
                "return 1",
                ""));

        list.add(new MatrixRow("TS_07", "Hiển thị sao hoàn thành", "TestServiceImpl", "commpletedStar",
                pkg + "TestServiceImplTest", "commpletedStar_shouldReturn0_whenNoAttempts",
                "0 sao khi chưa có attempt",
                "repository trả về empty list",
                "return 0",
                ""));

        list.add(new MatrixRow("TS_08", "Điểm số bài test", "TestServiceImpl", "getMaxScore",
                pkg + "TestServiceImplTest", "getMaxScore_shouldReturnHighestScore",
                "Lấy điểm cao nhất từ nhiều lượt làm",
                "3 attempt 50, 90, 70",
                "return 90f",
                ""));

        list.add(new MatrixRow("TS_09", "Khóa bài test (isLock)", "TestServiceImpl", "isLock",
                pkg + "TestServiceImplTest", "isLock_shouldHandleNull_whenStudentNotEnrolled",
                "Học sinh chưa ghi danh: findStatus trả về null — không NPE",
                "Test có module/course; enrollmentcourseRepository.findStatus(1,1) → null",
                "Không throw (hoặc xử lý an toàn); hiện tại code có thể NPE",
                "Phát hiện bug: so sánh null.equals — cần sửa production code"));

        list.add(new MatrixRow("TS_10", "Đánh dấu hoàn thành bài test", "TestServiceImpl", "isCompletedTest",
                pkg + "TestServiceImplTest", "isCompletedTest_shouldHandleNullProcess",
                "process null trong DB — không NPE khi auto-unbox",
                "testProgressRepository → 1 bản ghi process=null",
                "Không throw; mong false hoặc xử lý null; hiện NPE",
                "Phát hiện bug: so sánh Integer null với 0"));

        // TestAttemptServiceImpl
        list.add(new MatrixRow("TA_01", "Lưu kết quả làm bài (First test)", "TestAttemptServiceImpl", "saveResultFirstTest",
                pkg + "TestAttemptServiceImplTest", "saveResultFirstTest_success",
                "Lưu kết quả, cập nhật firstLogin, gọi enrollment",
                "TestAttemptRequest 2 câu đúng/sai; mock student/test/assessment/questions",
                "firstLogin=false; saveEnrollment score 50; TestAttempt totalScore=50",
                ""));

        list.add(new MatrixRow("TA_02", "Lưu kết quả làm bài (First test)", "TestAttemptServiceImpl", "saveResultFirstTest",
                pkg + "TestAttemptServiceImplTest", "saveResultFirstTest_studentNotFound",
                "Student không tồn tại",
                "studentProfileId=999 → empty",
                "STUDENT_PROFILE_NOT_FOUND",
                ""));

        list.add(new MatrixRow("TA_03", "Lưu kết quả Mini test", "TestAttemptServiceImpl", "saveResultMiniTest",
                pkg + "TestAttemptServiceImplTest", "saveResultMiniTest_noEnrollmentCall",
                "Mini test không gọi enrollment",
                "Request hợp lệ; mock đủ repository",
                "save TestAttempt; never saveEnrollment",
                ""));

        list.add(new MatrixRow("TA_04", "Tính điểm", "TestAttemptServiceImpl", "saveResultMiniTest (tinhDiem)",
                pkg + "TestAttemptServiceImplTest", "tinhDiem_allCorrect_100",
                "Tất cả câu đúng → 100 điểm",
                "3 câu isCorrect=true",
                "totalScore=100",
                ""));

        list.add(new MatrixRow("TA_05", "Tính điểm", "TestAttemptServiceImpl", "saveResultMiniTest (tinhDiem)",
                pkg + "TestAttemptServiceImplTest", "tinhDiem_allWrong_0",
                "Tất cả sai → 0 điểm",
                "2 câu isCorrect=false",
                "totalScore=0",
                ""));

        list.add(new MatrixRow("TA_06", "Chi tiết lượt làm bài", "TestAttemptServiceImpl", "getTestAttemptDetailById",
                pkg + "TestAttemptServiceImplTest", "getTestAttemptDetailById_success",
                "Trả về response khi tồn tại",
                "id=1; converter trả mockResponse",
                "Response không null; totalScore=80",
                ""));

        list.add(new MatrixRow("TA_07", "Chi tiết lượt làm bài", "TestAttemptServiceImpl", "getTestAttemptDetailById",
                pkg + "TestAttemptServiceImplTest", "getTestAttemptDetailById_notFound",
                "Không tìm thấy attempt",
                "id=999 → empty",
                "TEST_ATTEMPT_NOT_FOUND",
                ""));

        // TestProgressServiceImpl
        list.add(new MatrixRow("TP_01", "Điều kiện hoàn thành bài test", "TestProgressServiceImpl", "checkCompletionCondition",
                pkg + "TestProgressServiceImplTest", "checkCompletionCondition_completed_whenScoreAbove50",
                "Hoàn thành khi điểm >= 50",
                "process=0; attempt top score=80; getNextLessonOrTest → LESSON_NOT_HAS_NEXT",
                "true; process lưu=2; unLockNextCourse được gọi",
                ""));

        list.add(new MatrixRow("TP_02", "Điều kiện hoàn thành bài test", "TestProgressServiceImpl", "checkCompletionCondition",
                pkg + "TestProgressServiceImplTest", "checkCompletionCondition_notCompleted_whenScoreBelow50",
                "Chưa hoàn thành khi điểm < 50",
                "attempt score=30",
                "false; process=1",
                ""));

        list.add(new MatrixRow("TP_03", "Điều kiện hoàn thành bài test", "TestProgressServiceImpl", "checkCompletionCondition",
                pkg + "TestProgressServiceImplTest", "checkCompletionCondition_throwException_whenTestNotFound",
                "Test không tồn tại",
                "testId=999 → empty",
                "TEST_NOT_FOUND",
                ""));

        list.add(new MatrixRow("TP_04", "Điều kiện hoàn thành bài test", "TestProgressServiceImpl", "checkCompletionCondition",
                pkg + "TestProgressServiceImplTest", "checkCompletionCondition_throwException_whenStudentNotFound",
                "Học sinh không tồn tại",
                "studentprofileId=999 → empty",
                "STUDENT_PROFILE_NOT_FOUND",
                ""));

        list.add(new MatrixRow("TP_05", "Điều kiện hoàn thành bài test", "TestProgressServiceImpl", "checkCompletionCondition",
                pkg + "TestProgressServiceImplTest", "checkCompletionCondition_throwException_whenNoProgressExists",
                "Không có bản ghi progress",
                "findByTest_IdAndStudentProfile_Id → empty",
                "TEST_PROGRESS_NOT_EXISTS",
                ""));

        // AssessmentServiceImpl
        list.add(new MatrixRow("AS_01", "Quản lý assessment", "AssessmentServiceImpl", "createAssessment",
                pkg + "AssessmentServiceImplTest", "createAssessment_Success",
                "Tạo assessment thành công",
                "type LISTENING_1; exerciseType + test tồn tại",
                "assessmentRepository.save; test và exerciseType gán đúng",
                ""));

        list.add(new MatrixRow("AS_02", "Quản lý assessment", "AssessmentServiceImpl", "createAssessment",
                pkg + "AssessmentServiceImplTest", "createAssessment_ExerciseTypeNotFound",
                "Exercise type không tồn tại",
                "findByCode INVALID_TYPE → empty",
                "EXERCISE_TYPE_NOT_FOUND",
                ""));

        list.add(new MatrixRow("AS_03", "Quản lý assessment", "AssessmentServiceImpl", "createAssessment",
                pkg + "AssessmentServiceImplTest", "createAssessment_TestNotFound",
                "Test không tồn tại",
                "testId=999 → empty",
                "TEST_NOT_FOUND",
                ""));

        list.add(new MatrixRow("AS_04", "Quản lý assessment", "AssessmentServiceImpl", "updateAssessment",
                pkg + "AssessmentServiceImplTest", "updateAssessment_SuccessPartialUpdate",
                "Cập nhật partial (title)",
                "request id=1 title mới; existing trong repo",
                "title cập nhật; save 1 lần",
                ""));

        list.add(new MatrixRow("AS_05", "Quản lý assessment", "AssessmentServiceImpl", "updateAssessment",
                pkg + "AssessmentServiceImplTest", "updateAssessment_NotFound",
                "Assessment không tồn tại",
                "id=999 → empty",
                "ASSESSMENT_NOT_FOUND",
                ""));

        list.add(new MatrixRow("AS_06", "Chi tiết assessment (First test)", "AssessmentServiceImpl", "getAssessmentDetailForFistTest",
                pkg + "AssessmentServiceImplTest", "getAssessmentDetailForFistTest_Success",
                "Lấy danh sách chi tiết khi có FIRST_TEST",
                "test type FIRST_TEST + assessments; converter mock",
                "Danh sách response không rỗng",
                ""));

        list.add(new MatrixRow("AS_07", "Chi tiết assessment (First test)", "AssessmentServiceImpl", "getAssessmentDetailForFistTest",
                pkg + "AssessmentServiceImplTest", "getAssessmentDetailForFistTest_NoFirstTest",
                "Không có first test",
                "findByType FIRST_TEST → empty",
                "RuntimeException message \"No test found\"",
                ""));

        return list;
    }

    private TestCaseExcelExporter() {
    }
}
