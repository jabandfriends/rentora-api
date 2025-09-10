package com.rentora.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

}