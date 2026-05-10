const fs = require("fs");
const path = require("path");
const ExcelJS = require("exceljs");

const ROOT = __dirname;
const JEST_JSON = path.join(ROOT, "tmp-jest-results.json");
const OUTPUT_XLSX = path.join(ROOT, "tests", "TestResult_Merged.xlsx");

const DETAIL = {
  TC_ES_UPD_006: {
    goal: "Validate Title không rỗng khi update",
    input: 'updateData:{Title:""}',
    expected: 'Throw "Exam title cannot be empty"',
  },
  TC_ES_UPD_007: {
    goal: "Validate Title không chỉ whitespace",
    input: 'updateData:{Title:"   "}',
    expected: 'Throw "Exam title cannot be empty"',
  },
  TC_ES_AQ_006: {
    goal: "Chặn payload questions rỗng",
    input: "questions:[]",
    expected: 'Throw "Questions payload cannot be empty"',
  },
  TC_ES_AQ_007: {
    goal: "Chặn duplicate QuestionID trong request addQuestions",
    input: "questions:[{QuestionID:1,OrderIndex:1},{QuestionID:1,OrderIndex:2}]",
    expected: 'Throw "Duplicate QuestionID in request payload"',
  },
  TC_ES_AQ_008: {
    goal: "Chặn duplicate OrderIndex trong request addQuestions",
    input: "questions:[{QuestionID:1,OrderIndex:10},{QuestionID:2,OrderIndex:10}]",
    expected: 'Throw "Duplicate OrderIndex in request payload"',
  },
  TC_ES_RQ_004: {
    goal: "Chặn questionIds rỗng khi remove",
    input: "questionIds:[]",
    expected: 'Throw "Question IDs cannot be empty"',
  },
  TC_ES_MMG_002: {
    goal: "Validate newStartOrderIndex > 0",
    input: "newStartOrderIndex=0",
    expected: 'Throw "newStartOrderIndex must be greater than 0"',
  },
  TC_ES_CRE_013: {
    goal: "Chặn duplicate QuestionID trong payload",
    input: "questions:[{QuestionID:1,OrderIndex:1},{QuestionID:1,OrderIndex:2}]",
    expected: 'Throw "Duplicate QuestionID in request payload"',
  },
  TC_ES_CRE_014: {
    goal: "Chặn duplicate OrderIndex trong payload",
    input: "questions:[{QuestionID:1,OrderIndex:1},{QuestionID:2,OrderIndex:1}]",
    expected: 'Throw "Duplicate OrderIndex in request payload"',
  },
  TC_ES_CRE_015: {
    goal: "Validate QuestionID > 0",
    input: "questions:[{QuestionID:0,OrderIndex:1}]",
    expected: 'Throw "QuestionID must be a positive integer"',
  },
  TC_ES_CRE_016: {
    goal: "Validate MediaQuestionID > 0",
    input: "MediaQuestionIDs:[0]",
    expected: 'Throw "MediaQuestionID must be a positive integer"',
  },
  TC_ES_CRE_017: {
    goal: "Validate userId > 0",
    input: "userId=0",
    expected: 'Throw "UserID must be a positive integer"',
  },
  TC_ES_GET_004: {
    goal: "Fail graceful khi thiếu examType relation",
    input: "exam.examType=null",
    expected: 'Throw "Exam type data is missing"',
  },
  TC_ES_GET_005: {
    goal: "Fail graceful khi question thiếu media relation",
    input: "question.mediaQuestion=null",
    expected: 'Throw "Question media data is missing"',
  },
  TC_ES_GCO_002: {
    goal: "Fail graceful khi standalone question thiếu media",
    input: "standalone.question.mediaQuestion=null",
    expected: 'Throw "Standalone question media data is missing"',
  },
  TC_ES_RMG_005: {
    goal: "Validate mediaQuestionId > 0",
    input: "mediaQuestionId=0",
    expected: 'Throw "MediaQuestionID must be a positive integer"',
  },
};

function parseTitle(title) {
  if (title.startsWith("TC_") && title.includes(":")) {
    const idx = title.indexOf(":");
    return {
      id: title.slice(0, idx).trim(),
      useCase: title.slice(idx + 1).trim(),
    };
  }
  return { id: "", useCase: title.trim() };
}

function parseMethod(ancestorTitles = []) {
  if (ancestorTitles.length >= 2) {
    return ancestorTitles[1].split(" - ")[0].trim();
  }
  return "";
}

