package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.PublicCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.PublicCourseService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCourseServiceImpl implements PublicCourseService {

    private final CourseRepository courseRepository;
    private final PublicCourseMapper publicCourseMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PublicCourseListDto> getAllPublicCourses(
            int page, int size, CourseCategory category,
            String title, String authorName, String... sort) {

        // --- 1️⃣ Chuẩn hóa paging  ---
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        // --- 2️⃣ Xử lý sort động  ---
        List<String> allowedFields = List.of("id", "title", "category", "createdOn"); // (Bỏ 'status' vì chỉ có 1 status)
        List<Sort.Order> orders = new ArrayList<>();
        // (Copy y hệt logic 'for' của AdminServiceImpl để xử lý sort...)
        if (sort != null && sort.length > 0) {
            for (int i = 0; i < sort.length; i++) {
                String raw = sort[i];
                if (raw == null) continue;
                raw = raw.trim();
                if (raw.isBlank()) continue;
                if (raw.contains(",")) {
                    String[] parts = raw.split(",", 2);
                    String field = parts[0].trim();
                    String direction = parts.length == 2 ? parts[1].trim() : "asc";
                    if (!allowedFields.contains(field)) continue;
                    orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                    continue;
                }
                String next = (i + 1) < sort.length ? sort[i + 1] : null;
                if (next != null) {
                    next = next.trim();
                    if (next.equalsIgnoreCase("asc") || next.equalsIgnoreCase("desc")) {
                        String field = raw;
                        String direction = next;
                        if (allowedFields.contains(field)) {
                            orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                        }
                        i++;
                        continue;
                    }
                }
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

            // --- ĐIỀU KIỆN BẮT BUỘC CỦA API NÀY ---
            predicates.add(cb.equal(root.get("status"), CourseStatus.ACTIVE));
            // ---

            // Lọc (Filter)
            if (category != null) predicates.add(cb.equal(root.get("category"), category));

            // Tìm kiếm (Search)
            if (title != null && !title.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));

            if (authorName != null && !authorName.isBlank()) {
                Join<Course, User> author = root.join("author", JoinType.LEFT);
                String pattern = "%" + authorName.toLowerCase() + "%";
                Predicate matchFirst = cb.like(cb.lower(author.get("firstname")), pattern);
                Predicate matchLast = cb.like(cb.lower(author.get("lastname")), pattern);
                // (Bỏ matchEmail nếu không muốn public)
                predicates.add(cb.or(matchFirst, matchLast));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // --- 4️⃣ Truy vấn ---
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        // --- 5️⃣ Map dùng PublicMapper ---
        Page<PublicCourseListDto> dtoPage = coursePage.map(publicCourseMapper::toCourseListDto);

        // --- 6️⃣ Trả kết quả ---
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
    public PublicCourseDetailDto getPublicCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // --- ĐIỀU KIỆN BẮT BUỘC ---
        // Nếu khóa học không được duyệt, thì coi như không tìm thấy (404)
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        return publicCourseMapper.toCourseDetailDto(course);
    }
}