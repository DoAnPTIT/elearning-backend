package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.InteractionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRequest {
    
    private Long courseId;
    
    private Long lessonId;
    
    @NotNull(message = "Loại tương tác không được để trống")
    private InteractionType interactionType;
    
    private String searchQuery;
    
    private Integer durationSeconds;
    
    private String deviceType;
    
    private String sessionId;
}
