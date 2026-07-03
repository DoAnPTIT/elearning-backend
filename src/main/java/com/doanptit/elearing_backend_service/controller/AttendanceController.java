package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.model.Attendance;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.AttendanceRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AttendanceController {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAttendanceForMonth(
            @RequestParam String month,
            Authentication authentication
    ) {
        YearMonth ym = YearMonth.parse(month); // yyyy-MM
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<LocalDate> dates = attendanceRepository.findAttendedDatesInRange(
                authentication.getName(), start, end
        );
        return ResponseEntity.ok(ApiResponse.success(dates));
    }

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<Boolean>> checkin(Authentication authentication) {
        LocalDate today = LocalDate.now();
        boolean exists = attendanceRepository.findByUser_EmailAndAttendedDate(authentication.getName(), today).isPresent();
        if (exists) {
            return ResponseEntity.ok(ApiResponse.success("Bạn đã điểm danh hôm nay.", true));
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Attendance attendance = Attendance.builder()
                .user(user)
                .attendedDate(today)
                .build();
        attendanceRepository.save(attendance);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Điểm danh thành công.", true));
    }
}


