package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ReviewRequestDto;
import com.doanptit.elearing_backend_service.dto.res.ReviewResponseDto;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Review;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.EnrollmentRepository;
import com.doanptit.elearing_backend_service.repository.ReviewRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public ReviewResponseDto createOrUpdateReview(Long courseId, String userEmail, ReviewRequestDto request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        EnrollmentStatus enrollment = enrollmentRepository.findEnrollmentStatus(user.getEmail(), courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED));

        Review review = reviewRepository.findByCourseIdAndUserId(courseId, user.getId())
                .orElse(new Review());

        if (review.getId() == null) {
            review.setCourse(course);
            review.setUser(user);
            review.setCreatedBy(user.getId());
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUpdatedBy(user.getId());

        Review savedReview = reviewRepository.save(review);

        updateCourseRatingSummary(course);

        return mapToDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDto> getCourseReviews(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedOn").descending());

        Page<Review> reviewPage = reviewRepository.findByCourseId(courseId, pageable);

        List<ReviewResponseDto> content = reviewPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }

    private void updateCourseRatingSummary(Course course) {
        Double avg = reviewRepository.calculateAverageRating(course.getId());
        long count = reviewRepository.countByCourseId(course.getId());

        double finalAvg = (avg == null) ? 0.0 : avg;

        double roundedAvg = Math.round(finalAvg * 10.0) / 10.0;

        course.setAverageRating(roundedAvg);
        course.setTotalReviews((int) count);

        courseRepository.save(course);
    }

    private ReviewResponseDto mapToDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .userName(review.getUser().getFirstname() + " " + review.getUser().getLastname())
                .userAvatar(review.getUser().getImage())
                .createdAt(review.getUpdatedOn() != null ? review.getUpdatedOn() : review.getCreatedOn())
                .build();
    }
}
