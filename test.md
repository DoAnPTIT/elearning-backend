 BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
## HỆ THỐNG E-LEARNING VỚI TÍCH HỢP AI

---

## PHẦN 1: KIẾN TRÚC VÀ MÔI TRƯỜNG TRIỂN KHAI

### 1.1. Tổng quan Kiến trúc Hệ thống

Hệ thống E-Learning được xây dựng theo mô hình **kiến trúc phân lớp (Layered Architecture)** với sự tách biệt rõ ràng giữa các tầng, đảm bảo tính mô đun hóa, dễ bảo trì và mở rộng. Hệ thống được phát triển bằng **Spring Boot 3.4.0** và **Java 17**, tuân theo các nguyên tắc thiết kế hiện đại như RESTful API, Dependency Injection, và Separation of Concerns.

#### 1.1.1. Kiến trúc Tổng quan

Hệ thống được chia thành 4 tầng chính:

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Layer (Frontend)                   │
│              ReactJS Application (Web Browser)               │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ HTTP/HTTPS REST API
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                  Presentation Layer                         │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  REST Controllers (15+ API Endpoints)                │  │
│  │  - AuthenticationController                          │  │
│  │  - CourseController, TeacherCourseController         │  │
│  │  - UserController, AdminController                  │  │
│  │  - StudentController, EnrollmentController          │  │
│  │  - AiController, CommentController                  │  │
│  │  - NotificationController, FileController           │  │
│  └─────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  DTOs (Data Transfer Objects)                        │  │
│  │  - Request DTOs: 21+ classes                         │  │
│  │  - Response DTOs: 38+ classes                        │  │
│  │  - ApiResponse<T>: Wrapper chuẩn                    │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                    Business Logic Layer                      │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Service Layer (15+ Services)                       │  │
│  │  - AuthenticationService                            │  │
│  │  - CourseService, EnrollmentService                 │  │
│  │  - UserService, AiService                           │  │
│  │  - EmailService, S3Service                          │  │
│  └─────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Mapper Layer (MapStruct)                            │  │
│  │  - Entity ↔ DTO Conversion                          │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                    Data Access Layer                         │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Repository Layer (Spring Data JPA)                 │  │
│  │  - 18+ Repository interfaces                         │  │
│  │  - Custom queries, Specifications                   │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                    Data Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  PostgreSQL  │  │   MinIO/S3   │  │  DynamoDB    │     │
│  │  (pgvector)  │  │  (File Store)│  │ (JWT Blacklist)│   │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

#### 1.1.2. Các Tầng Chi tiết

**a) Presentation Layer (Lớp trình bày)**

Lớp này chịu trách nhiệm xử lý các HTTP request từ frontend ReactJS, validate dữ liệu đầu vào, và trả về response dưới dạng JSON.

- **REST Controllers**: 15+ controllers xử lý các endpoint API
  - Mỗi controller được đánh dấu bằng `@RestController` và `@RequestMapping`
  - Sử dụng `@PreAuthorize` để kiểm soát quyền truy cập dựa trên vai trò (ADMIN, TEACHER, STUDENT)
  - Validation sử dụng Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)

- **DTOs (Data Transfer Objects)**:
  - Request DTOs: 21+ classes trong package `dto.req` để nhận dữ liệu từ client
  - Response DTOs: 38+ classes trong package `dto.res` để trả về dữ liệu cho client
  - `ApiResponse<T>`: Wrapper chuẩn cho tất cả API responses, đảm bảo format thống nhất
  - `PagedResponse<T>`: Wrapper cho phân trang với thông tin `totalElements`, `totalPages`, `currentPage`

- **Exception Handling**:
  - `GlobalExceptionHandler`: Xử lý exception toàn cục với `@ControllerAdvice`
  - `AppException`: Custom exception với mã lỗi và thông điệp
  - `ErrorCode`: Enum định nghĩa các mã lỗi chuẩn

**b) Business Logic Layer (Lớp nghiệp vụ)**

Lớp này chứa toàn bộ logic nghiệp vụ của hệ thống, xử lý dữ liệu và điều phối các tác vụ.

- **Service Layer**:
  - Interface Services: Định nghĩa contract cho các service
  - Implementation Services: Triển khai logic nghiệp vụ trong package `service.impl`
  - Transaction Management: Sử dụng `@Transactional` để đảm bảo tính toàn vẹn dữ liệu
  - Async Processing: Sử dụng `@Async` cho các tác vụ không đồng bộ (gửi email, xử lý file)

