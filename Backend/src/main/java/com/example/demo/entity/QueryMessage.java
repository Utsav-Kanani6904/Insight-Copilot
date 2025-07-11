package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "query_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryMessage {
    @Id
    private ObjectId id;

    @Indexed
    private ObjectId databaseId; // Reference to which database this query belongs to

    @Indexed
    private ObjectId userId; // User who made the query

    private String userQuery;
    private String sqlQuery;
    private String explanation;
    private List<Map<String, Object>> result;
    private String queryExecutedSuccessfully;
    private String status;
    private String errorMessage;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
    private String chartType;
    private String xAxis;
    private String yAxis;
    private String insight;

    public QueryMessage(ObjectId databaseId, ObjectId userId, String userQuery) {
        this.databaseId = databaseId;
        this.userId = userId;
        this.userQuery = userQuery;
        this.timestamp = LocalDateTime.now();
    }
}

