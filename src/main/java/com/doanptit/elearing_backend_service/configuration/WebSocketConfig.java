package com.doanptit.elearing_backend_service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Chạy trước các config bảo mật mặc định khác
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình các prefix cho broker
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user"); // Quan trọng cho thông báo riêng tư
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint chính để client kết nối
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Chấp nhận mọi nguồn (CORS) cho WebSocket
                .withSockJS(); // Hỗ trợ fallback
    }

    /**
     * Phần quan trọng nhất: Interceptor để chặn và xác thực kết nối
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Chỉ kiểm tra khi client gửi lệnh CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // 1. Lấy header Authorization từ gói tin STOMP
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader == null || authHeader.isBlank()) {
                        // Một số client/bundler có thể gửi header key dạng lowercase
                        authHeader = accessor.getFirstNativeHeader("authorization");
                    }

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7); // Cắt bỏ chữ "Bearer "

                        try {
                            // 2. Lấy username từ token bằng JwtUtil
                            String username = jwtUtil.extractUsername(token);

                            if (username != null) {
                                // 3. Load thông tin user từ DB
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                // 4. Validate token (check hạn, khớp username...)
                                if (jwtUtil.validateToken(token, userDetails.getUsername())) {

                                    // 5. Tạo đối tượng Authentication
                                    UsernamePasswordAuthenticationToken authentication =
                                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                                    // 6. GÁN USER VÀO SESSION WEBSOCKET
                                    // Đây là bước quyết định để convertAndSendToUser hoạt động
                                    accessor.setUser(authentication);

                                    log.info("WebSocket Connected & Authenticated User: {}", username);
                                }
                            }
                        } catch (Exception e) {
                            // Nếu token lỗi, log ra và không set User -> Kết nối sẽ bị coi là Anonymous hoặc từ chối
                            log.error("WebSocket Authentication failed: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}