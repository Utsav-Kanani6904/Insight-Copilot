package com.example.demo.repository;

import com.example.demo.entity.DatabaseChat;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatabaseChatRepository extends MongoRepository<DatabaseChat, ObjectId> {
    Optional<DatabaseChat> findByDatabaseId(ObjectId databaseId);
    Optional<DatabaseChat> findByDatabaseIdAndUserId(ObjectId databaseId, ObjectId userId);
    boolean existsByDatabaseId(ObjectId databaseId);
}

