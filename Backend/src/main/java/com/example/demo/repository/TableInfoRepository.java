package com.example.demo.repository;

import com.example.demo.entity.TableInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TableInfoRepository extends MongoRepository<TableInfo, ObjectId> {
    List<TableInfo> findByDatabaseConfigId(ObjectId dbConfigId);
}
