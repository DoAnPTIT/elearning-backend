package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.ChatMessageRequest;
import com.doanptit.elearing_backend_service.dto.res.ChatMessageResponse;
import com.doanptit.elearing_backend_service.dto.res.ChatMemberResponse;
import com.doanptit.elearing_backend_service.dto.res.ChatRoomResponse;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.ChatMessage;
import com.doanptit.elearing_backend_service.model.ChatRoom;
import com.doanptit.elearing_backend_service.model.ChatRoomMember;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.ChatMessageRepository;
import com.doanptit.elearing_backend_service.repository.ChatRoomMemberRepository;
import com.doanptit.elearing_backend_service.repository.ChatRoomRepository;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.ChatService;
import com.doanptit.elearing_backend_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatRoomResponse getOrCreateChatRoomForCourse(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Tìm chat room đã tồn tại
        ChatRoom chatRoom = chatRoomRepository.findActiveByCourseId(courseId)
                .orElseGet(() -> {
                    // Tạo mới chat room
                    User creator = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                    ChatRoom newRoom = ChatRoom.builder()
                            .name(course.getTitle() + " - Group Chat")
                            .description("Group chat cho khóa học: " + course.getTitle())
                            .course(course)
                            .createdByUser(creator)
                            .active(true)
                            .build();

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);

                    // Thêm teacher vào room
                    User teacher = course.getAuthor();
                    addMemberToRoomInternal(savedRoom.getId(), teacher.getId());

                    return savedRoom;
                });

        // Đảm bảo user hiện tại là member
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Đảm bảo user hiện tại là member
        chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoom.getId(), currentUser.getId())
                .orElseGet(() -> {
                    ChatRoomMember newMember = ChatRoomMember.builder()
                            .chatRoom(chatRoom)
                            .user(currentUser)
                            .isOnline(false)
                            .active(true)
                            .build();
                    return chatRoomMemberRepository.save(newMember);
                });

        return mapToChatRoomResponse(chatRoom, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserChatRooms(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<ChatRoom> rooms = chatRoomRepository.findByMemberId(user.getId());
        return rooms.stream()
                .map(room -> mapToChatRoomResponse(room, user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomResponse getChatRoom(Long roomId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Kiểm tra user có phải member không
        boolean isMember = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, user.getId()).isPresent();
        if (!isMember && !user.getRole().equals(Role.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return mapToChatRoomResponse(room, user.getId());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, ChatMessageRequest request, String userEmail) {
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Kiểm tra user có phải member không
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, sender.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        ChatMessage.MessageType messageType = ChatMessage.MessageType.TEXT;
        String fileUrl = null;
        String fileName = null;
        Long fileSize = null;
        String fileType = null;

        // Xử lý file upload nếu có
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            fileUrl = s3Service.uploadFile(file, "chat/" + roomId + "/");
            fileName = file.getOriginalFilename();
            fileSize = file.getSize();
            fileType = file.getContentType();

            if (fileType != null && fileType.startsWith("image/")) {
                messageType = ChatMessage.MessageType.IMAGE;
            } else {
                messageType = ChatMessage.MessageType.FILE;
            }
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(request.getContent())
                .messageType(messageType)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .edited(false)
                .deleted(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Gửi real-time message qua WebSocket
        ChatMessageResponse response = mapToChatMessageResponse(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ChatMessageResponse> getMessages(Long roomId, int page, int size, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra user có phải member không
        boolean isMember = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, user.getId()).isPresent();
        if (!isMember && !user.getRole().equals(Role.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoomIdAndDeletedFalseOrderByCreatedOnDesc(roomId, pageable);

        List<ChatMessageResponse> content = messagePage.getContent().stream()
                .map(this::mapToChatMessageResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void addMemberToRoom(Long roomId, Long userId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Chỉ teacher của course hoặc admin mới được thêm member
        if (!requester.getRole().equals(Role.ADMIN) && 
            !room.getCourse().getAuthor().getEmail().equals(requesterEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        addMemberToRoomInternal(roomId, userId);
    }

    private void addMemberToRoomInternal(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        User user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra đã là member chưa
        if (chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId).isPresent()) {
            return; // Đã là member rồi
        }

        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(room)
                .user(user)
                .isOnline(false)
                .active(true)
                .build();

        chatRoomMemberRepository.save(member);

        // Gửi system message
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(user)
                .content(user.getFirstname() + " " + user.getLastname() + " đã tham gia nhóm")
                .messageType(ChatMessage.MessageType.SYSTEM)
                .deleted(false)
                .build();

        chatMessageRepository.save(systemMessage);

        // Broadcast member added
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "member_added");
        payload.put("userId", userId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/members", payload);
    }

    @Override
    @Transactional
    public void removeMemberFromRoom(Long roomId, Long userId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Chỉ teacher của course hoặc admin mới được xóa member
        if (!requester.getRole().equals(Role.ADMIN) && 
            !room.getCourse().getAuthor().getEmail().equals(requesterEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User removedUser = member.getUser();

        chatRoomMemberRepository.delete(member);

        // Gửi system message
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(removedUser)
                .content(removedUser.getFirstname() + " " + removedUser.getLastname() + " đã rời khỏi nhóm")
                .messageType(ChatMessage.MessageType.SYSTEM)
                .deleted(false)
                .build();

        chatMessageRepository.save(systemMessage);

        // Broadcast member removed
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "member_removed");
        payload.put("userId", userId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/members", payload);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Chỉ sender hoặc admin mới được xóa
        if (!message.getSender().getEmail().equals(userEmail) && !user.getRole().equals(Role.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        message.setDeleted(true);
        chatMessageRepository.save(message);

        // Broadcast message deleted
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "message_deleted");
        payload.put("messageId", messageId);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatRoom().getId(), payload);
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long roomId, String userEmail, boolean isOnline) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, user.getId())
                .orElse(null);

        if (member != null) {
            member.setIsOnline(isOnline);
            if (isOnline) {
                member.setLastSeenAt(LocalDateTime.now());
            }
            chatRoomMemberRepository.save(member);

            // Broadcast online status change
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.getId());
            payload.put("isOnline", isOnline);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/status", payload);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getOnlineMembers(Long roomId) {
        return chatRoomMemberRepository.findOnlineUserIdsByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMemberResponse> getMemberDetails(Long roomId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Chỉ cho phép thành viên room hoặc admin xem danh sách thành viên
        boolean isMember = chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, requester.getId()).isPresent();
        if (!isMember && !requester.getRole().equals(Role.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(roomId);
        Set<Long> onlineIds = new HashSet<>(chatRoomMemberRepository.findOnlineUserIdsByRoomId(roomId));

        return members.stream()
                .map(m -> {
                    User u = m.getUser();
                    String fullname = (u.getFirstname() != null ? u.getFirstname() : "") +
                            " " +
                            (u.getLastname() != null ? u.getLastname() : "");
                    return ChatMemberResponse.builder()
                            .id(u.getId())
                            .fullname(fullname.trim().isEmpty() ? u.getEmail() : fullname.trim())
                            .email(u.getEmail())
                            .image(u.getImage())
                            .online(onlineIds.contains(u.getId()))
                            .role(u.getRole() != null ? u.getRole().name() : null)
                            .build();
                })
                .toList();
    }

    private ChatRoomResponse mapToChatRoomResponse(ChatRoom room, Long currentUserId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(room.getId());
        Set<Long> onlineUserIds = chatRoomMemberRepository.findOnlineUserIdsByRoomId(room.getId())
                .stream().collect(Collectors.toSet());

        return ChatRoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .courseId(room.getCourse().getId())
                .courseTitle(room.getCourse().getTitle())
                .createdBy(room.getCreatedByUser() != null ? room.getCreatedByUser().getId() : null)
                .memberCount(members.size())
                .onlineCount(onlineUserIds.size())
                .onlineUserIds(onlineUserIds)
                .isCurrentUserOnline(onlineUserIds.contains(currentUserId))
                .lastMessage(getLastMessage(room.getId()))
                .build();
    }

    private ChatMessageResponse getLastMessage(Long roomId) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoomIdAndDeletedFalseOrderByCreatedOnDesc(roomId, pageable);
        if (messagePage.isEmpty()) {
            return null;
        }
        return mapToChatMessageResponse(messagePage.getContent().get(0));
    }

    private ChatMessageResponse mapToChatMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstname() + " " + message.getSender().getLastname())
                .senderEmail(message.getSender().getEmail())
                .senderImage(message.getSender().getImage())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .fileSize(message.getFileSize())
                .fileType(message.getFileType())
                .edited(message.getEdited())
                .deleted(message.getDeleted())
                .createdAt(message.getCreatedOn())
                .build();
    }
}

