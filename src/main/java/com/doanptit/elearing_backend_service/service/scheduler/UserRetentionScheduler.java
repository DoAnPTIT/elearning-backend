package com.doanptit.elearing_backend_service.service.scheduler;

import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRetentionScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Chạy mỗi ngày một lần vào lúc 9:00 sáng
    // Cron expression: giây phút giờ ngày tháng thứ
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminderEmails() {
        log.info(">>>> Bắt đầu quét người dùng không hoạt động...");

        // Thời điểm 7 ngày trước
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<User> inactiveUsers = userRepository.findInactiveUsers(sevenDaysAgo);

        for (User user : inactiveUsers) {
            String subject = "Chúng tôi nhớ bạn! 👋";
            String content = String.format("""
                <h3>Xin chào %s,</h3>
                <p>Đã 7 ngày rồi chúng tôi chưa thấy bạn quay lại học tập.</p>
                <p>Có rất nhiều bài giảng mới đang chờ bạn khám phá.</p>
                <a href="http://localhost:5173/login">Quay lại học ngay</a>
                """, user.getFirstname());

            // Gửi mail (Hàm này đã Async nên không lo chậm)
            emailService.sendEmail(user.getEmail(), subject, content);
        }

        log.info(">>>> Đã gửi email nhắc nhở cho {} người dùng.", inactiveUsers.size());
    }
}
