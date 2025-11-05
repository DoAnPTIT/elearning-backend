package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.AdminCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.AdminCourseService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseRepository courseRepository;
    private final AdminCourseMapper adminCourseMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminCourseListDto> getAllCourses(
            int page, int size, CourseStatus status, CourseCategory category,
            String title, String authorName, String... sort
    ) {
        // --- 1️⃣ Chuẩn hóa paging ---
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        // --- 2️⃣ Xử lý sort động (hỗ trợ cả "title,desc" và ["title","desc"]) ---
        List<String> allowedFields = List.of("id", "title", "status", "category", "createdOn");
        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null && sort.length > 0) {
            // Dùng index để có thể "kết hợp" phần tử khi Spring tách thành ["field","desc"]
            for (int i = 0; i < sort.length; i++) {
                String raw = sort[i];
                if (raw == null) continue;
                raw = raw.trim();
                if (raw.isBlank()) continue;

                // Nếu raw chứa dấu phẩy => đúng định dạng "field,direction"
                if (raw.contains(",")) {
                    String[] parts = raw.split(",", 2);
                    String field = parts[0].trim();
                    String direction = parts.length == 2 ? parts[1].trim() : "asc";
                    if (!allowedFields.contains(field)) continue;
                    orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                    continue;
                }

                // Nếu không chứa dấu phẩy, có thể Spring đã tách thành ["field","desc"]
                String next = (i + 1) < sort.length ? sort[i + 1] : null;
                if (next != null) {
                    next = next.trim();
                    if (next.equalsIgnoreCase("asc") || next.equalsIgnoreCase("desc")) {
                        String field = raw;
                        String direction = next;
                        if (allowedFields.contains(field)) {
                            orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                        }
                        i++; // đã tiêu thụ phần next
                        continue;
                    }
                }

                // Nếu đến đây: raw không chứa comma và không kèm hướng; mặc định ASC
                String field = raw;
                if (!allowedFields.contains(field)) continue;
                orders.add(Sort.Order.asc(field));
            }
        }

        Sort sortSpec = orders.isEmpty()
                ? Sort.by(Sort.Order.desc("id"))
                : Sort.by(orders);

        Pageable pageable = PageRequest.of(page, size, sortSpec);

        // --- 3️⃣ Tạo Specification ---
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (title != null && !title.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));

            if (authorName != null && !authorName.isBlank()) {
                Join<Course, User> author = root.join("author", JoinType.LEFT);
                String pattern = "%" + authorName.toLowerCase() + "%";
                Predicate matchFirst = cb.like(cb.lower(author.get("firstname")), pattern);
                Predicate matchLast = cb.like(cb.lower(author.get("lastname")), pattern);
                Predicate matchEmail = cb.like(cb.lower(author.get("email")), pattern);
                predicates.add(cb.or(matchFirst, matchLast, matchEmail));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // --- 4️⃣ Truy vấn ---
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        Page<AdminCourseListDto> dtoPage = coursePage.map(adminCourseMapper::toCourseListDto);

        // --- 5️⃣ Trả kết quả ---
        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
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
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_REVIEW);
        }

        course.setStatus(request.getStatus());
        course.setRejectionReason(
                request.getStatus() == CourseStatus.REJECTED
                        ? request.getRejectionReason()
                        : null
        );

        return getCourseDetails(courseId);
    }
}