function methodFromId(id, title = "") {
  if (!id) return "";
  const byPrefix = [
    ["_CRE_", "createExam"],
    ["_GET_", "getExamById"],
    ["_GALL_", "getAllExams"],
    ["_UPD_", "updateExam"],
    ["_DEL_", "deleteExam"],
    ["_AQ_", "addQuestionsToExam"],
    ["_RQ_", "removeQuestionsFromExam"],
    ["_STAT_", "getExamStatistics"],
    ["_SRCH_", "searchExams"],
    ["_DUP_", "duplicateExam"],
    ["_AMG_", "addMediaGroupToExam"],
    ["_RMG_", "removeMediaGroupFromExam"],
    ["_ET_", "ExamType CRUD"],
    ["_NOI_", "getNextOrderIndex"],
    ["_VES_", "validateExamStructure"],
    ["_CEO_", "compactExamOrder"],
    ["_RPQ_", "replaceQuestionInExam"],
    ["_MMG_", "moveMediaGroupInExam"],
    ["_MGS_", "getExamMediaGroupSummary"],
    ["_GCO_", "getExamContentOrganized"],
  ];
  for (const [token, method] of byPrefix) {
    if (id.includes(token)) return method;
  }
  // BUG cases: map by title keyword
  const t = title.toLowerCase();
  if (t.includes("movemediagroupinexam")) return "moveMediaGroupInExam";
  if (t.includes("getexammediagroupsummary")) return "getExamMediaGroupSummary";
  if (t.includes("getexamcontentorganized")) return "getExamContentOrganized";
  if (t.includes("updatexam") || t.includes("updateexam")) return "updateExam";
  if (t.includes("addquestionstoexam")) return "addQuestionsToExam";
  if (t.includes("removequestionsfromexam")) return "removeQuestionsFromExam";
  return "";
}

function functionGroup(method) {
  const m = method.toLowerCase();
  if (m.includes("createexam")) return "Tạo bài test";
  if (m.includes("getexambyid")) return "Lấy thông tin bài test";
  if (m.includes("getallexams")) return "Danh sách bài test";
  if (m.includes("updateexam")) return "Cập nhật bài test";
  if (m.includes("deleteexam")) return "Xóa bài test";
  if (m.includes("addquestionstoexam")) return "Thêm câu hỏi vào bài test";
  if (m.includes("removequestionsfromexam")) return "Xóa câu hỏi khỏi bài test";
  if (m.includes("getexamstatistics")) return "Thống kê bài test";
  if (m.includes("searchexams")) return "Tìm kiếm bài test";
  if (m.includes("duplicateexam")) return "Nhân bản bài test";
  if (m.includes("addmediagrouptoexam")) return "Thêm media group";
  if (m.includes("removemediagroupfromexam")) return "Xóa media group";
  if (m.includes("getnextorderindex")) return "Lấy thứ tự câu hỏi";
  if (m.includes("validateexamstructure")) return "Validate cấu trúc đề";
  if (m.includes("compactexamorder")) return "Nén thứ tự câu hỏi";
  if (m.includes("replacequestioninexam")) return "Thay thế câu hỏi";
  if (m.includes("movemediagroupinexam")) return "Di chuyển media group";
  if (m.includes("getexammediagroupsummary")) return "Tổng hợp media group";
  if (m.includes("getexamcontentorganized")) return "Nội dung đề theo nhóm";
  if (m.includes("examtype")) return "Quản lý loại đề";
  return "Quản lý bài test đầu vào";
}

function shortFailure(msg) {
  const clean = (msg || "").replace(/\r?\n/g, " ").trim();
  return clean.length > 500 ? `${clean.slice(0, 500)}...` : clean;
}

