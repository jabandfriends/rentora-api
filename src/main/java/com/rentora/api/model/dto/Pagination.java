package com.rentora.api.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
public class Pagination {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;

    public Pagination(int page, int size, int totalPages, long totalElements) {
        this.page = page + 1;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}