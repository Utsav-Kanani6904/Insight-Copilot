package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuerySummaryDto {
    private String messageId; // ObjectId as string
    private String userQuery;
    private String status; // success/error
    private LocalDateTime timestamp;
}