async function run() {
  if (!fs.existsSync(JEST_JSON)) {
    throw new Error(
      `Không tìm thấy ${JEST_JSON}. Hãy chạy: npx jest tests/exam.service.test.ts --runInBand --json --outputFile tmp-jest-results.json`
    );
  }

  // Parse JSDoc
  const tsContent = fs.readFileSync(path.join(ROOT, "tests", "exam.service.test.ts"), "utf-8");
  const lines = tsContent.split('\n');
  const jsDocMap = {};
  let i = 0;
  while (i < lines.length) {
      if (lines[i].includes('/**') && lines[i+1] && lines[i+1].includes('TC_ES_')) {
          let docblock = '';
          let j = i;
          while (j < lines.length && !lines[j].includes('*/')) {
              docblock += lines[j] + '\n';
              j++;
          }
          docblock += lines[j] + '\n';
          
          const tcMatch = docblock.match(/\*\s*(TC_ES_[A-Z0-9_]+):/);
          const objMatch = docblock.match(/\*\s*Test Objective:\s*(.*)/);
          const inputMatch = docblock.match(/\*\s*Input:\s*(.*)/);
          const outputMatch = docblock.match(/\*\s*Expected Output:\s*(.*)/);
          const notesMatch = docblock.match(/\*\s*Notes:\s*(.*)/);

          if (tcMatch) {
              jsDocMap[tcMatch[1].trim()] = {
                  objective: objMatch ? objMatch[1].trim() : '',
                  input: inputMatch ? inputMatch[1].trim() : '',
                  expected: outputMatch ? outputMatch[1].trim() : '',
                  notes: notesMatch ? notesMatch[1].trim() : ''
              };
          }
          i = j;
      }
      i++;
  }

  const jestData = JSON.parse(fs.readFileSync(JEST_JSON, "utf8"));

  const workbook = new ExcelJS.Workbook();
  const allRows = [];

  for (const suite of jestData.testResults || []) {
    for (const a of suite.assertionResults || []) {
      const { id, useCase } = parseTitle(a.title || "");
      const method =
        methodFromId(id, useCase) || parseMethod(a.ancestorTitles || "");
      const usecaseGroup = functionGroup(method);
      const status = a.status === "passed" ? "PASS" : "FAIL";
      const detail = DETAIL[id] || {};
      const jsDoc = jsDocMap[id] || {};
      
      const goal = jsDoc.objective || detail.goal || useCase;
      const input = jsDoc.input || detail.input || "Theo mock setup trong testcase";
      const expected = jsDoc.expected || detail.expected || "Theo assertion trong testcase";
      
      const failMsg = status === "FAIL" ? shortFailure((a.failureMessages || [])[0] || "") : "";
      const noteArr = [];
      if (jsDoc.notes) noteArr.push(jsDoc.notes);
      if (failMsg) noteArr.push(`Lỗi: ${failMsg}`);
      const note = noteArr.join(" | ");

      const row = {
        id,
        usecase: usecaseGroup,
        className: "ExamService",
        method,
        goal,
        input,
        expected,
        result: status,
        note,
      };

      allRows.push(row);
    }
  }

  // Gom nhóm các dòng theo phương thức (method)
  allRows.sort((a, b) => (a.method || "").localeCompare(b.method || ""));

  const sheet = workbook.addWorksheet("TestResults");
  sheet.columns = [
    { header: "TestcaseID", key: "id", width: 20 },
    { header: "Chức năng/use case", key: "usecase", width: 42 },
    { header: "Lớp", key: "className", width: 16 },
    { header: "Phương thức", key: "method", width: 24 },
    { header: "Mục tiêu kiểm thử", key: "goal", width: 42 },
    { header: "Input (Dữ liệu mock)", key: "input", width: 42 },
    { header: "Expected output", key: "expected", width: 40 },
    { header: "Kết quả", key: "result", width: 12 },
    { header: "Ghi chú", key: "note", width: 80 },
  ];
  
  sheet.addRows(allRows);

  sheet.getRow(1).font = { bold: true };
  sheet.getRow(1).alignment = { vertical: "middle", horizontal: "center" };
  sheet.views = [{ state: "frozen", ySplit: 1 }];

  sheet.eachRow((row, rowNumber) => {
    row.eachCell((cell, colNumber) => {
      cell.alignment = { wrapText: true, vertical: "top" };
      cell.border = {
        top: { style: "thin" },
        left: { style: "thin" },
        bottom: { style: "thin" },
        right: { style: "thin" },
      };
      if (rowNumber > 1 && colNumber === 8) {
        if (cell.value === "FAIL") {
          cell.font = { color: { argb: "FFFF0000" }, bold: true };
        } else {
          cell.font = { color: { argb: "FF008000" }, bold: true };
        }
      }
    });
  });

  let outPath = OUTPUT_XLSX;
  try {
    await workbook.xlsx.writeFile(outPath);
  } catch (err) {
    if (err && err.code === "EBUSY") {
      outPath = path.join(ROOT, "tests", "TestResult_Merged_v2.xlsx");
      await workbook.xlsx.writeFile(outPath);
      console.log("File đích đang mở trong Excel, đã xuất sang file _v2.");
    } else {
      throw err;
    }
  }
  console.log(`Exported ${allRows.length} test rows into 1 sheet to: ${outPath}`);
}

run().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
