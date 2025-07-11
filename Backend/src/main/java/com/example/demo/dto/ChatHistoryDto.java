package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatHistoryDto {
    private String databaseId;
    private String databaseName;
    private LocalDateTime lastQueryAt;
    private Integer totalQueries;
    private List<QuerySummaryDto> queryHistory; // Just summary for sidebar

}