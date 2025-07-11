package com.example.demo.repository;

import com.example.demo.entity.DatabaseConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DatabaseConfigRepository extends MongoRepository<DatabaseConfig, ObjectId> {

    List<DatabaseConfig> findByUserid(ObjectId userid);
}
