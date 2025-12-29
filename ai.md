# 3. TRIỂN KHAI CHATBOT AI

## 3.1. Tổng quan

Chatbot AI là một trong những tính năng nổi bật của hệ thống E-Learning, được tích hợp để hỗ trợ học viên trong quá trình học tập. Chatbot hoạt động như một trợ giảng thông minh, có khả năng trả lời các câu hỏi của học viên dựa trên nội dung khóa học đã được lưu trữ trong hệ thống.

### 3.1.1. Mục tiêu

- **Hỗ trợ học viên 24/7**: Chatbot có thể trả lời câu hỏi của học viên bất cứ lúc nào mà không cần sự can thiệp của giáo viên
- **Trả lời chính xác**: Chỉ sử dụng thông tin từ nội dung khóa học, tránh trả lời sai hoặc bịa đặt thông tin
- **Bối cảnh hóa**: Hiểu được ngữ cảnh cuộc trò chuyện thông qua lịch sử chat
- **Tích hợp mượt mà**: Tích hợp trực tiếp vào giao diện học tập, không cần chuyển sang ứng dụng khác

### 3.1.2. Đặc điểm nổi bật

- **RAG (Retrieval-Augmented Generation)**: Sử dụng kỹ thuật RAG để tìm kiếm thông tin liên quan từ vector store trước khi trả lời
- **Vector Search**: Sử dụng pgvector để lưu trữ và tìm kiếm embeddings của nội dung khóa học
- **Context-aware**: Hiểu được ngữ cảnh cuộc trò chuyện thông qua chat history
- **Course-specific**: Có thể trả lời câu hỏi về một khóa học cụ thể hoặc trả lời câu hỏi chung

## 3.2. Kiến trúc và Công nghệ

### 3.2.1. Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────┐
│              Frontend (ReactJS)                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Chat Interface                                   │  │
│  │  - Gửi câu hỏi                                    │  │
│  │  - Hiển thị câu trả lời                          │  │
│  │  - Lịch sử chat                                   │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ HTTP POST /api/ai/chat
                        │
┌───────────────────────▼─────────────────────────────────┐
│         Backend (Spring Boot)                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │  AiController                                     │  │
│  │  - Nhận request từ frontend                      │  │
│  │  - Xác thực người dùng                           │  │
│  └───────────────┬──────────────────────────────────┘  │
│                  │                                      │
│  ┌───────────────▼──────────────────────────────────┐  │
│  │  AiService                                        │  │
│  │  - Xử lý logic chat                              │  │
│  │  - Tìm kiếm vector                               │  │
│  │  - Gọi AI model                                  │  │
│  └───────────────┬──────────────────────────────────┘  │
│                  │                                      │
│      ┌───────────┼───────────┐                          │
│      │           │           │                          │
│  ┌───▼───┐  ┌───▼───┐  ┌───▼───┐                      │
│  │Vector │  │Chat   │  │Chat   │                      │
│  │Store  │  │Client │  │History│                      │
│  │(pgvec)│  │(Groq) │  │(Dynamo)│                      │
│  └───────┘  └───────┘  └───────┘                      │
└─────────────────────────────────────────────────────────┘
```

### 3.2.2. Công nghệ sử dụng

#### a) Spring AI Framework
- **Phiên bản**: 1.0.0-M4
- **Mục đích**: Framework chính để tích hợp AI vào ứng dụng Spring Boot
- **Thành phần**:
  - `ChatClient`: Giao tiếp với AI model (Groq API)
  - `VectorStore`: Quản lý vector embeddings
  - `Document`: Đại diện cho tài liệu trong vector store
  - `TokenTextSplitter`: Chia nhỏ văn bản thành các chunks

#### b) PostgreSQL với pgvector
- **Mục đích**: Lưu trữ vector embeddings của nội dung khóa học
- **Cấu hình**:
  - Index type: HNSW (Hierarchical Navigable Small World)
  - Dimension: 384 (sử dụng transformer model)
  - Auto-initialize schema: true

#### c) Groq API (Llama 3.1 8B Instant)
- **Model**: llama-3.1-8b-instant
- **Base URL**: https://api.groq.com/openai
- **Lý do chọn**: 
  - Tốc độ xử lý nhanh
  - Hỗ trợ tiếng Việt tốt
  - Chi phí thấp
  - API tương thích với OpenAI

#### d) DynamoDB (LocalStack)
- **Mục đích**: Lưu trữ lịch sử chat
- **Cấu trúc bảng**:
  - Partition Key: ConversationId
  - Sort Key: CreatedAt
  - Attributes: Role, Content

#### e) Transformers (Embedding Model)
- **Mục đích**: Tạo embeddings cho nội dung khóa học
- **Model**: Transformer model với dimension 384

### 3.2.3. Dependencies

```kotlin
// Spring AI
implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter")
implementation("org.springframework.ai:spring-ai-transformers-spring-boot-starter")

