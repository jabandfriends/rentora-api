package com.rentora.api.model.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PaginatedResponseWithMetadata<T, M> extends PaginatedResponse<T> {
    private final M metadata;

    public PaginatedResponseWithMetadata(List<T> data, Pagination pagination, M metadata) {
        super(data, pagination);
        this.metadata = metadata;
    }
    public static <T, M> PaginatedResponseWithMetadata<T, M> of(Page<T> page, int requestPage, M metadata) {
        Pagination pagination = new Pagination(
                requestPage,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        return new PaginatedResponseWithMetadata<>(page.getContent(), pagination, metadata);
    }
}