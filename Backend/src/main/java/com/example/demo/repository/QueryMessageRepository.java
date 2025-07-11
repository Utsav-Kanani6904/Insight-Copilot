package com.example.demo.repository;

import com.example.demo.entity.QueryMessage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueryMessageRepository extends MongoRepository<QueryMessage, ObjectId> {
    List<QueryMessage> findByDatabaseIdAndUserIdOrderByTimestampDesc(ObjectId databaseId, ObjectId userId);
    List<QueryMessage> findByDatabaseIdAndUserId(ObjectId databaseId, ObjectId userId, Sort sort);
    Optional<QueryMessage> findByIdAndUserId(ObjectId id, ObjectId userId);
    void deleteByIdAndUserId(ObjectId id, ObjectId userId);
    void deleteByDatabaseIdAndUserId(ObjectId databaseId, ObjectId userId);
    long countByDatabaseIdAndUserId(ObjectId databaseId, ObjectId userId);
}

