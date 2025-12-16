package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.Section;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.AiService;
import com.doanptit.elearing_backend_service.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j // Thêm log để theo dõi quá trình train 100 khóa
public class AiServiceImpl implements AiService {

    private final VectorStore vectorStore;
    private final CourseRepository courseRepository;
    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;

    // 🔥 PROMPT NÂNG CẤP: "Dằn mặt" AI để không bịa đặt
    private final String BASE_SYSTEM_PROMPT = """
            Bạn là Trợ giảng AI chuyên nghiệp của hệ thống E-Learning do Minh Hiếu phát triển.
            
            NHIỆM VỤ:
            Giải đáp thắc mắc của học viên CHỈ DỰA TRÊN thông tin được cung cấp trong phần CONTEXT.
            
            QUY TẮC BẤT KHẢ XÂM PHẠM:
            1. KHÔNG được sử dụng kiến thức bên ngoài (Internet, training data cũ) để trả lời nếu Context không có.
            2. Nếu Context không chứa thông tin người dùng hỏi, hãy trả lời: "Xin lỗi, tài liệu khóa học hiện tại chưa đề cập đến vấn đề này."
            3. Trích dẫn tên [BÀI HỌC] hoặc [CHƯƠNG] chứa thông tin đó nếu có thể.
            4. Không nhắc đến OpenAI, Groq, Llama, Meta.
            5. Trả lời ngắn gọn, súc tích bằng tiếng Việt.
            """;

    public AiServiceImpl(ChatClient.Builder builder,
                         VectorStore vectorStore,
                         CourseRepository courseRepository,
                         ChatHistoryService chatHistoryService) {
        this.vectorStore = vectorStore;
        this.courseRepository = courseRepository;
        this.chatHistoryService = chatHistoryService;

        // Cấu hình Temperature thấp để AI bớt ảo giác
        this.chatClient = builder.build();
    }

    // --- 1. HÀM TRAIN TỪNG KHÓA (Cải tiến Smart Chunking) ---
    @Override
    public void ingestCourseData(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        log.info(">>>> Bắt đầu Train dữ liệu cho khóa: " + course.getTitle());
        List<Document> documents = new ArrayList<>();

        // A. Thông tin chung
        String courseInfo = """
                [LOẠI: THÔNG TIN KHÓA HỌC]
                Tên khóa: %s
                Mô tả: %s
                Mục tiêu: %s
                """.formatted(course.getTitle(), course.getDescription(), course.getObjectives());
        documents.add(new Document(courseInfo, Map.of("courseId", courseId, "type", "info")));

        // B. Nội dung bài học (Smart Chunking: Gắn nhãn Chương/Bài vào nội dung)
        if (course.getSections() != null) {
            for (Section section : course.getSections()) {
                if (section.getLessons() != null) {
                    for (Lesson l : section.getLessons()) {

                        String cleanContent = (l.getArticleContent() != null) ? l.getArticleContent() : "Video Lesson";

                        // 🔥 Kỹ thuật: Gắn Context vào từng miếng thịt (Chunk)
                        String enrichedContent = """
                                [KHÓA HỌC: %s]
                                [CHƯƠNG: %s]
                                [BÀI HỌC: %s]
                                ----------------
                                NỘI DUNG CHI TIẾT:
                                %s
                                """.formatted(course.getTitle(), section.getTitle(), l.getTitle(), cleanContent);

                        documents.add(new Document(enrichedContent, Map.of("courseId", courseId, "type", "lesson")));
                    }
                }
            }
        }

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> split = splitter.apply(documents);
        vectorStore.add(split);
        log.info(">>>> Hoàn tất Train khóa: " + course.getTitle());
    }

    // --- 2. HÀM TRAIN TOÀN BỘ (Chạy 1 lần cho 100 khóa) ---
    @Override
    public void ingestAllActiveCourses() {
        // Lấy tất cả khóa học đang ACTIVE
        List<Course> activeCourses = courseRepository.findByStatus(CourseStatus.ACTIVE);
        log.info(">>>> Tìm thấy {} khóa học ACTIVE. Đang tiến hành ingest...", activeCourses.size());

        int count = 0;
        for (Course course : activeCourses) {
            try {
                // Gọi lại hàm train lẻ ở trên
                ingestCourseData(course.getId());
                count++;
                log.info(">>>> Tiến độ: {}/{}", count, activeCourses.size());
            } catch (Exception e) {
                log.error(">>>> Lỗi khi ingest khóa ID {}: {}", course.getId(), e.getMessage());
                // Continue chạy tiếp khóa sau chứ không dừng
            }
        }
        log.info(">>>> HOÀN TẤT INGEST TOÀN BỘ HỆ THỐNG!");
    }

    // --- 3. CHAT LOGIC (Prompt Structure) ---
    @Override
    public String chatWithCourse(String message, Long courseId, String userId) {
        String conversationId = (courseId != null) ? userId + "_course_" + courseId : userId + "_global";

        // 1. Lưu User Message
        chatHistoryService.saveMessage(conversationId, "user", message);

        // 2. Search Vector
        SearchRequest searchRequest = SearchRequest.query(message).withTopK(4); // Tăng lên 4 để lấy nhiều context hơn
        if (courseId != null) {
            searchRequest = searchRequest.withFilterExpression(new FilterExpressionBuilder().eq("courseId", courseId).build());
        }
        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        String context = docs.stream().map(Document::getContent).collect(Collectors.joining("\n\n"));

        // 3. Lấy History
        List<Message> history = chatHistoryService.getRecentMessages(conversationId, 6);

        // 4. Prompt Engineering: Dùng thẻ XML giả lập để ngăn cách dữ liệu
        String modeInstruction = (courseId != null) ? "Vai trò: TRỢ GIẢNG." : "Vai trò: TƯ VẤN VIÊN.";

        String finalSystemText = """
                %s
                %s
                
                <CONTEXT_DATABSE>
                %s
                </CONTEXT_DATABSE>
                
                Hãy trả lời dựa trên thẻ <CONTEXT_DATABASE> ở trên.
                """.formatted(BASE_SYSTEM_PROMPT, modeInstruction, context);

        List<Message> messagesToSend = new ArrayList<>();
        messagesToSend.add(new SystemMessage(finalSystemText));
        messagesToSend.addAll(history);

        // 5. Call AI
        String aiResponse = chatClient.prompt().messages(messagesToSend).call().content();

        // 6. Lưu AI Response
        chatHistoryService.saveMessage(conversationId, "assistant", aiResponse);

        return aiResponse;
    }
}