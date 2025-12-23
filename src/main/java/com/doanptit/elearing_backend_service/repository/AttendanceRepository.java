package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a.attendedDate FROM Attendance a WHERE a.user.email = :email AND a.attendedDate BETWEEN :start AND :end")
    List<LocalDate> findAttendedDatesInRange(String email, LocalDate start, LocalDate end);

    Optional<Attendance> findByUser_EmailAndAttendedDate(String email, LocalDate attendedDate);
}


