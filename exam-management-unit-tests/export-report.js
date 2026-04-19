const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

async function createExcel() {
  const workbook = new ExcelJS.Workbook();
  const sheet = workbook.addWorksheet('Unit Test Cases');
  
  // Thiết lập các cột theo yêu cầu mới nhất
  sheet.columns = [
    { header: 'TestcaseID', key: 'id', width: 20 },
    { header: 'Chức năng/use case', key: 'usecase', width: 25 },
    { header: 'Lớp', key: 'class', width: 20 },
    { header: 'Phương thức', key: 'method', width: 25 },
    { header: 'Mục tiêu kiểm thử', key: 'objective', width: 45 },
    { header: 'Input (Dữ liệu mock)', key: 'input', width: 45 },
    { header: 'Expected output', key: 'expected', width: 45 },
    { header: 'Kết quả', key: 'result', width: 15 },
    { header: 'Ghi chú', key: 'notes', width: 35 }
  ];

  // STYLING HEADERS
  sheet.getRow(1).font = { bold: true };
  sheet.getRow(1).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFD3D3D3' } };

  // Helper tìm tên phương thức
  function getMethodName(id) {
    if (id.includes('_CRE_')) return 'createExam';
    if (id.includes('_GET_')) return 'getExamById';
    if (id.includes('_GALL_')) return 'getAllExams';
    if (id.includes('_UPD_')) return 'updateExam';
    if (id.includes('_DEL_')) return 'deleteExam';
    if (id.includes('_AQ_')) return 'addQuestionsToExam';
    if (id.includes('_RQ_')) return 'removeQuestionsFromExam';
    if (id.includes('_STAT_')) return 'getExamStatistics';
    if (id.includes('_SRCH_')) return 'searchExams';
    if (id.includes('_DUP_')) return 'duplicateExam';
    if (id.includes('_AMG_')) return 'addMediaGroupToExam';
    if (id.includes('_RMG_')) return 'removeMediaGroupFromExam';
    if (id.includes('_ET_')) {
      if (['TC_ES_ET_001'].includes(id)) return 'getExamTypes';
      if (['TC_ES_ET_002', 'TC_ES_ET_003'].includes(id)) return 'createExamType';
      if (['TC_ES_ET_004', 'TC_ES_ET_005', 'TC_ES_ET_006'].includes(id)) return 'updateExamType';
      if (['TC_ES_ET_007', 'TC_ES_ET_008'].includes(id)) return 'deleteExamType';
    }
    if (id.includes('_NOI_')) return 'getNextOrderIndex';
    if (id.includes('_VES_')) return 'validateExamStructure';
    if (id.includes('_CEO_')) return 'compactExamOrder';
    if (id.includes('_RPQ_')) return 'replaceQuestionInExam';
    return '';
  }

  const failedTests = ['TC_ES_CRE_007', 'TC_ES_GET_002', 'TC_ES_STAT_001'];

  const content = fs.readFileSync(path.join(__dirname, 'tests/exam.service.test.ts'), 'utf8');

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
    const result = failedTests.includes(id) ? 'Fail' : 'Pass';

    sheet.addRow({
      id,
      usecase: 'Quản lý bài test đầu vào',
      class: 'ExamService',
      method,
      objective,
      input,
      expected,
      result,
      notes
    });
    count++;
  }

  // Style the cells
  sheet.eachRow((row, rowNumber) => {
    row.eachCell((cell, colNumber) => {
      cell.border = {
        top: {style:'thin'}, left: {style:'thin'}, bottom: {style:'thin'}, right: {style:'thin'}
      };
      
      if (rowNumber > 1) {
        cell.alignment = { wrapText: true, vertical: 'top' };
        // Đổi màu cột kết quả dựa trên Pass/Fail
        if (colNumber === 8) { // Cột Kết quả
          if (cell.value === 'Fail') {
            cell.font = { color: { argb: 'FFFF0000' }, bold: true }; // Màu đỏ
          } else {
            cell.font = { color: { argb: 'FF00B050' }, bold: true }; // Màu xanh
          }
        }
      } else {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
      }
    });
  });

  await workbook.xlsx.writeFile('Unit_Testing_Report_Final.xlsx');
  console.log(`Successfully created Unit_Testing_Report_Final.xlsx with ${count} test cases.`);
}

createExcel().catch(console.error);
