package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.ReviewRequestDto;
import com.doanptit.elearing_backend_service.dto.req.SurveyRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.res.ReviewResponseDto;
import com.doanptit.elearing_backend_service.dto.res.UploadImageResponse;
import com.doanptit.elearing_backend_service.dto.res.UserPreferenceResponse;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.service.ReviewService;
import com.doanptit.elearing_backend_service.service.UserPreferenceService;
import com.doanptit.elearing_backend_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final UserPreferenceService userPreferenceService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Integer id,
            @RequestBody @Valid ChangePasswordRequest request,
            Principal principal) {
        userService.changePassword(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi mật khẩu thành công"));
    }

    @PutMapping("/edit-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        UserResponseDto updatedUser = userService.updateProfile(email, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", updatedUser));
    }

    @PostMapping(value = "/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadImageResponse>> uploadUserImage(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file
    ) {
        ApiResponse<UploadImageResponse> response = userService.uploadUserImage(userId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete survey with preferences (new version - saves preferences).
     */
    @PostMapping("/me/survey")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> completeSurveyWithPreferences(
            Authentication authentication,
            @Valid @RequestBody SurveyRequest request) {
        UserPreferenceResponse preferences = userPreferenceService.saveSurveyPreferences(
                authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu khảo sát và sở thích học tập.", preferences));
    }

    /**
     * Legacy endpoint - just marks survey as complete without preferences.
     */
    @PatchMapping("/me/survey")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Boolean>> completeMySurvey(Authentication authentication) {
        userService.completeMySurvey(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã lưu khảo sát.", true));
    }

    /**
     * Get current user's learning preferences.
     */
    @GetMapping("/me/preferences")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getMyPreferences(Authentication authentication) {
        UserPreferenceResponse preferences = userPreferenceService.getPreferences(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    // api: rating va danh gia khoa hoc
    @PostMapping("/review/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> submitReview(
            @PathVariable Long courseId,
            @RequestBody @Valid ReviewRequestDto request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        ReviewResponseDto response = reviewService.createOrUpdateReview(courseId, userEmail, request);

        return ResponseEntity.ok(ApiResponse.success("Cảm ơn bạn đã đánh giá khóa học!", response));
    }


     //API: Xem danh sách đánh giá của khóa học
    @GetMapping("/review/course/{courseId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponseDto>>> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ReviewResponseDto> response = reviewService.getCourseReviews(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}