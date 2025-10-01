package com.doanptit.elearing_backend_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PagedResponse<T>{
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
