package com.rentora.api.dto;

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