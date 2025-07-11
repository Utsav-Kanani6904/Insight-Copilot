package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "databases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfig {
    @Id
    private ObjectId id;

    private ObjectId userid;

    private String description;

    @NonNull
    private String dbName;
    @NonNull
    private String host;
    @NonNull
    private int port;
    @NonNull
    private String user;
    @NonNull
    private String password; // Consider encrypting this

}
