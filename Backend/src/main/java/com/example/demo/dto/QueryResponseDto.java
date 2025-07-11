package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class QueryResponseDto {
    private String messageId; // ObjectId as string
    private String userQuery;
    private String sqlQuery;
    private String explanation;
    private List<Map<String, Object>> result;
    private String queryExecutedSuccessfully;
    private String status;
    private String errorMessage;
    private String chartType;
    private String xAxis;
    private String yAxis;
    private String insight;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
}