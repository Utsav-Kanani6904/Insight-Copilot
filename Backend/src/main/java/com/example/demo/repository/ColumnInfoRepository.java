package com.example.demo.repository;

import com.example.demo.entity.ColumnInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ColumnInfoRepository extends MongoRepository<ColumnInfo, ObjectId> {
    List<ColumnInfo> findByDatabaseConfigId(ObjectId databaseConfigId);

}
