package com.rentora.api.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}