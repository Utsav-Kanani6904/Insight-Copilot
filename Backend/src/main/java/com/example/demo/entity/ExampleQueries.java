package com.example.demo.entity;

import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class ExampleQueries {
    @Id
    private ObjectId id;

    @NonNull
    private String naturalLanguageQuery;

    @NonNull
    private String sqlQuery;

    private ObjectId databaseConfigId;
}
