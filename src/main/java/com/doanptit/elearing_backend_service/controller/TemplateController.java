package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.enums.ExamType;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
public class TemplateController {

    @GetMapping(value = "/exam.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> downloadExamTemplate(
            @RequestParam(defaultValue = "QUIZ") ExamType examType
    ) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Template");

            int r = 0;
            r = writeMetaRow(sheet, r, "EXAM_TYPE", examType.name());
            r = writeMetaRow(sheet, r, "TITLE", examType == ExamType.ASSIGNMENT ? "Bài tập mẫu" : "Bài kiểm tra mẫu");
            r = writeMetaRow(sheet, r, "DESCRIPTION", "Mô tả ngắn cho bài " + (examType == ExamType.ASSIGNMENT ? "tập" : "kiểm tra"));
            r = writeMetaRow(sheet, r, "TIME_LIMIT_MINUTES", "30");
            r = writeMetaRow(sheet, r, "MAX_ATTEMPTS", "2");

            r += 1;
            Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("QUESTION_CONTENT");
            header.createCell(1).setCellValue("QUESTION_TYPE"); // SINGLE_CHOICE / MULTIPLE_CHOICE
            header.createCell(2).setCellValue("POINT");
            header.createCell(3).setCellValue("ANSWER_A");
            header.createCell(4).setCellValue("ANSWER_B");
            header.createCell(5).setCellValue("ANSWER_C");
            header.createCell(6).setCellValue("ANSWER_D");
            header.createCell(7).setCellValue("CORRECT"); // e.g. A or A;C

            Row sample1 = sheet.createRow(r++);
            sample1.createCell(0).setCellValue("S3 là dịch vụ gì?");
            sample1.createCell(1).setCellValue("SINGLE_CHOICE");
            sample1.createCell(2).setCellValue(1);
            sample1.createCell(3).setCellValue("Cơ sở dữ liệu");
            sample1.createCell(4).setCellValue("Lưu trữ đối tượng");
            sample1.createCell(5).setCellValue("CDN");
            sample1.createCell(6).setCellValue("Compute");
            sample1.createCell(7).setCellValue("B");

            Row sample2 = sheet.createRow(r++);
            sample2.createCell(0).setCellValue("Dịch vụ nào chạy serverless function?");
            sample2.createCell(1).setCellValue("SINGLE_CHOICE");
            sample2.createCell(2).setCellValue(1);
            sample2.createCell(3).setCellValue("Lambda");
            sample2.createCell(4).setCellValue("EC2");
            sample2.createCell(5).setCellValue("RDS");
            sample2.createCell(6).setCellValue("S3");
            sample2.createCell(7).setCellValue("A");

            for (int c = 0; c <= 7; c++) {
                sheet.autoSizeColumn(c);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);

            String filename = examType == ExamType.ASSIGNMENT ? "exam_template_assignment.xlsx" : "exam_template_quiz.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(baos.toByteArray());
        }
    }

    private int writeMetaRow(Sheet sheet, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return rowIdx + 1;
    }
}