// AWS SDK for DynamoDB
implementation("software.amazon.awssdk:dynamodb:2.26.24")
```

## 3.3. Quy trình Triển khai

### 3.3.1. Giai đoạn 1: Chuẩn bị Dữ liệu (Data Ingestion)

#### a) Thu thập dữ liệu khóa học

Khi giáo viên tạo xong một khóa học và được Admin phê duyệt, hệ thống cần "dạy" AI về nội dung khóa học đó. Quá trình này được gọi là **Data Ingestion** hoặc **Training**.

**Luồng xử lý:**

1. **Lấy thông tin khóa học**: Hệ thống truy vấn database để lấy toàn bộ thông tin khóa học bao gồm:
   - Thông tin chung: Tiêu đề, mô tả, mục tiêu
   - Các chương (Sections)
   - Các bài học (Lessons) với nội dung chi tiết

2. **Chuẩn hóa dữ liệu**: Mỗi phần nội dung được format theo cấu trúc chuẩn:
   ```
   [KHÓA HỌC: Tên khóa học]
   [CHƯƠNG: Tên chương]
   [BÀI HỌC: Tên bài học]
   ----------------
   NỘI DUNG CHI TIẾT:
   [Nội dung bài học]
   ```

3. **Chia nhỏ văn bản (Text Chunking)**: Sử dụng `TokenTextSplitter` để chia nội dung thành các chunks nhỏ hơn, phù hợp với giới hạn của AI model.

4. **Tạo Embeddings**: Mỗi chunk được chuyển đổi thành vector embedding (384 dimensions) bằng transformer model.

5. **Lưu vào Vector Store**: Các embeddings được lưu vào PostgreSQL với metadata:
   - `courseId`: ID khóa học
   - `type`: Loại nội dung (info, lesson)

**Code Implementation:**

```java
@Override
public void ingestCourseData(Long courseId) {
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

    List<Document> documents = new ArrayList<>();

    // 1. Thông tin chung khóa học
    String courseInfo = """
            [LOẠI: THÔNG TIN KHÓA HỌC]
            Tên khóa: %s
            Mô tả: %s
            Mục tiêu: %s
            """.formatted(course.getTitle(), course.getDescription(), course.getObjectives());
    documents.add(new Document(courseInfo, Map.of("courseId", courseId, "type", "info")));

    // 2. Nội dung bài học với context enrichment
    if (course.getSections() != null) {
        for (Section section : course.getSections()) {
            if (section.getLessons() != null) {
                for (Lesson lesson : section.getLessons()) {
                    String enrichedContent = """
                            [KHÓA HỌC: %s]
                            [CHƯƠNG: %s]
                            [BÀI HỌC: %s]
                            ----------------
                            NỘI DUNG CHI TIẾT:
                            %s
                            """.formatted(course.getTitle(), section.getTitle(), 
                                         lesson.getTitle(), lesson.getArticleContent());
                    documents.add(new Document(enrichedContent, 
                                              Map.of("courseId", courseId, "type", "lesson")));
                }
            }
        }
    }

    // 3. Chia nhỏ và lưu vào vector store
    TokenTextSplitter splitter = new TokenTextSplitter();
    List<Document> split = splitter.apply(documents);
    vectorStore.add(split);
}
```

**Kỹ thuật Smart Chunking:**

- **Context Enrichment**: Mỗi chunk được gắn thêm metadata về khóa học, chương, bài học để AI có thể trích dẫn chính xác nguồn thông tin
- **Token-based Splitting**: Chia văn bản dựa trên số lượng tokens thay vì số ký tự, đảm bảo phù hợp với giới hạn của AI model

#### b) API Endpoints cho Training

**1. Train một khóa học cụ thể:**
```
POST /api/ai/train/{courseId}
Authorization: Bearer {token}
Role: ADMIN
```

**2. Train tất cả khóa học đang active:**
```
POST /api/ai/train/all
Authorization: Bearer {token}
Role: ADMIN
```

### 3.3.2. Giai đoạn 2: Xử lý Câu hỏi (Query Processing)

Khi học viên gửi câu hỏi, hệ thống thực hiện các bước sau:

#### Bước 1: Lưu tin nhắn người dùng

Tin nhắn của người dùng được lưu vào DynamoDB để duy trì lịch sử cuộc trò chuyện.

```java
String conversationId = (courseId != null) 
    ? userId + "_course_" + courseId 
    : userId + "_global";
