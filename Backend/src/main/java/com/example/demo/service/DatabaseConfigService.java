package com.example.demo.service;

import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.User;
import com.example.demo.repository.DatabaseConfigRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseConfigService {
    @Autowired
    DatabaseConfigRepository databaseConfigRepository;

    @Autowired
    UserEntryService userEntryService;

    @Transactional
    public DatabaseConfig saveDatabaseConfig(DatabaseConfig config, String username) {
        User user = userEntryService.getbyusername(username);

        config.setUserid(user.getId());
        DatabaseConfig savedConfig = databaseConfigRepository.save(config);
        return savedConfig;
    }


    public DatabaseConfig updateDatabaseConfig(ObjectId configId, DatabaseConfig updatedConfig, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig existing = databaseConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Database config not found"));

        if (!existing.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this config.");
        }

        updatedConfig.setId(configId); // Preserve ID
        updatedConfig.setUserid(user.getId()); // Make sure user ID stays the same
        return databaseConfigRepository.save(updatedConfig);
    }

    public void deleteDatabaseConfig(ObjectId configId, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig existing = databaseConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Database config not found"));

        if (!existing.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this config.");
        }

        databaseConfigRepository.deleteById(configId);
    }

    public Optional<DatabaseConfig> getDatabaseConfigbyId(ObjectId id){
        return databaseConfigRepository.findById(id);
    }

    public List<DatabaseConfig> getAllConfigsForUser(String username) {
        User user = userEntryService.getbyusername(username);
        return databaseConfigRepository.findByUserid(user.getId());
    }

}
