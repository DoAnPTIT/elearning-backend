package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.CreateAnswerRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateQuestionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.ExamResponse;
import com.doanptit.elearing_backend_service.enums.ExamType;
import com.doanptit.elearing_backend_service.enums.QuestionType;
import com.doanptit.elearing_backend_service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
public class TeacherExamImportController {
    private final CourseService courseService;

    @PostMapping(value = "/sections/{sectionId}/exams/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExamResponse>> importExamFromExcel(
            @PathVariable Long sectionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("examType") ExamType examType,
            Authentication authentication
    ) throws Exception {
        CreateExamRequestDto dto = parseTemplateExcel(file, examType);
        ExamResponse created = courseService.createExam(sectionId, dto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    private CreateExamRequestDto parseTemplateExcel(MultipartFile file, ExamType examType) throws Exception {
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            Map<String, String> meta = new HashMap<>();
            int headerRowIdx = -1;
            for (int i = 0; i < Math.min(50, sheet.getLastRowNum() + 1); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String c0 = fmt.formatCellValue(row.getCell(0)).trim();
                if ("QUESTION_CONTENT".equalsIgnoreCase(c0)) {
                    headerRowIdx = i;
                    break;
                }
                String c1 = fmt.formatCellValue(row.getCell(1)).trim();
                if (!c0.isEmpty() && !c1.isEmpty()) {
                    meta.put(c0.toUpperCase(), c1);
                }
            }
            if (headerRowIdx < 0) {
                throw new IllegalArgumentException("Không tìm thấy dòng header QUESTION_CONTENT trong file template.");
            }

            CreateExamRequestDto dto = new CreateExamRequestDto();
            dto.setExamType(examType);
            dto.setTitle(Optional.ofNullable(meta.get("TITLE")).orElse(examType == ExamType.ASSIGNMENT ? "Bài tập" : "Bài kiểm tra"));
            dto.setDescription(Optional.ofNullable(meta.get("DESCRIPTION")).orElse(""));
            dto.setTimeLimitMinutes(parseInt(meta.get("TIME_LIMIT_MINUTES"), null));
            dto.setMaxAttempts(parseInt(meta.get("MAX_ATTEMPTS"), null));

            List<CreateQuestionRequestDto> questions = new ArrayList<>();
            for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String qContent = fmt.formatCellValue(row.getCell(0)).trim();
                if (qContent.isEmpty()) continue;

                String qTypeRaw = fmt.formatCellValue(row.getCell(1)).trim().toUpperCase();
                QuestionType qType = "MULTIPLE_CHOICE".equals(qTypeRaw) ? QuestionType.MULTIPLE_CHOICE : QuestionType.SINGLE_CHOICE;
                Integer point = parseInt(fmt.formatCellValue(row.getCell(2)).trim(), 1);

                String a = fmt.formatCellValue(row.getCell(3)).trim();
                String b = fmt.formatCellValue(row.getCell(4)).trim();
                String c = fmt.formatCellValue(row.getCell(5)).trim();
                String d = fmt.formatCellValue(row.getCell(6)).trim();
                String correctRaw = fmt.formatCellValue(row.getCell(7)).trim().toUpperCase();
                Set<String> correctSet = new HashSet<>();
                if (!correctRaw.isEmpty()) {
                    for (String part : correctRaw.split("[;,\\s]+")) {
                        String p = part.trim();
                        if (!p.isEmpty()) correctSet.add(p);
                    }
                }

                List<CreateAnswerRequestDto> answers = new ArrayList<>();
                addAnswer(answers, "A", a, correctSet);
                addAnswer(answers, "B", b, correctSet);
                addAnswer(answers, "C", c, correctSet);
                addAnswer(answers, "D", d, correctSet);

                CreateQuestionRequestDto q = new CreateQuestionRequestDto();
                q.setContent(qContent);
                q.setQuestionType(qType);
                q.setPoint(point);
                q.setAnswers(answers);
                questions.add(q);
            }

            dto.setQuestions(questions);
            return dto;
        }
    }

    private void addAnswer(List<CreateAnswerRequestDto> answers, String letter, String content, Set<String> correctSet) {
        if (content == null || content.isBlank()) return;
        CreateAnswerRequestDto a = new CreateAnswerRequestDto();
        a.setContent(content);
        a.setIsCorrect(correctSet.contains(letter));
        answers.add(a);
    }

    private Integer parseInt(String raw, Integer fallback) {
        if (raw == null) return fallback;
        String t = raw.trim();
        if (t.isEmpty()) return fallback;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}