chatHistoryService.saveMessage(conversationId, "user", message);
```

#### Bước 2: Tìm kiếm Vector (Vector Similarity Search)

Hệ thống chuyển đổi câu hỏi thành embedding và tìm kiếm các chunks có độ tương đồng cao nhất trong vector store.

**Cấu hình tìm kiếm:**
- **TopK**: 4 (lấy 4 chunks có độ tương đồng cao nhất)
- **Filter**: Nếu có `courseId`, chỉ tìm trong khóa học đó
- **Similarity Metric**: Cosine similarity

```java
SearchRequest searchRequest = SearchRequest.query(message).withTopK(4);
if (courseId != null) {
    searchRequest = searchRequest.withFilterExpression(
        new FilterExpressionBuilder().eq("courseId", courseId).build()
    );
}
List<Document> docs = vectorStore.similaritySearch(searchRequest);
String context = docs.stream()
    .map(Document::getContent)
    .collect(Collectors.joining("\n\n"));
```

#### Bước 3: Lấy lịch sử chat

Lấy 6 tin nhắn gần nhất để AI hiểu được ngữ cảnh cuộc trò chuyện.

```java
List<Message> history = chatHistoryService.getRecentMessages(conversationId, 6);
```

#### Bước 4: Xây dựng Prompt

Prompt được xây dựng với cấu trúc rõ ràng để AI hiểu rõ nhiệm vụ và giới hạn.

**Cấu trúc Prompt:**

```
[BASE_SYSTEM_PROMPT]
[VAI TRÒ]
<CONTEXT_DATABASE>
[Nội dung tìm được từ vector store]
</CONTEXT_DATABASE>
Hãy trả lời dựa trên thẻ <CONTEXT_DATABASE> ở trên.
```

**Base System Prompt:**
```
Bạn là Trợ giảng AI chuyên nghiệp của hệ thống E-Learning.

NHIỆM VỤ:
Giải đáp thắc mắc của học viên CHỈ DỰA TRÊN thông tin được cung cấp trong phần CONTEXT.

QUY TẮC BẤT KHẢ XÂM PHẠM:
1. KHÔNG được sử dụng kiến thức bên ngoài để trả lời nếu Context không có.
2. Nếu Context không chứa thông tin, hãy trả lời: "Xin lỗi, tài liệu khóa học hiện tại chưa đề cập đến vấn đề này."
3. Trích dẫn tên [BÀI HỌC] hoặc [CHƯƠNG] chứa thông tin đó nếu có thể.
4. Không nhắc đến OpenAI, Groq, Llama, Meta.
5. Trả lời ngắn gọn, súc tích bằng tiếng Việt.
```

**Vai trò:**
- Nếu có `courseId`: "Vai trò: TRỢ GIẢNG" (trả lời về nội dung khóa học cụ thể)
- Nếu không có `courseId`: "Vai trò: TƯ VẤN VIÊN" (trả lời câu hỏi chung)

#### Bước 5: Gọi AI Model

Gửi prompt đến Groq API và nhận phản hồi.

```java
List<Message> messagesToSend = new ArrayList<>();
messagesToSend.add(new SystemMessage(finalSystemText));
messagesToSend.addAll(history);

