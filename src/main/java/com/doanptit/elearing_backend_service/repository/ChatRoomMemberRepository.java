package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    
    Optional<ChatRoomMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    
    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);
    
    List<ChatRoomMember> findByUserId(Long userId);
    
    @Query("SELECT crm FROM ChatRoomMember crm WHERE crm.chatRoom.id = :roomId AND crm.isOnline = true")
    List<ChatRoomMember> findOnlineMembersByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT crm.user.id FROM ChatRoomMember crm WHERE crm.chatRoom.id = :roomId AND crm.isOnline = true")
    List<Long> findOnlineUserIdsByRoomId(@Param("roomId") Long roomId);
}