- **Mapper Layer (MapStruct)**:
  - Chuyển đổi tự động giữa Entity và DTO tại compile time
  - Giảm boilerplate code, tăng hiệu suất
  - Các mapper: `CourseMapper`, `UserMapper`, `LessonMapper`, `SectionMapper`, `ExamMapper`

- **Event Handling**:
  - Spring Events cho async processing
  - `CourseContentUpdatedEvent`: Event khi nội dung khóa học thay đổi
  - `NotificationEvent`: Event cho thông báo

**c) Data Access Layer (Lớp truy cập dữ liệu)**

Lớp này chịu trách nhiệm truy cập và thao tác với cơ sở dữ liệu.

- **Repository Layer (Spring Data JPA)**:
  - 18+ repository interfaces kế thừa `JpaRepository`
  - Query methods: Sử dụng naming convention (ví dụ: `findByEmail`, `findByStatus`)
  - `@Query`: JPQL hoặc Native SQL cho các truy vấn phức tạp
  - Specifications: Dynamic queries với Criteria API

- **Entity Models**:
  - 17 entity classes trong package `model`
  - JPA annotations: `@Entity`, `@Table`, `@OneToMany`, `@ManyToOne`, `@JoinColumn`
  - `BaseEntity`: Abstract class chứa audit fields (created_on, updated_on, created_by, updated_by)
  - Lazy Loading: Sử dụng `FetchType.LAZY` để tối ưu hiệu suất
  - Batch Fetching: `@BatchSize` để giảm N+1 query problem

**d) Configuration Layer (Lớp cấu hình)**

- **Security Configuration**:
  - `SecurityConfig`: Cấu hình Spring Security với JWT authentication
  - `JwtAuthenticationFilter`: Filter xử lý JWT token trong mỗi request
  - `JwtUtil`: Utility class cho việc tạo và validate JWT
  - `CustomUserDetailsService`: Load user details từ database
  - Role-based Access Control (RBAC): ADMIN, TEACHER, STUDENT

- **Database Configuration**:
  - Flyway migration: Tự động chạy migration khi ứng dụng khởi động
  - JPA/Hibernate configuration: Connection pooling, lazy loading
  - pgvector extension: Hỗ trợ vector database cho AI

- **AWS/S3 Configuration**:
  - `S3Config`: Cấu hình S3 client (MinIO cho development)
  - `LocalStackDynamoDBConfig`: Cấu hình DynamoDB (LocalStack cho development)

### 1.2. Công nghệ và Framework

#### 1.2.1. Core Framework

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **Java** | 17 | Ngôn ngữ lập trình chính |
| **Spring Boot** | 3.4.0 | Framework chính, cung cấp auto-configuration |
| **Spring Framework** | 6.x | Dependency injection, AOP, MVC |
| **Spring Data JPA** | - | ORM và data access abstraction |
| **Spring Security** | - | Authentication & Authorization |
| **Spring AI** | 1.0.0-M4 | Tích hợp AI (OpenAI, Vector Store) |
| **Spring WebSocket** | - | Real-time communication |

#### 1.2.2. Database và Persistence

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **PostgreSQL** | 17+ | Cơ sở dữ liệu quan hệ chính |
| **pgvector** | - | Vector database extension cho AI embeddings |
| **Hibernate** | - | ORM framework (JPA implementation) |
| **Flyway** | 11.10.0 | Database migration tool |
| **DynamoDB** (LocalStack) | - | NoSQL database cho JWT blacklist |

#### 1.2.3. Security và Authentication

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **JWT (jjwt)** | 0.11.5 | JSON Web Token cho stateless authentication |
| **BCrypt** | - | Password hashing algorithm |
| **Spring Security** | - | Security framework |

#### 1.2.4. Storage và File Management

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **MinIO** | - | Object storage (S3-compatible) cho development |
| **AWS SDK v2** | 2.26.24 | S3 client library |
| **Thumbnailator** | 0.4.20 | Image processing (resize images) |

