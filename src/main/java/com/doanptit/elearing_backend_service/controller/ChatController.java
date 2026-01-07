package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ChatMessageRequest;
import com.doanptit.elearing_backend_service.dto.res.ChatMessageResponse;
import com.doanptit.elearing_backend_service.dto.res.ChatRoomResponse;
import com.doanptit.elearing_backend_service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Lấy hoặc tạo chat room cho khóa học
     */
    @GetMapping("/rooms/course/{courseId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateChatRoom(
            @PathVariable Long courseId,
            Authentication authentication) {
        ChatRoomResponse room = chatService.getOrCreateChatRoomForCourse(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Lấy danh sách chat rooms của user
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getUserChatRooms(Authentication authentication) {
        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Lấy thông tin chat room
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @PathVariable Long roomId,
            Authentication authentication) {
        ChatRoomResponse room = chatService.getChatRoom(roomId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Gửi tin nhắn
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long roomId,
            @Valid @ModelAttribute ChatMessageRequest request,
            Authentication authentication) {
        ChatMessageResponse message = chatService.sendMessage(roomId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Lấy lịch sử tin nhắn
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        PagedResponse<ChatMessageResponse> messages = chatService.getMessages(roomId, page, size, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Thêm thành viên vào chat room (chỉ teacher/admin)
     */
    @PostMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<String>> addMember(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            Authentication authentication) {
        chatService.addMemberToRoom(roomId, userId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã thêm thành viên vào nhóm"));
    }

    /**
     * Xóa thành viên khỏi chat room (chỉ teacher/admin)
     */
    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<String>> removeMember(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            Authentication authentication) {
        chatService.removeMemberFromRoom(roomId, userId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thành viên khỏi nhóm"));
    }

    /**
     * Xóa tin nhắn
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        chatService.deleteMessage(messageId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa tin nhắn"));
    }

    /**
     * Cập nhật online status
     */
    @PostMapping("/rooms/{roomId}/status")
    public ResponseEntity<ApiResponse<String>> updateOnlineStatus(
            @PathVariable Long roomId,
            @RequestParam boolean isOnline,
            Authentication authentication) {
        chatService.updateOnlineStatus(roomId, authentication.getName(), isOnline);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái"));
    }

    /**
     * Lấy danh sách thành viên online
     */
    @GetMapping("/rooms/{roomId}/online")
    public ResponseEntity<ApiResponse<List<Long>>> getOnlineMembers(@PathVariable Long roomId) {
        List<Long> onlineUserIds = chatService.getOnlineMembers(roomId);
        return ResponseEntity.ok(ApiResponse.success(onlineUserIds));
    }

    /**
     * Lấy danh sách chi tiết thành viên trong room
     */
    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<ApiResponse<List<com.doanptit.elearing_backend_service.dto.res.ChatMemberResponse>>> getRoomMembers(
            @PathVariable Long roomId,
            Authentication authentication
    ) {
        var members = chatService.getMemberDetails(roomId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}

