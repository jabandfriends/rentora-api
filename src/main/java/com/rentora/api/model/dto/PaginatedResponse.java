package com.rentora.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    public static <T> PaginatedResponse<T> of(Page<T> page, int requestPage) {
        Pagination pagination = new Pagination(
                requestPage,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        return new PaginatedResponse<>(page.getContent(), pagination);
    }
}