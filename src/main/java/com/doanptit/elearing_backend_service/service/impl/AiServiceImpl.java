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
@Slf4j
public class AiServiceImpl implements AiService {

    private final VectorStore vectorStore;
    private final CourseRepository courseRepository;
    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;

    // 🔥 PROMPT V3: Tone giọng trung tính, chuyên nghiệp
    private final String BASE_SYSTEM_PROMPT = """
            Bạn là Trợ lý AI hỗ trợ học tập của hệ thống E-Learning.
            
            NHIỆM VỤ:
            Hỗ trợ người dùng tìm kiếm khóa học phù hợp và giải đáp thắc mắc chuyên môn dựa trên dữ liệu được cung cấp.
            
            NGUYÊN TẮC TRẢ LỜI:
            1. CHỈ sử dụng thông tin trong phần [DỮ LIỆU NỘI BỘ] bên dưới để trả lời.
            2. Tuyệt đối KHÔNG nhắc đến các thuật ngữ kỹ thuật như "Context", "Vector Store", "XML".
            3. Nếu không tìm thấy thông tin trong dữ liệu, hãy trả lời: "Xin lỗi, hiện tại tài liệu hệ thống chưa có thông tin về vấn đề này."
            4. Văn phong: Trung lập, lịch sự, ngắn gọn và đi thẳng vào vấn đề. Xưng hô là "Tôi" và gọi người dùng là "Bạn".
            5. Khi đưa ra thông tin, hãy cố gắng trích dẫn nguồn (ví dụ: "Theo nội dung chương X...").
            6. VỀ DANH TÍNH: Nếu được hỏi "Bạn là ai?", "Ai tạo ra bạn?", hãy trả lời duy nhất: "Tôi là Trợ lý AI của hệ thống E-Learning, được phát triển để hỗ trợ quá trình học tập của bạn." Tuyệt đối không nhắc đến Meta, Llama, OpenAI hay tên cá nhân nào khác.
            """;

    public AiServiceImpl(ChatClient.Builder builder,
                         VectorStore vectorStore,
                         CourseRepository courseRepository,
                         ChatHistoryService chatHistoryService) {
        this.vectorStore = vectorStore;
        this.courseRepository = courseRepository;
        this.chatHistoryService = chatHistoryService;

        // Chỉ cần build đơn giản, vì mình đã xử lý history thủ công trong hàm chatWithCourse rồi
        this.chatClient = builder.build();
    }

