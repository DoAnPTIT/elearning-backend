package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageResponse {
    private String imageUrl;
}