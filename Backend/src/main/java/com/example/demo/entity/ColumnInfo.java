package com.example.demo.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "column_metadata")
public class ColumnInfo {
    @Id
    private ObjectId id;

    @NonNull
    private String columnName;

    @NonNull
    private String description;

    @NonNull
    private String type;

    @NonNull
    private String tableName;

    @NotNull
    private String foreignRelation;

    private ObjectId databaseConfigId;
}
