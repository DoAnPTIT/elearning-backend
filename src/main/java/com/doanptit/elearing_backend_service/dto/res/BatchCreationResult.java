package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BatchCreationResult {
    private int successCount = 0;
    private int failureCount = 0;
    private List<String> errorMessages = new ArrayList<>();
    
    public BatchCreationResult(int successCount, int failureCount, List<String> errorMessages) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
    }
}