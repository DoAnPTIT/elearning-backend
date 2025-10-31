package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.AdminCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.AdminCourseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseRepository courseRepository;
    private final AdminCourseMapper adminCourseMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminCourseListDto> getAllCourses(int page, int size, CourseStatus status, String... sort) {

        // 1. Xây dựng logic Sort (Phiên bản an toàn nhất)
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && sort.length > 0) {
            for (String sortOrder : sort) {

                if (sortOrder == null || sortOrder.trim().isEmpty()) continue;

                String[] parts = sortOrder.split(",");
                String field = parts[0].trim();

                // Bỏ qua nếu field rỗng
                if (field.isEmpty()) continue;

                // --- ĐÂY LÀ PHẦN SỬA LỖI ---
                // Bỏ qua nếu "field" là 'asc' hoặc 'desc' (trường hợp bị truyền nhầm)
                if (field.equalsIgnoreCase("asc") || field.equalsIgnoreCase("desc")) {
                    continue;
                }
                // --- KẾT THÚC PHẦN SỬA ---

                // Nếu có 2 phần (field,direction)
                if (parts.length == 2 && parts[1] != null && !parts[1].trim().isEmpty()) {
                    Sort.Direction direction = parts[1].trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, field));
                } else {
                    // Nếu chỉ có 1 phần (chỉ field)
                    orders.add(new Sort.Order(Sort.Direction.ASC, field));
                }
            }
        }

        // Mặc định sort theo 'id' nếu không có tham số sort nào hợp lệ
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        // 2. Xây dựng logic Filter (Specification)
        Specification<Course> spec = (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("status"), status);
            }
            return null; // Trả về null (không lọc) là an toàn nhất
        };

        // 3. Gọi Repository
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        // 4. Map sang DTO
        Page<AdminCourseListDto> dtoPage = coursePage.map(adminCourseMapper::toCourseListDto);

        // 5. THAY ĐỔI: Chuyển Page -> PagedResponse (của bạn)
        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),      // page
                dtoPage.getSize(),        // size
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCourseDetailDto getCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return adminCourseMapper.toCourseDetailDto(course);
    }

    @Override
    @Transactional
    public AdminCourseDetailDto updateCourseStatus(Long courseId, AdminUpdateCourseStatusDto request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_REVIEW); // (Tạo lỗi này)
        }

        course.setStatus(request.getStatus());

        if (request.getStatus() == CourseStatus.REJECTED) {
            course.setRejectionReason(request.getRejectionReason());
        } else {
            course.setRejectionReason(null);
        }

        return getCourseDetails(courseId);
    }
}