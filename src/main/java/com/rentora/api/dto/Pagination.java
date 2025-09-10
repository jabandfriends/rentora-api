package com.rentora.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Pagination {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;


}