String aiResponse = chatClient.prompt()
    .messages(messagesToSend)
    .call()
    .content();
```

#### Bước 6: Lưu phản hồi

Phản hồi của AI được lưu vào DynamoDB để duy trì lịch sử.

```java
chatHistoryService.saveMessage(conversationId, "assistant", aiResponse);
return aiResponse;
```

### 3.3.3. Luồng hoạt động tổng thể

```
┌─────────────┐
│  Học viên   │
│  gửi câu hỏi│
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  AiController       │
│  POST /api/ai/chat  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  AiService          │
│  chatWithCourse()   │
└──────┬──────────────┘
       │
       ├─────────────────┐
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│Lưu tin nhắn  │  │Tìm kiếm     │
│vào DynamoDB  │  │Vector Store  │
└──────────────┘  └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Lấy 4 chunks  │
                 │liên quan nhất│
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Lấy chat      │
                 │history (6 msg)│
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Xây dựng      │
                 │Prompt        │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Gọi Groq API  │
                 │(Llama 3.1)   │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Lưu phản hồi  │
                 │vào DynamoDB  │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │Trả về cho    │
                 │Frontend      │
                 └──────────────┘
```

## 3.4. Các Tính năng Chính

### 3.4.1. Chat theo Khóa học (Course-specific Chat)

Học viên có thể chat với AI về nội dung của một khóa học cụ thể. AI sẽ chỉ tìm kiếm và trả lời dựa trên nội dung của khóa học đó.

**Request:**
```json
POST /api/ai/chat
{
  "message": "Giải thích về RESTful API là gì?",
  "courseId": 123
}
```

**Xử lý:**
- Filter vector search theo `courseId`
- Chỉ lấy context từ khóa học đó
- Vai trò: TRỢ GIẢNG

### 3.4.2. Chat Tổng quát (Global Chat)

Học viên có thể chat với AI về các câu hỏi chung, không gắn với khóa học cụ thể.

**Request:**
```json
POST /api/ai/chat
{
  "message": "Hệ thống có những tính năng gì?"
}
```

**Xử lý:**
- Tìm kiếm trong toàn bộ vector store
- Vai trò: TƯ VẤN VIÊN

### 3.4.3. Duy trì Lịch sử Chat

Hệ thống lưu trữ lịch sử chat để:
- AI hiểu được ngữ cảnh cuộc trò chuyện
- Học viên có thể xem lại lịch sử
- Hỗ trợ cuộc trò chuyện đa lượt (multi-turn conversation)

**Cấu trúc Conversation ID:**
- Course-specific: `{userId}_course_{courseId}`
- Global: `{userId}_global`

### 3.4.4. Context-aware Responses

AI có thể hiểu được ngữ cảnh nhờ:
- **Chat History**: 6 tin nhắn gần nhất
- **Vector Search**: Tìm kiếm thông tin liên quan
- **Metadata**: Thông tin về khóa học, chương, bài học

## 3.5. Cấu hình và Thiết lập

### 3.5.1. Cấu hình Spring AI

**File: `application.properties`**

```properties
# Groq API Configuration
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.1-8b-instant

# Embedding Configuration
spring.ai.embedding.transformer.enabled=true
spring.ai.openai.embedding.enabled=false

