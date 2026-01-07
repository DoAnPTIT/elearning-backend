package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ChatMessageRequest;
import com.doanptit.elearing_backend_service.dto.res.ChatMessageResponse;
import com.doanptit.elearing_backend_service.dto.res.ChatMemberResponse;
import com.doanptit.elearing_backend_service.dto.res.ChatRoomResponse;

import java.util.List;

public interface ChatService {
    
    /**
     * Tự động tạo chat room cho khóa học (nếu chưa có)
     */
    ChatRoomResponse getOrCreateChatRoomForCourse(Long courseId, String userEmail);
    
    /**
     * Lấy danh sách chat rooms của user
     */
    List<ChatRoomResponse> getUserChatRooms(String userEmail);
    
    /**
     * Lấy thông tin chat room
     */
    ChatRoomResponse getChatRoom(Long roomId, String userEmail);
    
    /**
     * Gửi tin nhắn
     */
    ChatMessageResponse sendMessage(Long roomId, ChatMessageRequest request, String userEmail);
    
    /**
     * Lấy lịch sử tin nhắn
     */
    PagedResponse<ChatMessageResponse> getMessages(Long roomId, int page, int size, String userEmail);
    
    /**
     * Thêm thành viên vào chat room (chỉ teacher/admin)
     */
    void addMemberToRoom(Long roomId, Long userId, String requesterEmail);
    
    /**
     * Xóa thành viên khỏi chat room (chỉ teacher/admin)
     */
    void removeMemberFromRoom(Long roomId, Long userId, String requesterEmail);
    
    /**
     * Xóa tin nhắn
     */
    void deleteMessage(Long messageId, String userEmail);
    
    /**
     * Cập nhật online status
     */
    void updateOnlineStatus(Long roomId, String userEmail, boolean isOnline);
    
    /**
     * Lấy danh sách thành viên online
     */
    List<Long> getOnlineMembers(Long roomId);

    /**
     * Lấy danh sách chi tiết thành viên trong room
     */
    List<ChatMemberResponse> getMemberDetails(Long roomId, String requesterEmail);
}

