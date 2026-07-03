package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.AdminUserListDto;
import com.doanptit.elearing_backend_service.dto.res.BatchCreationResult;
import com.doanptit.elearing_backend_service.dto.res.StudentEnrollmentSummaryDto;
import com.doanptit.elearing_backend_service.dto.res.UploadImageResponse;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.UserMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Enrollment;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.repository.EnrollmentRepository;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.service.S3Service;
import com.doanptit.elearing_backend_service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final com.doanptit.elearing_backend_service.repository.CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public UserResponseDto createNewUserByAdmin(UserRequestDto request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new AppException(ErrorCode.USER_EMAIL_EXISTS);
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .dateOfBirth(request.getDateOfBirth())
                .role(Role.valueOf(request.getRole()))
                .active(request.isActive())
                .build();

        User savedUser = userRepository.save(newUser);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto findUserById(Long id) {
        return null;
    }

    @Override
    public UserResponseDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        UserResponseDto dto = userMapper.toUserResponseDto(user);
        
        // Add statistics based on role
        if (user.getRole() == Role.TEACHER) {
            var teacherCoursesPage = courseRepository.findByAuthor_Email(user.getEmail(), Pageable.unpaged());

            // Count courses authored by this teacher
            long courseCount = teacherCoursesPage.getTotalElements();
            dto.setCourseCount(courseCount);
            
            // Count total enrollments in teacher's courses
            long totalEnrollments = teacherCoursesPage.getContent()
                    .stream()
                    .mapToLong(course -> enrollmentRepository.findByCourse_Id(course.getId(), Pageable.unpaged()).getTotalElements())
                    .sum();
            dto.setStudentCount(totalEnrollments);

            dto.setRating(calculateTeacherAverageRating(teacherCoursesPage.getContent()));
        } else if (user.getRole() == Role.STUDENT) {
            StudentLearningSnapshot snapshot = computeStudentLearningSnapshot(user);
            dto.setCourseNames(snapshot.getCourseNames());
            dto.setEnrolledCourses((long) snapshot.getEnrolledCount());
            dto.setCompletedCourses((long) snapshot.getCompletedCount());
            dto.setProgress(snapshot.getAverageProgress());
            dto.setLastActivity(snapshot.getLastActivity());
            dto.setEnrollments(snapshot.getEnrollmentSummaries());
        }
        
        return dto;
    }

    @Override
    public Page<UserResponseDto> findAllUsers(int pageNo, int pageSize, String... sorts) {
        List<Sort.Order> orders = new ArrayList<>();
        if(sorts!=null){
            for(String sortBy: sorts){
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if(matcher.find()){
                    if(matcher.group(3).equalsIgnoreCase("asc")){
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    }else{
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }
        Pageable pageable = PageRequest.of(pageNo,pageSize,Sort.by(orders));
        return this.userRepository.findAll(pageable).map(userMapper::toUserResponseDto);
    }

    @Override
    public Page<AdminUserListDto> findAllUsersForAdmin(int pageNo, int pageSize, String role, 
                                                        String name, String email, String courseName, Boolean active, String... sorts) {
        // --- 1️⃣ Chuẩn hóa paging ---
        if (pageNo < 0) pageNo = 0;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        // --- 2️⃣ Xử lý sort động ---
        List<String> allowedFields = List.of("id", "email", "firstname", "lastname", "createdOn", "updatedOn");
        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null && sorts.length > 0) {
            for (String sortBy : sorts) {
                // Support both "field,asc" and "field:asc" formats
                String[] parts = sortBy.contains(",") ? sortBy.split(",") : sortBy.split(":");
                if (parts.length >= 2) {
                    String field = parts[0].trim();
                    String direction = parts[1].trim();
                    if (allowedFields.contains(field)) {
                        if (direction.equalsIgnoreCase("asc")) {
                            orders.add(Sort.Order.asc(field));
                        } else if (direction.equalsIgnoreCase("desc")) {
                            orders.add(Sort.Order.desc(field));
                        }
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc("id"));
        }

        Sort sortSpec = Sort.by(orders);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortSpec);

        // --- 3️⃣ Tạo Specification ---
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by role
            if (role != null && !role.isEmpty()) {
                try {
                    Role roleEnum = Role.valueOf(role.toUpperCase());
                    predicates.add(cb.equal(root.get("role"), roleEnum));
                } catch (IllegalArgumentException e) {
                    // Invalid role, ignore filter
                }
            }

            // Filter by name (firstname or lastname)
            if (name != null && !name.isBlank()) {
                String pattern = "%" + name.toLowerCase() + "%";
                Predicate matchFirst = cb.like(cb.lower(root.get("firstname")), pattern);
                Predicate matchLast = cb.like(cb.lower(root.get("lastname")), pattern);
                // Also search in concatenated full name
                Predicate matchFull = cb.like(
                    cb.lower(cb.concat(
                        cb.concat(cb.coalesce(root.get("firstname"), ""), " "),
                        cb.coalesce(root.get("lastname"), "")
                    )), pattern
                );
                predicates.add(cb.or(matchFirst, matchLast, matchFull));
            }

            // Filter by email
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            // Filter by active status
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            // Filter by course name (join with enrollments -> course)
            if (courseName != null && !courseName.isBlank()) {
                Join<User, Enrollment> enrollments = root.join("enrollments", JoinType.LEFT);
                Join<Enrollment, Course> course = enrollments.join("course", JoinType.LEFT);
                String pattern = "%" + courseName.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(course.get("title")), pattern));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // --- 4️⃣ Truy vấn ---
        Page<User> userPage = userRepository.findAll(spec, pageable);

        // --- 5️⃣ Map to DTO ---
        return userPage.map(this::mapUserToAdminUserListDto);
    }
    
    private AdminUserListDto mapUserToAdminUserListDto(User user) {
        AdminUserListDto dto = new AdminUserListDto();
        dto.setId(user.getId().intValue());
        dto.setEmail(user.getEmail());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setRole(user.getRole());
        dto.setImage(user.getImage());
        dto.setActive(user.getActive());
        
        // For teachers, query courses by author and calculate stats
        if (user.getRole() == Role.TEACHER) {
            // Get all courses created by this teacher
            List<com.doanptit.elearing_backend_service.model.Course> teacherCourses = 
                    courseRepository.findByAuthor_Email(user.getEmail(), Pageable.unpaged()).getContent();
            
            dto.setCourseCount(teacherCourses.size());
            
            // Count unique students from all enrollments across all teacher's courses
            long studentCount = teacherCourses.stream()
                    .flatMap(course -> course.getEnrollments() != null ? course.getEnrollments().stream() : java.util.stream.Stream.empty())
                    .filter(enrollment -> enrollment.getStatus() == com.doanptit.elearing_backend_service.enums.EnrollmentStatus.APPROVED)
                    .map(enrollment -> enrollment.getUser().getId())
                    .distinct()
                    .count();
            dto.setStudentCount((int) studentCount);

            dto.setRating(calculateTeacherAverageRating(teacherCourses));
        }
        
        // For students, derive enrollment stats/progress
        if (user.getRole() == Role.STUDENT) {
            StudentLearningSnapshot snapshot = computeStudentLearningSnapshot(user);
            dto.setCourseNames(snapshot.getCourseNames());
            dto.setEnrolledCourses(snapshot.getEnrolledCount());
            dto.setCompletedCourses(snapshot.getCompletedCount());
            dto.setProgress(snapshot.getAverageProgress());
            dto.setLastActivity(snapshot.getLastActivity());
        }
        
        return dto;
    }

    private double calculateTeacherAverageRating(List<com.doanptit.elearing_backend_service.model.Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        long totalReviews = 0L;

        for (var course : courses) {
            if (course == null) {
                continue;
            }
            Double avg = course.getAverageRating();
            Integer count = course.getTotalReviews();

            double avgValue = avg != null ? avg : 0.0;
            long countValue = count != null ? count.longValue() : 0L;
            if (countValue <= 0) {
                continue;
            }

            weightedSum += avgValue * countValue;
            totalReviews += countValue;
        }

        if (totalReviews <= 0) {
            return 0.0;
        }

        double raw = weightedSum / totalReviews;
        return Math.round(raw * 10.0) / 10.0;
    }

    @Override
    public void changePassword(Integer id, String email, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Đảm bảo user chỉ đổi mật khẩu của chính mình
        if (!user.getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATE);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        System.out.println("=== UPDATE PROFILE DEBUG ===");
        System.out.println("Email: " + email);
        System.out.println("Request dateOfBirth: " + request.getDateOfBirth());
        System.out.println("User before update - dateOfBirth: " + user.getDateOfBirth());

        userMapper.updateUserFromDto(user, request);

        System.out.println("User after mapper - dateOfBirth: " + user.getDateOfBirth());

        User savedUser = userRepository.save(user);

        System.out.println("User after save - dateOfBirth: " + savedUser.getDateOfBirth());
        System.out.println("=== END DEBUG ===");

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public BatchCreationResult createUsersFromExcel(MultipartFile file, String roleStr) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.trim().toUpperCase());
            if (role != Role.STUDENT && role != Role.TEACHER) {
                throw new AppException(ErrorCode.INVALID_ROLE);
            }
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        BatchCreationResult result = new BatchCreationResult();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (rows.hasNext()) rows.next();

            int rowNumber = 1;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNumber++;
                try {
                    String firstname = getCellValueAsString(currentRow.getCell(0));
                    String lastname = getCellValueAsString(currentRow.getCell(1));
                    String email = getCellValueAsString(currentRow.getCell(2));
                    String dobString = getCellValueAsString(currentRow.getCell(3));

                    if (email == null || email.isBlank()) {
                        throw new IllegalArgumentException("Email is required.");
                    }
                    if (userRepository.findByEmail(email).isPresent()) {
                       throw new AppException(ErrorCode.USER_EMAIL_EXISTS);
                    }

                    LocalDate dob = parseDate(dobString);

                    // Mật khẩu mặc định = ngày sinh (định dạng ddMMyyyy)
                    String defaultPassword = dob != null
                            ? dob.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                            : "12345678";
                    String encodedPassword = passwordEncoder.encode(defaultPassword);

                    User newUser = User.builder()
                            .firstname(firstname)
                            .lastname(lastname)
                            .email(email)
                            .password(encodedPassword)
                            .role(role)
                            .active(true)
                            .dateOfBirth(dob)
                            .build();

                    userRepository.save(newUser);
                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String errorMessage = "Error at row " + rowNumber + ": " + e.getMessage();
                    result.getErrorMessages().add(errorMessage);
                }
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
        return result;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.format("%d", (long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";

            default:
                return null;
        }
    }

    private LocalDate parseDate(String dobString) {
        if (dobString == null || dobString.isBlank()) {
            return null;
        }

        // Trường hợp Excel lưu ngày dưới dạng số (ví dụ: 45230)
        try {
            double numericValue = Double.parseDouble(dobString);
            return LocalDate.of(1900, 1, 1).plusDays((long) numericValue - 2);
        } catch (NumberFormatException ignored) {}

        // Các định dạng chuỗi phổ biến
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dobString, formatter);
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ: " + dobString);
    }

    @Override
    @Transactional
    public ApiResponse<UploadImageResponse> uploadUserImage(Integer userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Xóa ảnh cũ nếu có
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            s3Service.deleteUserImage(userId);
        }

        try {
            String imageUrl = s3Service.uploadUserImage(userId, file);
            String versionedImageUrl = imageUrl + "?t=" + System.currentTimeMillis();
            user.setImage(versionedImageUrl);
            userRepository.save(user);

            UploadImageResponse response = new UploadImageResponse(versionedImageUrl);
            return ApiResponse.success("Tải ảnh thành công", response);

        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE);
        }
    }

        private StudentLearningSnapshot computeStudentLearningSnapshot(User user) {
        if (user == null) {
            return StudentLearningSnapshot.empty();
        }

        List<Enrollment> enrollments = Optional.ofNullable(user.getEnrollments())
            .orElse(Collections.emptyList());

        List<Enrollment> approvedEnrollments = enrollments.stream()
            .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.APPROVED)
            .toList();

        List<String> courseNames = approvedEnrollments.stream()
            .map(Enrollment::getCourse)
            .filter(Objects::nonNull)
            .map(Course::getTitle)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        int enrolledCount = approvedEnrollments.size();

        int completedCourses = (int) approvedEnrollments.stream()
            .map(Enrollment::getProgress)
            .filter(Objects::nonNull)
            .filter(progress -> progress >= 99.9f)
            .count();

        double averageProgress = approvedEnrollments.stream()
            .map(Enrollment::getProgress)
            .filter(Objects::nonNull)
            .mapToDouble(Float::doubleValue)
            .average()
            .orElse(0.0d);

        String lastActivity = approvedEnrollments.stream()
            .map(Enrollment::getUpdatedOn)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .map(LocalDateTime::toString)
            .orElse(null);

        List<StudentEnrollmentSummaryDto> enrollmentSummaries = approvedEnrollments.stream()
            .map(this::mapStudentEnrollmentSummary)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new StudentLearningSnapshot(
            courseNames,
            enrolledCount,
            completedCourses,
            averageProgress,
            lastActivity,
            enrollmentSummaries
        );
        }

    private StudentEnrollmentSummaryDto mapStudentEnrollmentSummary(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        Course course = enrollment.getCourse();
        String teacherName = Optional.ofNullable(course)
                .map(Course::getAuthor)
                .map(author -> (Optional.ofNullable(author.getFirstname()).orElse("") + " "
                        + Optional.ofNullable(author.getLastname()).orElse("")).trim())
                .filter(name -> !name.isBlank())
                .orElse(null);

        String lastActivity = Optional.ofNullable(enrollment.getUpdatedOn())
                .map(LocalDateTime::toString)
                .orElse(null);

        return StudentEnrollmentSummaryDto.builder()
                .enrollmentId(enrollment.getId())
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .courseImage(course != null ? course.getImage() : null)
                .progress(enrollment.getProgress())
                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : null)
                .teacherName(teacherName)
                .lastActivity(lastActivity)
                .build();
    }

    private static class StudentLearningSnapshot {
        private final List<String> courseNames;
        private final int enrolledCount;
        private final int completedCount;
        private final double averageProgress;
        private final String lastActivity;
        private final List<StudentEnrollmentSummaryDto> enrollmentSummaries;

        private StudentLearningSnapshot(List<String> courseNames,
                                        int enrolledCount,
                                        int completedCount,
                                        double averageProgress,
                                        String lastActivity,
                                        List<StudentEnrollmentSummaryDto> enrollmentSummaries) {
            this.courseNames = courseNames;
            this.enrolledCount = enrolledCount;
            this.completedCount = completedCount;
            this.averageProgress = averageProgress;
            this.lastActivity = lastActivity;
            this.enrollmentSummaries = enrollmentSummaries;
        }

        private static StudentLearningSnapshot empty() {
            return new StudentLearningSnapshot(Collections.emptyList(), 0, 0, 0.0d, null, Collections.emptyList());
        }

        public List<String> getCourseNames() {
            return courseNames;
        }

        public int getEnrolledCount() {
            return enrolledCount;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public double getAverageProgress() {
            return averageProgress;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public List<StudentEnrollmentSummaryDto> getEnrollmentSummaries() {
            return enrollmentSummaries;
        }
    }

    @Override
    @Transactional
    public void completeMySurvey(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (Boolean.TRUE.equals(user.getHasCompletedSurvey())) {
            return;
        }
        user.setHasCompletedSurvey(true);
        userRepository.save(user);
    }
}