# Vector Store Configuration
spring.ai.vectorstore.pgvector.initialize-schema=true
spring.ai.vectorstore.pgvector.index-type=HNSW
spring.ai.vectorstore.pgvector.dimension=384
```

### 3.5.2. Cấu hình DynamoDB

**File: `LocalStackDynamoDBConfig.java`**

```java
@Configuration
public class LocalStackDynamoDBConfig {
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4577"))
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")
            ))
            .build();
    }
}
```

### 3.5.3. Cấu trúc Bảng Chat History

**Bảng: `Chat_History`**

| Thuộc tính | Kiểu | Mô tả |
|------------|------|-------|
| ConversationId | String (PK) | ID cuộc trò chuyện |
| CreatedAt | String (SK) | Thời gian tạo |
| Role | String | "user" hoặc "assistant" |
| Content | String | Nội dung tin nhắn |

## 3.6. Prompt Engineering

### 3.6.1. Kỹ thuật sử dụng

**1. System Prompt rõ ràng:**
- Định nghĩa vai trò của AI
- Quy tắc nghiêm ngặt về việc chỉ sử dụng context được cung cấp
- Hướng dẫn cách trả lời khi không có thông tin

**2. Context Enrichment:**
- Gắn metadata vào mỗi chunk (khóa học, chương, bài học)
- Giúp AI trích dẫn chính xác nguồn thông tin

**3. XML-like Tags:**
- Sử dụng thẻ `<CONTEXT_DATABASE>` để phân tách rõ ràng context
- Giúp AI dễ dàng nhận biết phần nào là dữ liệu cần sử dụng

**4. Temperature Control:**
- Sử dụng temperature thấp để giảm "ảo giác" (hallucination)
- Đảm bảo AI trả lời chính xác dựa trên context

### 3.6.2. Ví dụ Prompt

**Input từ người dùng:**
```
"Giải thích về RESTful API là gì?"
```

**Context tìm được:**
```
[KHÓA HỌC: Lập trình Web với Spring Boot]
[CHƯƠNG: API Development]
[BÀI HỌC: RESTful API Design]
----------------
NỘI DUNG CHI TIẾT:
RESTful API là một kiến trúc API tuân theo các nguyên tắc REST...
```

**System Prompt gửi đến AI:**
```
Bạn là Trợ giảng AI chuyên nghiệp của hệ thống E-Learning.

NHIỆM VỤ:
Giải đáp thắc mắc của học viên CHỈ DỰA TRÊN thông tin được cung cấp trong phần CONTEXT.

QUY TẮC BẤT KHẢ XÂM PHẠM:
1. KHÔNG được sử dụng kiến thức bên ngoài...
2. Nếu Context không chứa thông tin...
...

Vai trò: TRỢ GIẢNG.

<CONTEXT_DATABASE>
[KHÓA HỌC: Lập trình Web với Spring Boot]
[CHƯƠNG: API Development]
[BÀI HỌC: RESTful API Design]
----------------
NỘI DUNG CHI TIẾT:
RESTful API là một kiến trúc API tuân theo các nguyên tắc REST...
</CONTEXT_DATABASE>

