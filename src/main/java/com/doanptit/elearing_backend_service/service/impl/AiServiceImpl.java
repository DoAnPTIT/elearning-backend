package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.Section;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.AiService;
import com.doanptit.elearing_backend_service.service.ChatHistoryService;
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
// 1. BỎ @RequiredArgsConstructor ĐỂ TỰ VIẾT CONSTRUCTOR
public class AiServiceImpl implements AiService {

    private final VectorStore vectorStore;
    private final CourseRepository courseRepository;
    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;

    // System prompt cơ bản
    private final String BASE_SYSTEM_PROMPT = """
            Bạn là trợ lý ảo thông minh của hệ thống E-Learning do Minh Hiếu phát triển.

            QUY TẮC:
            1. Nếu được hỏi "Bạn là ai?" hãy trả lời: "Tôi là trợ lý ảo do Minh Hiếu tạo ra."
            2. Không bao giờ nhắc đến OpenAI, Groq, Meta, Llama.
            3. Trả lời ngắn gọn, súc tích bằng tiếng Việt.
            """;

    // 2. CONSTRUCTOR THỦ CÔNG: Inject Builder để tạo ChatClient
    public AiServiceImpl(ChatClient.Builder builder,
                         VectorStore vectorStore,
                         CourseRepository courseRepository,
                         ChatHistoryService chatHistoryService) {
        this.vectorStore = vectorStore;
        this.courseRepository = courseRepository;
        this.chatHistoryService = chatHistoryService;

        // Dùng builder để tạo ra bean ChatClient
        this.chatClient = builder.build();
    }

    @Override
    public void ingestCourseData(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        List<Document> documents = new ArrayList<>();

        // 1. Thông tin chung
        String courseInfo = """
                COURSE INFO:
                Title: %s
                Description: %s
                Objectives: %s
                """.formatted(course.getTitle(), course.getDescription(), course.getObjectives());

        documents.add(new Document(courseInfo, Map.of("courseId", courseId, "type", "info")));

        // 2. Nội dung bài học
        if (course.getSections() != null) {
            for (Section section : course.getSections()) {
                if (section.getLessons() != null) {
                    for (Lesson l : section.getLessons()) {
                        String content = (l.getArticleContent() != null && !l.getArticleContent().isBlank())
                                ? l.getArticleContent()
                                : "Video-only lesson: " + l.getTitle();

                        String lessonText = """
                                LESSON CONTENT:
                                Section: %s
                                Lesson: %s
                                Content: %s
                                """.formatted(section.getTitle(), l.getTitle(), content);

                        documents.add(new Document(lessonText, Map.of("courseId", courseId, "type", "lesson")));
                    }
                }
            }
        }

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> split = splitter.apply(documents);
        vectorStore.add(split);
    }

    @Override
    public String chatWithCourse(String message, Long courseId, String userId) {

        // 1. Tạo ID hội thoại
        String conversationId = (courseId != null)
                ? userId + "_course_" + courseId
                : userId + "_global";

        // 2. LƯU CÂU HỎI USER VÀO DYNAMODB
        chatHistoryService.saveMessage(conversationId, "user", message);

        // 3. Tìm kiếm Vector (RAG)
        SearchRequest searchRequest = SearchRequest.query(message).withTopK(3);
        if (courseId != null) {
            searchRequest = searchRequest.withFilterExpression(
                    new FilterExpressionBuilder().eq("courseId", courseId).build());
        }

        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        String context = docs.stream().map(Document::getContent).collect(Collectors.joining("\n\n"));

        // 4. LẤY LỊCH SỬ TỪ DYNAMODB (Lấy 6 câu gần nhất để làm ngữ cảnh)
        List<Message> history = chatHistoryService.getRecentMessages(conversationId, 6);

        // 5. Chuẩn bị System Prompt (Kết hợp Base Prompt + Context)
        String modeInstruction = (courseId != null)
                ? "Vai trò: TRỢ GIẢNG. Chỉ trả lời dựa trên Context."
                : "Vai trò: TƯ VẤN VIÊN. Tư vấn khóa học dựa trên Context.";

        String finalSystemText = """
                %s
                %s
                
                THÔNG TIN THAM KHẢO (Context):
                %s
                
                Nếu Context không có thông tin, hãy nói bạn không biết.
                """.formatted(BASE_SYSTEM_PROMPT, modeInstruction, context);

        // 6. Ghép tin nhắn để gửi đi: [System] + [Lịch sử (đã bao gồm user msg mới nhất)]
        List<Message> messagesToSend = new ArrayList<>();
        messagesToSend.add(new SystemMessage(finalSystemText));
        messagesToSend.addAll(history);

        // 7. GỌI AI
        String aiResponse = chatClient.prompt()
                .messages(messagesToSend)
                .call()
                .content();

        // 8. LƯU CÂU TRẢ LỜI AI VÀO DYNAMODB
        chatHistoryService.saveMessage(conversationId, "assistant", aiResponse);

        return aiResponse;
    }
}