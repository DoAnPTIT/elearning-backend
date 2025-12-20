package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ReviewRequestDto;
import com.doanptit.elearing_backend_service.dto.res.ReviewResponseDto;

public interface ReviewService {
    ReviewResponseDto createOrUpdateReview(Long courseId, String userEmail, ReviewRequestDto request);

    PagedResponse<ReviewResponseDto> getCourseReviews(Long courseId, int page, int size);
}