Hãy trả lời dựa trên thẻ <CONTEXT_DATABASE> ở trên.
```

## 3.7. Tối ưu hóa và Cải tiến

### 3.7.1. Tối ưu Vector Search

- **HNSW Index**: Sử dụng HNSW (Hierarchical Navigable Small World) index cho tốc độ tìm kiếm nhanh
- **TopK = 4**: Lấy 4 chunks có độ tương đồng cao nhất để cân bằng giữa độ chính xác và độ dài context
- **Filter Expression**: Lọc theo courseId để giảm không gian tìm kiếm

### 3.7.2. Tối ưu Chat History

- **Limit = 6**: Chỉ lấy 6 tin nhắn gần nhất để:
  - Giảm độ dài prompt
  - Giảm chi phí API
  - Vẫn đủ để hiểu ngữ cảnh

### 3.7.3. Error Handling

- **Try-catch**: Xử lý lỗi khi gọi AI API
- **Fallback**: Trả về thông báo lỗi thân thiện nếu AI không phản hồi
- **Logging**: Ghi log để theo dõi và debug

## 3.8. API Endpoints

### 3.8.1. Chat với AI

**Endpoint:** `POST /api/ai/chat`

**Request:**
```json
{
  "message": "Câu hỏi của học viên",
  "courseId": 123  // Optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thành công",
  "data": "Câu trả lời từ AI",
  "timestamp": "2024-01-01T00:00:00"
}
```

**Authorization:** 
- Roles: STUDENT, TEACHER, ADMIN
- JWT Token required

### 3.8.2. Train AI cho khóa học

**Endpoint:** `POST /api/ai/train/{courseId}`

**Response:**
```json
{
  "success": true,
  "message": "AI đã học dữ liệu khóa học thành công! Dữ liệu đã được lưu vào Vector Store.",
  "data": null
}
```

**Authorization:**
- Role: ADMIN only

### 3.8.3. Train tất cả khóa học

**Endpoint:** `POST /api/ai/train/all`

**Response:**
```json
{
  "success": true,
  "message": "Đã hoàn tất nạp dữ liệu cho TOÀN BỘ khóa học Active!",
  "data": null
}
```

**Authorization:**
- Role: ADMIN only

## 3.9. Tích hợp với Frontend (ReactJS)

### 3.9.1. Gọi API từ React

```javascript
const chatWithAI = async (message, courseId = null) => {
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        message: message,
        courseId: courseId
      })
    });
    
    const data = await response.json();
    return data.data; // Câu trả lời từ AI
  } catch (error) {
    console.error('Error chatting with AI:', error);
    throw error;
  }
};
```

### 3.9.2. Component Chat Interface

```jsx
const ChatBot = ({ courseId }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  
  const handleSend = async () => {
    // Thêm tin nhắn người dùng
    setMessages([...messages, { role: 'user', content: input }]);
    
    // Gọi API
    const response = await chatWithAI(input, courseId);
    
    // Thêm phản hồi AI
    setMessages(prev => [...prev, { role: 'assistant', content: response }]);
    
    setInput('');
  };
  
  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.role}`}>
            {msg.content}
          </div>
        ))}
      </div>
      <div className="input-area">
        <input 
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
        />
        <button onClick={handleSend}>Gửi</button>
      </div>
    </div>
  );
};
```

## 3.10. Kết quả và Đánh giá

### 3.10.1. Hiệu quả

- **Độ chính xác**: AI chỉ trả lời dựa trên nội dung khóa học, tránh được việc bịa đặt thông tin
- **Tốc độ phản hồi**: Trung bình 2-3 giây cho mỗi câu trả lời
- **Khả năng hiểu ngữ cảnh**: AI có thể hiểu được cuộc trò chuyện đa lượt nhờ chat history

### 3.10.2. Hạn chế

- **Phụ thuộc vào chất lượng dữ liệu**: Nếu nội dung khóa học không đầy đủ, AI sẽ không thể trả lời tốt
- **Giới hạn context**: Chỉ sử dụng 4 chunks, có thể bỏ sót thông tin nếu câu hỏi phức tạp
- **Chi phí API**: Mỗi lần chat đều tốn chi phí gọi Groq API

### 3.10.3. Hướng phát triển

- **Caching**: Cache các câu hỏi thường gặp để giảm chi phí
- **Streaming Response**: Trả về câu trả lời theo dòng để cải thiện trải nghiệm người dùng
- **Multi-modal**: Hỗ trợ hỏi đáp về hình ảnh, video trong tương lai
- **Fine-tuning**: Fine-tune model riêng cho từng lĩnh vực học tập

## 3.11. Kết luận

Chatbot AI là một tính năng quan trọng của hệ thống E-Learning, giúp nâng cao trải nghiệm học tập của học viên. Việc sử dụng kỹ thuật RAG với vector store và prompt engineering cẩn thận đã giúp chatbot có thể trả lời chính xác các câu hỏi của học viên dựa trên nội dung khóa học, đồng thời tránh được các vấn đề về "ảo giác" thường gặp ở các AI chatbot khác.

Hệ thống được thiết kế với khả năng mở rộng tốt, có thể dễ dàng thêm các tính năng mới như streaming response, multi-modal support, hoặc tích hợp với các AI model khác trong tương lai.