#### 1.2.5. Utilities và Tools

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **Lombok** | 1.18.34 | Code generation (getters, setters, builders) |
| **MapStruct** | 1.5.5.Final | Entity-DTO mapping tại compile time |
| **Apache POI** | 5.2.5 | Excel file processing (import users) |
| **Jakarta Validation** | - | Input validation |
| **Spring Mail** | - | Email sending |

### 1.3. Môi trường Triển khai

#### 1.3.1. Môi trường Phát triển (Development)

**Cấu hình**:
- **IDE**: IntelliJ IDEA / VS Code
- **Java**: JDK 17
- **Build Tool**: Gradle 8.x (Kotlin DSL)
- **Database**: PostgreSQL 17+ (chạy qua Docker)
- **Storage**: MinIO (chạy qua Docker)
- **LocalStack**: AWS services emulator (DynamoDB)

**Docker Compose Services**:
```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    ports: 5436:5432
    environment:
      POSTGRES_DB: elearing-backend-service
      POSTGRES_USER: elearningservice
      POSTGRES_PASSWORD: password
  
  minio:
    image: minio/minio
    ports: 9000:9000 (API), 9001:9001 (Console)
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
  
  localstack:
    image: localstack/localstack
    ports: 4577:4566
    services: DynamoDB
```

**Ports sử dụng**:
- Application: `8080` (default Spring Boot port)
- PostgreSQL: `5436`
- MinIO API: `9000`
- MinIO Console: `9001`
- LocalStack: `4577`

#### 1.3.2. Môi trường Production

**Yêu cầu hệ thống**:
- **OS**: Linux (Ubuntu 20.04+ / CentOS 7+)
- **Java**: OpenJDK 17 hoặc Oracle JDK 17
- **Memory**: Tối thiểu 2GB RAM, khuyến nghị 4GB+
- **CPU**: Tối thiểu 2 cores, khuyến nghị 4+ cores
- **Disk**: Tối thiểu 20GB, khuyến nghị 50GB+ (tùy thuộc vào số lượng file)

**Dependencies**:
- PostgreSQL 17+ với pgvector extension
- MinIO hoặc AWS S3 cho file storage
- AWS DynamoDB (hoặc LocalStack cho môi trường nhỏ)

#### 1.3.3. Quy trình Build và Deploy

**Build Application**:
```bash
# Clean và build project
./gradlew clean build

# Chạy tests
./gradlew test

# Tạo JAR file
./gradlew bootJar
```

**Chạy Development Environment**:
```bash
# Khởi động Docker services
docker compose up -d

# Chạy application
./gradlew bootRun
```

**Production Deployment**:
1. Build JAR file: `./gradlew bootJar`
2. Cấu hình environment variables
3. Setup database (PostgreSQL với pgvector)
4. Setup storage (MinIO hoặc AWS S3)
5. Run Flyway migrations (tự động khi app khởi động)
6. Deploy JAR file
7. Start application: `java -jar elearning-backend-service.jar`

### 1.4. Cấu hình Hệ thống

#### 1.4.1. Application Configuration

**File**: `application.yml`

```yaml
spring:
  application:
    name: elearning-backend-service
  
  datasource:
    url: jdbc:postgresql://localhost:5436/elearing-backend-service
    username: elearningservice
    password: password
  
  jpa:
    hibernate:
      ddl-auto: none  # Sử dụng Flyway
    show-sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
  
  jwt:
    secret: ${JWT_SECRET:defaultSecretKey...}
    expiration: 86400000  # 24 hours
```

#### 1.4.2. Security Configuration

**Features**:
- **JWT-based authentication**: Stateless, không cần session
- **Role-based access control (RBAC)**: ADMIN, TEACHER, STUDENT
- **CORS enabled**: Cho phép frontend ReactJS gọi API
- **CSRF disabled**: Không cần cho REST API
- **Password encryption**: BCrypt với salt rounds

**Security Filter Chain**:
1. Public endpoints: `/api/auth/**`, `/api/files/**`, `/swagger-ui/**`
2. Protected endpoints: Tất cả các endpoint khác yêu cầu JWT token
3. Method-level security: `@PreAuthorize("hasRole('ADMIN')")` annotations

#### 1.4.3. API Design

- **RESTful**: Tuân theo chuẩn REST với HTTP methods (GET, POST, PUT, DELETE, PATCH)
- **Response Format**: 
  ```json
  {
    "success": true,
    "message": "...",
    "data": {...},
    "timestamp": "..."
  }
  ```
