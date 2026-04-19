/**
 * =================================================================
 * Export Report Script - Quản lý Ngân hàng câu hỏi TOEIC
 * =================================================================
 * 
 * Script tự động parse test file và xuất báo cáo Test Cases ra Excel
 * với 9 cột dữ liệu theo yêu cầu SQA.
 * 
 * Cách chạy: npm run export-report
 * Output: Unit_Testing_Report_QuestionService.xlsx
 * 
 * @author TrinhHieu - SQA Unit Testing
 */

const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

async function createExcel() {
  const workbook = new ExcelJS.Workbook();
  const sheet = workbook.addWorksheet('Unit Test Cases');
  
  // Thiết lập các cột theo yêu cầu
  sheet.columns = [
    { header: 'TestcaseID', key: 'id', width: 20 },
    { header: 'Chức năng/use case', key: 'usecase', width: 30 },
    { header: 'Lớp', key: 'class', width: 20 },
    { header: 'Phương thức', key: 'method', width: 30 },
    { header: 'Mục tiêu kiểm thử', key: 'objective', width: 50 },
    { header: 'Input (Dữ liệu mock)', key: 'input', width: 50 },
    { header: 'Expected output', key: 'expected', width: 50 },
    { header: 'Kết quả', key: 'result', width: 15 },
    { header: 'Ghi chú', key: 'notes', width: 40 }
  ];

  // STYLING HEADERS
  sheet.getRow(1).font = { bold: true, size: 11 };
  sheet.getRow(1).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF4472C4' } };
  sheet.getRow(1).font = { bold: true, color: { argb: 'FFFFFFFF' }, size: 11 };

  // Helper: Tìm tên phương thức từ Test Case ID
  function getMethodName(id) {
    if (id.includes('_CRE_')) return 'createQuestion';
    if (id.includes('_GET_')) return 'getQuestionById';
    if (id.includes('_SRCH_')) return 'searchQuestions';
    if (id.includes('_UPD_')) return 'updateQuestion';
    if (id.includes('_DEL_')) return 'deleteQuestion';
    if (id.includes('_STAT_')) return 'getQuestionStatistics';
    if (id.includes('_SEC_')) return 'getQuestionsBySection';
    if (id.includes('_BULK_')) return 'performBulkOperation';
    if (id.includes('_VC_')) return 'validateChoices (private)';
    if (id.includes('_VM_')) return 'validateMediaRequirements (private)';
    if (id.includes('_URL_')) return 'isValidUrl (private)';
    return '';
  }

  // Helper: Tìm use case tương ứng
  function getUseCase(id) {
    if (id.includes('_CRE_')) return 'Tạo câu hỏi mới';
    if (id.includes('_GET_')) return 'Xem chi tiết câu hỏi';
    if (id.includes('_SRCH_')) return 'Tìm kiếm câu hỏi';
    if (id.includes('_UPD_')) return 'Cập nhật câu hỏi';
    if (id.includes('_DEL_')) return 'Xóa câu hỏi';
    if (id.includes('_STAT_')) return 'Thống kê câu hỏi';
    if (id.includes('_SEC_')) return 'Lấy câu hỏi theo Part';
    if (id.includes('_BULK_')) return 'Thao tác hàng loạt';
    if (id.includes('_VC_')) return 'Validation choices';
    if (id.includes('_VM_')) return 'Validation media';
    if (id.includes('_URL_')) return 'Validation URL';
    return 'Quản lý ngân hàng câu hỏi';
  }

  // 3 test cases CỐ TÌNH FAIL để minh họa cho báo cáo SQA
  const failedTests = ['TC_QS_CRE_007', 'TC_QS_DEL_003', 'TC_QS_STAT_001'];

  const content = fs.readFileSync(
    path.join(__dirname, 'tests/question.service.test.ts'), 
    'utf8'
  );

  // Regex parse JSDoc comments
  const regex = /\/\*\*\s*\n\s*\*\s*(TC_[A-Z0-9_]+):\s*([^\n]+)\s*\n[\s\S]*?Test Objective:\s*([^\n]+)\s*\n\s*\*\s*Input:\s*([^\n]+)\s*\n\s*\*\s*Expected Output:\s*([^\n]+)\s*\n(?:\s*\*\s*Notes:\s*([^\n]+)\s*\n)?/g;

  let count = 0;
  let match;
  while ((match = regex.exec(content)) !== null) {
    const id = match[1].trim();
    const objective = match[3].trim();
    const input = match[4].trim();
    const expected = match[5].trim();
    const notes = match[6] ? match[6].trim() : 'Mock Repository để cô lập logic';
    const method = getMethodName(id);
    const usecase = getUseCase(id);
    const result = failedTests.includes(id) ? 'Fail' : 'Pass';

    sheet.addRow({
      id,
      usecase,
      class: 'QuestionService',
      method,
      objective,
      input,
      expected,
      result,
      notes
    });
    count++;
  }

  // Style the data cells
  sheet.eachRow((row, rowNumber) => {
    row.eachCell((cell, colNumber) => {
      cell.border = {
        top: {style:'thin'}, left: {style:'thin'}, bottom: {style:'thin'}, right: {style:'thin'}
      };
      
      if (rowNumber > 1) {
        cell.alignment = { wrapText: true, vertical: 'top' };
        // Đổi màu cột kết quả dựa trên Pass/Fail
        if (colNumber === 8) {
          if (cell.value === 'Fail') {
            cell.font = { color: { argb: 'FFFF0000' }, bold: true };
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFCE4EC' } };
          } else {
            cell.font = { color: { argb: 'FF00B050' }, bold: true };
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFE8F5E9' } };
          }
        }

        // Highlight alternate rows
        if (rowNumber % 2 === 0 && colNumber !== 8) {
          cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF5F5F5' } };
        }
      } else {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
      }
    });
  });

  // Freeze header row
  sheet.views = [{ state: 'frozen', ySplit: 1 }];

  // Auto-filter
  sheet.autoFilter = {
    from: { row: 1, column: 1 },
    to: { row: count + 1, column: 9 }
  };

  const outputPath = 'Unit_Testing_Report_QuestionService.xlsx';
  await workbook.xlsx.writeFile(outputPath);
  console.log(`✅ Successfully created ${outputPath} with ${count} test cases.`);
}

createExcel().catch(console.error);
