package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SQLQueryResult {
    @JsonProperty("sql_query")
    private String sqlQuery;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("result")
    private List<Map<String, Object>> result;
    @JsonProperty("query_executed_successfully")
    private String queryExecutedSuccessfully;
    @JsonProperty("status")
    private String status;
    @JsonProperty("chart_type")
    private String chartType;
    @JsonProperty("x_axis")
    private String xAxis;
    @JsonProperty("y_axis")
    private String yAxis;
    @JsonProperty("insight")
    private String insight;
}