package com.example.demo.repository;

import com.example.demo.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserEntryRepository extends MongoRepository<User, ObjectId> {
    public User findByusername(String username);

    public void deleteByusername(String username);
}
