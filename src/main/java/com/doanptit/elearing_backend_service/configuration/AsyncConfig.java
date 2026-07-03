package com.doanptit.elearing_backend_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String EMAIL_NOTIFICATION_EXECUTOR = "emailNotificationExecutor";
    public static final String AI_TRAINING_EXECUTOR = "aiTrainingExecutor";
    public static final String INTERACTION_EXECUTOR = "interactionExecutor";

    /**
     * Email + notification: tần suất thấp/trung bình nhưng không được mất task.
     * CallerRunsPolicy -> khi pool và queue đầy, task chạy tạm trên thread gọi thay vì bị huỷ.
     */
    @Bean(EMAIL_NOTIFICATION_EXECUTOR)
    public Executor emailNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("email-notif-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * AI training: tần suất thấp nhưng mỗi task chạy lâu (ingest toàn bộ khoá học).
     * Tách riêng để không chiếm giữ pool gửi email/notification trong lúc training.
     */
    @Bean(AI_TRAINING_EXECUTOR)
    public Executor aiTrainingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("ai-training-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Track tương tác người dùng: tần suất rất cao, chấp nhận rớt bớt khi quá tải.
     * DiscardOldestPolicy -> quá tải thì bỏ task cũ nhất trong queue thay vì chặn request của user.
     */
    @Bean(INTERACTION_EXECUTOR)
    public Executor interactionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("interaction-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}
