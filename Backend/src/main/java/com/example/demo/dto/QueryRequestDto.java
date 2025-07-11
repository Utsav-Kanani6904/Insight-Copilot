package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequestDto {
    @NotBlank(message = "Query cannot be empty")
    private String query;
}
