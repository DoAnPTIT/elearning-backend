package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchCreationResult {
    private int successCount = 0;
    private int failureCount = 0;
    private List<String> errorMessages = new ArrayList<>();
}