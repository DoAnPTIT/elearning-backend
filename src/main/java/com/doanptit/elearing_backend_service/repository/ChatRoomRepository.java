package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.ChatRoom;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByCourseId(Long courseId);
    
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN cr.roomMembers crm WHERE crm.user.id = :userId AND cr.active = true AND crm.active = true")
    List<ChatRoom> findByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.course.id = :courseId AND cr.active = true")
    Optional<ChatRoom> findActiveByCourseId(@Param("courseId") Long courseId);
}

