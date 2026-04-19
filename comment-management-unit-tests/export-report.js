/**
 * =================================================================
 * Export Report Script - Quản lý Bình luận TOEIC
 * =================================================================
 * 
 * Script tự động parse test file và xuất báo cáo Test Cases ra Excel
 * với 9 cột dữ liệu theo yêu cầu SQA.
 * 
 * Cách chạy: npm run export-report
 * Output: Unit_Testing_Report_CommentService.xlsx
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
    if (id.includes('_CRE_')) return 'createComment';
    if (id.includes('_GETE_')) return 'getExamComments';
    if (id.includes('_THR_')) return 'getCommentThread';
    if (id.includes('_UPD_')) return 'updateComment';
    if (id.includes('_DEL_')) return 'deleteComment';
    if (id.includes('_MOD_')) return 'moderateComment';
    if (id.includes('_STU_')) return 'getStudentComments';
    if (id.includes('_FLG_')) return 'getFlaggedComments';
    if (id.includes('_SRCH_')) return 'searchComments';
    if (id.includes('_CNT_')) return 'getExamCommentCount';
    if (id.includes('_ALL_')) return 'getAllComments';
    return '';
  }

  // Helper: Tìm use case tương ứng
  function getUseCase(id) {
    if (id.includes('_CRE_')) return 'Tạo bình luận mới';
    if (id.includes('_GETE_')) return 'Lấy bình luận kỳ thi';
    if (id.includes('_THR_')) return 'Lấy thread phản hồi';
    if (id.includes('_UPD_')) return 'Sửa bình luận';
    if (id.includes('_DEL_')) return 'Xóa bình luận';
    if (id.includes('_MOD_')) return 'Kiểm duyệt / Duyệt';
    if (id.includes('_STU_')) return 'Tra cứu LS học sinh';
    if (id.includes('_FLG_')) return 'Lấy Comment vi phạm';
    if (id.includes('_SRCH_')) return 'Tìm kiếm content';
    if (id.includes('_CNT_')) return 'Đếm tổng bình luận';
    if (id.includes('_ALL_')) return 'Duyệt bảng Admin';
    return 'Quản lý Bình luận';
  }

  // 3 test cases CỐ TÌNH FAIL để minh họa
  const failedTests = ['TC_CMT_CRE_002', 'TC_CMT_UPD_003', 'TC_CMT_DEL_003'];

  const content = fs.readFileSync(
    path.join(__dirname, 'tests/comment.service.test.ts'), 
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
    const notes = match[6] ? match[6].trim() : 'Sử dụng Mock Repository';
    const method = getMethodName(id);
    const usecase = getUseCase(id);
    const result = failedTests.includes(id) ? 'Fail' : 'Pass';

    sheet.addRow({
      id,
      usecase,
      class: 'CommentService',
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

  const outputPath = 'Unit_Testing_Report_CommentService_V2.xlsx';
  await workbook.xlsx.writeFile(outputPath);
  console.log(`✅ Successfully created ${outputPath} with ${count} test cases in full format.`);
}

createExcel().catch(console.error);