- **Pagination**: `PagedResponse<T>` với `page`, `size`, `totalElements`, `totalPages`
- **Error Handling**: Standardized error responses với mã lỗi và thông điệp

---

## PHẦN 2: XÂY DỰNG CÁC CHỨC NĂNG CHÍNH

### 2.1. Tổng quan các Chức năng

Hệ thống E-Learning cung cấp các chức năng chính cho 3 đối tượng người dùng: **Học viên (Student)**, **Giáo viên (Teacher)**, và **Quản trị viên (Admin)**. Tổng cộng có **80+ API endpoints** được tổ chức thành **15+ controllers**.

### 2.2. Chức năng Xác thực và Phân quyền

#### 2.2.1. Đăng nhập và Đăng xuất

**Controller**: `AuthenticationController`

**API Endpoints**:
- `POST /api/auth/login`: Đăng nhập
- `POST /api/auth/logout`: Đăng xuất
- `POST /api/auth/forgot-password`: Quên mật khẩu
- `POST /api/auth/reset-password`: Reset mật khẩu

**Cơ chế hoạt động**:
1. **Đăng nhập**:
   - Nhận email và password từ client
   - Validate thông tin đăng nhập
   - Tạo JWT token với thông tin user (email, role)
   - Trả về token và thông tin user
   - Lưu `last_login` timestamp

2. **JWT Token**:
   - Payload chứa: email, role, expiration time
   - Secret key được lưu trong environment variable
   - Expiration: 24 giờ
   - Token được gửi trong header: `Authorization: Bearer <token>`

3. **Đăng xuất**:
   - Thêm token vào blacklist (DynamoDB)
   - Token sẽ không còn hợp lệ sau khi logout

