package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@CompoundIndex(name = "unique_table_per_db", def = "{'tableName' : 1, 'databaseConfigId': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "table_metadata")
public class TableInfo {
    @Id
    private ObjectId id;

    @NonNull
    private String tableName;

    @NonNull
    private String description;

    private List<String> joinsWith = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<String> foreignKeys = new ArrayList<>();

    private ObjectId databaseConfigId;
}
