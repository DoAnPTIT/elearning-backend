package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.AdminUserListDto;
import com.doanptit.elearing_backend_service.dto.res.BatchCreationResult;
import com.doanptit.elearing_backend_service.dto.res.UploadImageResponse;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.UserMapper;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final com.doanptit.elearing_backend_service.repository.CourseRepository courseRepository;

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
        return userMapper.toUserResponseDto(user);
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
    public Page<AdminUserListDto> findAllUsersForAdmin(int pageNo, int pageSize, String role, String... sorts) {
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
        
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(orders));
        Page<User> userPage;
        
        // Filter by role if provided
        if (role != null && !role.isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                userPage = userRepository.findByRole(roleEnum, pageable);
            } catch (IllegalArgumentException e) {
                userPage = userRepository.findAll(pageable);
            }
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
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
            
            // Calculate average rating (placeholder - need actual rating system)
            dto.setRating(0.0);
        }
        
        // For students, get approved course names
        if (user.getRole() == Role.STUDENT && user.getEnrollments() != null) {
            List<String> approvedCourseNames = user.getEnrollments().stream()
                    .filter(enrollment -> enrollment.getStatus() == com.doanptit.elearing_backend_service.enums.EnrollmentStatus.APPROVED)
                    .map(enrollment -> enrollment.getCourse().getTitle())
                    .toList();
            dto.setCourseNames(approvedCourseNames);
            dto.setEnrolledCourses(approvedCourseNames.size());
            
            // TODO: Calculate completed courses and progress
            dto.setCompletedCourses(0);
            dto.setProgress(0.0);
        }
        
        return dto;
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
            user.setImage(imageUrl);
            userRepository.save(user);

            UploadImageResponse response = new UploadImageResponse(imageUrl);
            return ApiResponse.success("Tải ảnh thành công", response);

        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE);
        }
    }
}
