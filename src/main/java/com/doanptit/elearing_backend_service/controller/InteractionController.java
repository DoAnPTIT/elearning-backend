package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.InteractionRequest;
import com.doanptit.elearing_backend_service.service.UserInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class InteractionController {

    private final UserInteractionService interactionService;

    /**
     * Track user interactions with courses and lessons.
     * This endpoint is intentionally fire-and-forget (async) to not block the main user flow.
     */
    @PostMapping("/interactions")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> trackInteraction(
            Authentication authentication,
            @Valid @RequestBody InteractionRequest request) {
        
        String email = authentication.getName();
        interactionService.trackInteractionAsync(email, request);
        
        return ResponseEntity.ok(ApiResponse.success("Interaction tracked", "OK"));
    }

    /**
     * Batch track multiple interactions at once (for offline sync).
     */
    @PostMapping("/interactions/batch")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> trackBatchInteractions(
            Authentication authentication,
            @Valid @RequestBody java.util.List<InteractionRequest> requests) {
        
        String email = authentication.getName();
        requests.forEach(req -> interactionService.trackInteractionAsync(email, req));
        
        return ResponseEntity.ok(ApiResponse.success("Batch interactions tracked", "OK"));
    }
}
