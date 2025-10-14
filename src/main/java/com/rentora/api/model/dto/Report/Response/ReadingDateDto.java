package com.rentora.api.model.dto.Report.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReadingDateDto {
    LocalDate readingDate;
}