**Code Example**:
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
    LoginResponse response = authenticationService.login(request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

#### 2.2.2. Phân quyền và Bảo mật

**Cơ chế**:
- **Role-based Access Control (RBAC)**: 3 vai trò chính
  - `ADMIN`: Quản lý toàn bộ hệ thống
  - `TEACHER`: Tạo và quản lý khóa học
  - `STUDENT`: Học tập và tương tác

- **Method-level Security**: Sử dụng `@PreAuthorize`
  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/users")
  public ResponseEntity<?> getAllUsers() { ... }
  
  @PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
  @PostMapping("/teacher/courses")
  public ResponseEntity<?> createCourse() { ... }
  ```

- **JWT Filter**: `JwtAuthenticationFilter` kiểm tra token trong mỗi request
  - Extract token từ header
  - Validate token (signature, expiration)
  - Load user details và set vào SecurityContext

### 2.3. Chức năng Quản lý Khóa học

#### 2.3.1. Tạo và Quản lý Khóa học (Giáo viên)

**Controller**: `TeacherCourseController`

**Các API chính**:
- `POST /api/teacher/courses`: Tạo khóa học mới
- `GET /api/teacher/courses`: Lấy danh sách khóa học của giáo viên
- `GET /api/teacher/courses/{courseId}`: Lấy chi tiết khóa học để chỉnh sửa
- `PUT /api/teacher/courses/{courseId}`: Cập nhật khóa học
- `POST /api/teacher/courses/{courseId}/image`: Upload ảnh bìa
- `PATCH /api/teacher/courses/{courseId}/submit-review`: Gửi khóa học để duyệt
- `PATCH /api/teacher/courses/{courseId}/hide`: Ẩn khóa học

**Quy trình tạo khóa học**:

1. **Bước 1: Tạo thông tin chung**
   - Tạo `Course` entity với thông tin cơ bản (title, description, category, objectives, targetAudience)
   - Status mặc định: `DRAFT`
   - Author: Giáo viên hiện tại (lấy từ JWT token)

2. **Bước 2: Upload ảnh bìa**
   - Upload file ảnh lên MinIO/S3
   - Resize ảnh về kích thước chuẩn (sử dụng Thumbnailator)
   - Lưu URL vào database

3. **Bước 3: Tạo cấu trúc khóa học**
   - Tạo `Section` (Chương): `POST /api/teacher/courses/{courseId}/sections`
   - Tạo `Lesson` (Bài học): `POST /api/teacher/courses/sections/{sectionId}/lessons`
   - Upload video/document: `POST /api/teacher/courses/lessons/upload/{lessonId}/video`
   - Tạo `Exam` (Bài kiểm tra): `POST /api/teacher/courses/sections/{sectionId}/exams`

4. **Bước 4: Gửi để duyệt**
   - Chuyển status từ `DRAFT` → `PENDING`
   - Gửi thông báo email cho Admin

**Cấu trúc dữ liệu**:
```
Course (Khóa học)
  ├── Section (Chương)
  │   ├── Lesson (Bài học)
  │   │   ├── Video URL (nếu lesson_type = VIDEO)
  │   │   └── Article Content (nếu lesson_type = ARTICLE)
  │   └── Exam (Bài kiểm tra)
  │       ├── Question (Câu hỏi)
  │       │   └── Answer (Đáp án)
```

#### 2.3.2. Xem và Tìm kiếm Khóa học (Công khai)

**Controller**: `CourseController`

**API Endpoints**:
- `GET /api/courses`: Lấy danh sách khóa học công khai (phân trang, lọc, sắp xếp)
- `GET /api/courses/{id}`: Lấy chi tiết khóa học
- `GET /api/courses/search?q={keyword}`: Tìm kiếm khóa học

**Tính năng**:
- **Phân trang**: Sử dụng Spring Data JPA `Pageable`
- **Lọc**: Theo category, title, authorName
- **Sắp xếp**: Nhiều trường, tăng/giảm dần
- **Chỉ hiển thị**: Khóa học có status `APPROVED` và `active = true`

**Code Example**:
```java
@GetMapping
public ResponseEntity<ApiResponse<PagedResponse<CourseListDto>>> getAllCourses(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) CourseCategory category,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String authorName,
        @RequestParam(defaultValue = "id,asc") String... sort) {
    
    PagedResponse<CourseListDto> courses = courseService.getAllPublicCourses(
            page, size, category, title, authorName, sort);
    return ResponseEntity.ok(ApiResponse.success(courses));
}
```

#### 2.3.3. Duyệt Khóa học (Admin)

**Controller**: `AdminController`

**API Endpoints**:
- `GET /api/admin/courses`: Lấy danh sách tất cả khóa học (kể cả chờ duyệt)
- `GET /api/admin/courses/{id}`: Lấy chi tiết khóa học
- `PATCH /api/admin/courses/{id}/status`: Duyệt hoặc từ chối khóa học
- `DELETE /api/admin/courses/{id}`: Xóa khóa học

**Quy trình duyệt**:
1. Admin xem danh sách khóa học có status `PENDING`
2. Xem chi tiết khóa học
3. Duyệt: Chuyển status `PENDING` → `APPROVED`
4. Từ chối: Chuyển status `PENDING` → `REJECTED`, nhập lý do từ chối
5. Gửi thông báo email cho giáo viên

### 2.4. Chức năng Đăng ký và Học tập

#### 2.4.1. Đăng ký Khóa học (Học viên)

**Controller**: `EnrollmentController`

**API Endpoints**:
- `POST /api/enrollments/{courseId}`: Đăng ký khóa học
- `GET /api/enrollments/course/{courseId}`: Kiểm tra trạng thái đăng ký

**Quy trình**:
1. Học viên chọn khóa học và click "Đăng ký"
2. Tạo `Enrollment` với status `PENDING`
3. Gửi thông báo email cho giáo viên
4. Giáo viên phê duyệt/từ chối đăng ký
5. Nếu được phê duyệt: status → `APPROVED`, học viên có thể bắt đầu học

**Ràng buộc**:
- Một học viên chỉ đăng ký một khóa học một lần (UNIQUE constraint)
- Chỉ học viên có enrollment `APPROVED` mới được xem nội dung khóa học

#### 2.4.2. Theo dõi Tiến độ Học tập

**Controller**: `StudentController`

**API Endpoints**:
- `GET /api/student/courses/{courseId}/progress`: Lấy tiến độ học tập
- `PATCH /api/student/lessons/{lessonId}/progress`: Cập nhật tiến độ bài học
- `GET /api/student/enrollments`: Lấy danh sách khóa học đã đăng ký

**Cơ chế**:
- `Enrollment.progress`: Float từ 0-100%, tính dựa trên số bài học đã hoàn thành
- `Enrollment.completedLessons`: JSON string chứa danh sách ID bài học đã hoàn thành
- Khi học viên hoàn thành bài học: Cập nhật `completedLessons` và tính lại `progress`

**Code Example**:
```java
@PatchMapping("/lessons/{lessonId}/progress")
public ResponseEntity<ApiResponse<CourseProgressResponse>> updateLessonProgress(
        @PathVariable Long lessonId,
        @RequestBody LessonProgressRequest request,
        Authentication authentication) {
    
    CourseProgressResponse response = enrollmentService.updateLessonProgress(
            lessonId, request, authentication.getName());
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 2.5. Chức năng Bài kiểm tra

#### 2.5.1. Tạo Bài kiểm tra (Giáo viên)

**Controller**: `TeacherCourseController`

**API Endpoint**: `POST /api/teacher/courses/sections/{sectionId}/exams`

**Thông tin bài kiểm tra**:
- Title, description
- Exam type: QUIZ, ASSIGNMENT, FINAL_EXAM
- Time limit (phút)
- Max attempts (số lần làm bài tối đa)

**Tạo câu hỏi và đáp án**:
- Mỗi bài kiểm tra có nhiều `Question`
- Mỗi câu hỏi có nhiều `Answer`
- Question type: SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
- Mỗi câu hỏi có điểm số (point)

#### 2.5.2. Làm Bài kiểm tra (Học viên)

**Controller**: `StudentExamController`

**API Endpoints**:
- `POST /api/student/exams/{examId}/start`: Bắt đầu làm bài
- `POST /api/student/exams/{examId}/submit`: Nộp bài

**Quy trình**:
1. **Bắt đầu làm bài**:
   - Kiểm tra enrollment status (phải `APPROVED`)
   - Kiểm tra số lần làm bài còn lại (dựa trên `ExamAttemptState`)
   - Kiểm tra cooldown (24h sau khi hết số lần làm)
   - Lưu `last_started_at` và tăng `attempts_used`
   - Trả về danh sách câu hỏi (không có đáp án đúng)

2. **Nộp bài**:
   - Kiểm tra thời gian làm bài (nếu có time limit)
   - Tính điểm dựa trên đáp án đúng
   - Lưu `Submission` với điểm số
   - Cập nhật `ExamAttemptState`
   - Nếu hết số lần làm: Set cooldown 24h

**Code Example**:
```java
@PostMapping("/{examId}/submit")
public ResponseEntity<ApiResponse<ExamSubmitResponseDto>> submit(
        @PathVariable Long examId,
        @RequestBody ExamSubmitRequestDto request,
        Authentication authentication) {
    
    // Tính điểm
    int score = 0;
    for (Question q : questions) {
        Set<Long> correct = q.getAnswers().stream()
            .filter(Answer::getIsCorrect)
            .map(Answer::getId)
            .collect(Collectors.toSet());
        Set<Long> chosen = submitted.get(q.getId());
        if (correct.equals(chosen)) {
            score += q.getPoint();
        }
    }
    
    // Lưu submission
    Submission submission = Submission.builder()
        .score(score)
        .user(user)
        .exam(exam)
        .build();
    submissionRepository.save(submission);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 2.6. Chức năng Tương tác

#### 2.6.1. Bình luận (Comments)

**Controller**: `CommentController`

**API Endpoints**:
- `GET /api/comments/lesson/{lessonId}`: Lấy bình luận của bài học
- `POST /api/comments/lesson/{lessonId}`: Tạo bình luận mới
- `PUT /api/comments/{commentId}`: Sửa bình luận
- `DELETE /api/comments/{commentId}`: Xóa bình luận

**Tính năng**:
- **Nested Comments**: Hỗ trợ bình luận lồng nhau (reply)
  - `Comment.parent_id`: Tham chiếu đến bình luận cha
  - `Comment.replies`: Danh sách bình luận con
- **Soft Delete**: Sử dụng `deleted_on` thay vì xóa vật lý
- **Phân trang**: Lấy bình luận theo page và size

#### 2.6.2. Đánh giá Khóa học (Reviews)

**Controller**: `UserController`

**API Endpoints**:
- `POST /api/users/review/course/{courseId}`: Tạo/update đánh giá
- `GET /api/users/review/course/{courseId}`: Lấy danh sách đánh giá

**Tính năng**:
- Rating: 1-5 sao
- Comment: Nội dung đánh giá
- Ràng buộc: Một học viên chỉ đánh giá một khóa học một lần (UNIQUE)
- Tự động cập nhật: `Course.averageRating` và `Course.totalReviews`

#### 2.6.3. Ghi chú Bài học (Lesson Notes)

**Controller**: `LessonNoteController`

**API Endpoints**:
- `GET /api/student/lessons/{lessonId}/note`: Lấy ghi chú
- `PUT /api/student/lessons/{lessonId}/note`: Tạo/sửa ghi chú

**Tính năng**:
- Mỗi học viên có một ghi chú cho mỗi bài học (UNIQUE constraint)
- Auto-save: Frontend có thể gọi API để lưu ghi chú tự động

#### 2.6.4. Điểm danh (Attendance)

**Controller**: `AttendanceController`

**API Endpoints**:
- `POST /api/student/attendance/checkin`: Điểm danh
- `GET /api/student/attendance?month={yyyy-MM}`: Lấy lịch điểm danh theo tháng

**Tính năng**:
- Một học viên chỉ điểm danh một lần mỗi ngày (UNIQUE constraint)
- Lưu `attended_date` (DATE) để theo dõi lịch sử

### 2.7. Chức năng Quản lý Người dùng

#### 2.7.1. Quản lý Người dùng (Admin)

**Controller**: `AdminController`

**API Endpoints**:
- `POST /api/admin/users`: Tạo người dùng mới
- `GET /api/admin/users`: Lấy danh sách người dùng (phân trang, lọc theo role)
- `GET /api/admin/users/{id}`: Lấy chi tiết người dùng
- `POST /api/admin/users/batch-create`: Tạo nhiều người dùng từ Excel

**Tính năng**:
- **Import từ Excel**: Sử dụng Apache POI để đọc file Excel
- **Phân quyền**: Chỉ Admin mới có quyền tạo và quản lý người dùng
- **Thống kê**: `GET /api/admin/statistics` - Thống kê số lượng người dùng theo role

#### 2.7.2. Quản lý Hồ sơ Cá nhân

**Controller**: `UserController`

**API Endpoints**:
- `PUT /api/users/edit-profile`: Cập nhật thông tin cá nhân
- `POST /api/users/{userId}/image`: Upload ảnh đại diện
- `PATCH /api/users/{id}/change-password`: Đổi mật khẩu

**Tính năng**:
- Upload ảnh đại diện lên MinIO/S3
- Resize ảnh về kích thước chuẩn
- Validation: Email phải unique, password phải đủ mạnh

### 2.8. Chức năng Thông báo

**Controller**: `NotificationController`

**API Endpoints**:
- `GET /api/notifications`: Lấy danh sách thông báo (phân trang)
- `PATCH /api/notifications/{id}/read`: Đánh dấu đã đọc
- `GET /api/notifications/unread-count`: Đếm số thông báo chưa đọc

**Tính năng**:
- **Real-time**: Sử dụng WebSocket để push thông báo mới
- **Tự động gửi**: Khi có sự kiện (duyệt khóa học, phê duyệt đăng ký, etc.)
- **Lưu trữ**: Lưu trong database `notifications` table

### 2.9. Chức năng Quản lý File

**Controller**: `FileController`

**API Endpoint**: `GET /api/files/{bucket}/{fileName}`

**Tính năng**:
- Serve files từ MinIO/S3
- Hỗ trợ nhiều loại file: images, videos, PDFs
- Caching headers để tối ưu hiệu suất

**Buckets**:
- `user-images`: Ảnh đại diện người dùng
- `course-images`: Ảnh bìa khóa học
- `lesson-videos`: Video bài học
- `lesson-documents`: Tài liệu bài học (PDF)

---

## PHẦN 3: TRIỂN KHAI CHATBOT AI

### 3.1. Tổng quan về Chatbot AI

Hệ thống tích hợp **Chatbot AI** sử dụng **Spring AI** framework để cung cấp trợ giảng AI cho học viên. Chatbot có khả năng trả lời câu hỏi về nội dung khóa học dựa trên dữ liệu đã được training từ nội dung khóa học.

### 3.2. Kiến trúc Chatbot AI

#### 3.2.1. Các Thành phần Chính

```
┌─────────────────────────────────────────────────────────┐
│              Frontend (ReactJS)                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Chat Interface                                  │  │
│  │  - Input message                                 │  │
│  │  - Display AI response                           │  │
│  │  - Chat history                                  │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ POST /api/ai/chat
                        │
┌───────────────────────▼─────────────────────────────────┐
│              AiController                               │
│  - Receive message from frontend                        │
│  - Extract courseId (optional)                          │
│  - Call AiService                                       │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│              AiService (AiServiceImpl)                   │
│  ┌──────────────────────────────────────────────────┐  │
│  │  1. Save user message to chat history            │  │
│  │  2. Vector Search (Similarity Search)            │  │
│  │  3. Get chat history (last 6 messages)           │  │
│  │  4. Build prompt with context                    │  │
│  │  5. Call AI (ChatClient)                         │  │
│  │  6. Save AI response to chat history             │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
│ Vector Store │ │ Chat History │ │ ChatClient │
│ (pgvector)   │ │  Service     │ │ (OpenAI)   │
└──────────────┘ └──────────────┘ └────────────┘
```

#### 3.2.2. Công nghệ Sử dụng

| Thành phần | Công nghệ | Mục đích |
|------------|-----------|----------|
| **Spring AI** | 1.0.0-M4 | Framework tích hợp AI |
| **OpenAI API** | - | LLM provider (GPT model) |
| **pgvector** | - | Vector database cho embeddings |
| **Vector Store** | Spring AI | Lưu trữ và tìm kiếm vector embeddings |
| **Chat History** | DynamoDB/In-memory | Lưu lịch sử chat |

### 3.3. Quy trình Training AI (Ingest Data)

#### 3.3.1. Mục đích

Training AI là quá trình chuyển đổi nội dung khóa học thành vector embeddings và lưu vào vector store để AI có thể tìm kiếm và trả lời câu hỏi dựa trên nội dung đó.

#### 3.3.2. API Endpoints

**Controller**: `AiController`

- `POST /api/ai/train/{courseId}`: Train AI cho một khóa học cụ thể
- `POST /api/ai/train/all`: Train AI cho tất cả khóa học đang active

**Quyền truy cập**: Chỉ `ADMIN` mới có quyền train AI

#### 3.3.3. Quy trình Training

**Bước 1: Lấy dữ liệu khóa học**

```java
Course course = courseRepository.findById(courseId)
    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
```

**Bước 2: Tạo Documents từ nội dung**

a) **Thông tin chung khóa học**:
```java
String courseInfo = """
    [LOẠI: THÔNG TIN KHÓA HỌC]
    Tên khóa: %s
    Mô tả: %s
    Mục tiêu: %s
    """.formatted(course.getTitle(), course.getDescription(), course.getObjectives());
    
Document doc = new Document(courseInfo, Map.of("courseId", courseId, "type", "info"));
```

b) **Nội dung bài học** (Smart Chunking):
```java
for (Section section : course.getSections()) {
    for (Lesson lesson : section.getLessons()) {
        String enrichedContent = """
            [KHÓA HỌC: %s]
            [CHƯƠNG: %s]
            [BÀI HỌC: %s]
            ----------------
            NỘI DUNG CHI TIẾT:
            %s
            """.formatted(
                course.getTitle(), 
                section.getTitle(), 
                lesson.getTitle(), 
                lesson.getArticleContent()
            );
        
        Document doc = new Document(enrichedContent, 
            Map.of("courseId", courseId, "type", "lesson"));
        documents.add(doc);
    }
}
```

**Kỹ thuật Smart Chunking**:
- Gắn context (tên khóa học, chương, bài học) vào mỗi chunk
- Giúp AI biết được thông tin đến từ đâu khi trả lời
- Metadata: `courseId`, `type` để filter khi search

**Bước 3: Chia nhỏ Documents (Text Splitting)**

```java
TokenTextSplitter splitter = new TokenTextSplitter();
List<Document> split = splitter.apply(documents);
```

- Chia nhỏ nội dung dài thành các chunks nhỏ hơn
- Mỗi chunk có kích thước phù hợp với model embedding
- Đảm bảo không mất thông tin quan trọng

**Bước 4: Lưu vào Vector Store**

```java
vectorStore.add(split);
```

- Tự động tạo embeddings cho mỗi document
- Lưu embeddings vào PostgreSQL với pgvector extension
- Metadata (courseId, type) được lưu kèm để filter
