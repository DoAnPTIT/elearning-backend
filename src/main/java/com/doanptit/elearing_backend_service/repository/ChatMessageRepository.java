package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByChatRoomIdAndDeletedFalseOrderByCreatedOnDesc(Long chatRoomId, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.deleted = false ORDER BY cm.createdOn ASC")
    List<ChatMessage> findAllByChatRoomIdOrderByCreatedOnAsc(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.deleted = false")
    Long countByChatRoomId(@Param("roomId") Long roomId);
}