    // --- 1. HÀM TRAIN TỪNG KHÓA (Cập nhật theo Entity mới) ---
    @Override
    public void ingestCourseData(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        log.info(">>>> [AI TRAIN] Bắt đầu học khóa: {}", course.getTitle());
        List<Document> documents = new ArrayList<>();

        // A. Thông tin chung (Metadata chuẩn theo Entity Course)
        // Xử lý tên giảng viên an toàn (tránh NullPointerException)
        String authorName = (course.getAuthor() != null)
                ? course.getAuthor().getLastname() + " " + course.getAuthor().getFirstname()
                : "Giảng viên hệ thống";

        String categoryName = (course.getCategory() != null) ? course.getCategory().name() : "Chung";

        // Tạo đoạn văn bản tổng quan để AI "hiểu" về khóa học này
        String courseInfo = """
                [LOẠI: TỔNG QUAN KHÓA HỌC]
                Tên khóa học: %s
                Giảng viên: %s
                Danh mục: %s
                Đánh giá: %.1f sao (%d lượt đánh giá)
                Đối tượng học phù hợp: %s
                Mô tả ngắn: %s
                Mục tiêu khóa học: %s
                """.formatted(
                course.getTitle(),
                authorName,
                categoryName,
                course.getAverageRating() != null ? course.getAverageRating() : 0.0,
                course.getTotalReviews() != null ? course.getTotalReviews() : 0,
                course.getTargetAudience() != null ? course.getTargetAudience() : "Mọi đối tượng",
                course.getShortDescription() != null ? course.getShortDescription() : course.getDescription(),
                course.getObjectives()
        );

        // Metadata giúp lọc và debug
        documents.add(new Document(courseInfo, Map.of(
                "courseId", courseId,
                "type", "info",
                "title", course.getTitle()
        )));

        // B. Nội dung bài học
        if (course.getSections() != null) {
            for (Section section : course.getSections()) {
                if (section.getLessons() != null) {
                    for (Lesson l : section.getLessons()) {
                        // Ưu tiên Article Content, nếu không có thì lấy Title làm nội dung (cho Video)
                        String cleanContent = (l.getArticleContent() != null && !l.getArticleContent().isBlank())
                                ? l.getArticleContent()
                                : "Bài học dạng Video có tiêu đề: " + l.getTitle();

                        String enrichedContent = """
                                [KHÓA HỌC: %s]
                                [CHƯƠNG: %s]
                                [BÀI HỌC: %s]
                                ----------------
                                NỘI DUNG KIẾN THỨC:
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
        log.info(">>>> [AI TRAIN] Hoàn tất học {} chunks cho khóa: {}", split.size(), course.getTitle());
    }

    // --- 2. HÀM TRAIN TOÀN BỘ (Giữ nguyên logic) ---
    @Override
    public void ingestAllActiveCourses() {
        List<Course> activeCourses = courseRepository.findByStatus(CourseStatus.ACTIVE);
        log.info(">>>> [AI BATCH] Tìm thấy {} khóa ACTIVE.", activeCourses.size());

        int count = 0;
        for (Course course : activeCourses) {
            try {
                ingestCourseData(course.getId());
                count++;
                log.info(">>>> [AI BATCH] Tiến độ: {}/{}", count, activeCourses.size());
            } catch (Exception e) {
                log.error(">>>> [AI BATCH] Lỗi khóa ID {}: {}", course.getId(), e.getMessage());
            }
        }
    }

    // --- 3. CHAT LOGIC (Tăng TopK & Cải thiện Prompt) ---
    @Override
    public String chatWithCourse(String message, Long courseId, String userId) {
        String conversationId = (courseId != null) ? userId + "_course_" + courseId : userId + "_global";

        // 1. Lưu tin nhắn User
        chatHistoryService.saveMessage(conversationId, "user", message);

        // 2. Tìm kiếm Vector (Retrieval)
        // 🔥 TĂNG TOP_K: Global = 8 (quét rộng), Course = 6 (đọc sâu)
        int topK = (courseId == null) ? 8 : 6;

        SearchRequest searchRequest = SearchRequest.query(message).withTopK(topK);

        if (courseId != null) {
            // Chat trong khóa học -> Lọc đúng ID khóa học
            searchRequest = searchRequest.withFilterExpression(new FilterExpressionBuilder().eq("courseId", courseId).build());
        }
        // Chat Global -> Tìm trên toàn bộ (Không filter)

        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        // Ghép nội dung tìm được
        String context = docs.stream()
                .map(d -> "--- THÔNG TIN THAM KHẢO ---\n" + d.getContent())
                .collect(Collectors.joining("\n\n"));

        // 3. Lấy lịch sử chat
        List<Message> history = chatHistoryService.getRecentMessages(conversationId, 6);

        // 4. Xây dựng Prompt
        String userMode = (courseId != null)
                ? "Người dùng đang hỏi về nội dung chuyên sâu của khóa học này."
                : "Người dùng đang hỏi tổng quan trên toàn hệ thống. Hãy tư vấn dựa trên các khóa học tìm thấy.";

        String finalSystemText = """
                %s
                
                NGỮ CẢNH:
                %s
                
                [DỮ LIỆU NỘI BỘ]
                %s
                """.formatted(BASE_SYSTEM_PROMPT, userMode, context);

        List<Message> messagesToSend = new ArrayList<>();
        messagesToSend.add(new SystemMessage(finalSystemText));
        messagesToSend.addAll(history);

        // 5. Gọi AI
        String aiResponse = chatClient.prompt().messages(messagesToSend).call().content();

        // 6. Làm sạch & Lưu
        String cleanResponse = sanitizeResponse(aiResponse);
        chatHistoryService.saveMessage(conversationId, "assistant", cleanResponse);

        return cleanResponse;
    }

    private String sanitizeResponse(String response) {
        if (response == null) return "Hệ thống đang xử lý, vui lòng thử lại sau.";
        // Xóa các thẻ nếu AI lỡ in ra
        return response.replaceAll("(?i)\\[DỮ LIỆU NỘI BỘ\\]|\\[LOẠI:.*?\\]", "").trim();
    }
}