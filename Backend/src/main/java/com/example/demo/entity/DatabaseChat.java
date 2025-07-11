package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "database_chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseChat {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private ObjectId databaseId; // One chat per database

    private String databaseName;
    private ObjectId userId; // Owner of the database
    private LocalDateTime createdAt;
    private LocalDateTime lastQueryAt;
    private Integer totalQueries = 0;

    public DatabaseChat(ObjectId databaseId, String databaseName, ObjectId userId) {
        this.databaseId = databaseId;
        this.databaseName = databaseName;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastQueryAt = LocalDateTime.now();
    }

    public void incrementQueryCount() {
        this.totalQueries = (this.totalQueries == null ? 0 : this.totalQueries) + 1;
        this.lastQueryAt = LocalDateTime.now();
    }
}
