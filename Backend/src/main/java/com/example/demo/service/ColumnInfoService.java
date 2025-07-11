package com.example.demo.service;

import com.example.demo.entity.ColumnInfo;
import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.User;
import com.example.demo.repository.ColumnInfoRepository;
import com.example.demo.repository.DatabaseConfigRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColumnInfoService {

    @Autowired
    private ColumnInfoRepository columnInfoRepository;

    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;

    @Autowired
    private UserEntryService userEntryService;

    @Autowired
    private PineconeNamespaceService pineconeColumnService;

    public ColumnInfo saveColumnInfo(ColumnInfo columnInfo, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig dbConfig = databaseConfigRepository.findById(columnInfo.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("DatabaseConfig not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to add columns to this DatabaseConfig.");
        }

        ColumnInfo savedColumn = columnInfoRepository.save(columnInfo);
        pineconeColumnService.upsertColumnRecord(savedColumn, user.getUsername(), dbConfig.getId());
        return savedColumn;
    }

    public ColumnInfo updateColumnInfo(ObjectId columnId, ColumnInfo updated, String username) {
        User user = userEntryService.getbyusername(username);

        ColumnInfo existing = columnInfoRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("ColumnInfo not found"));

        DatabaseConfig dbConfig = databaseConfigRepository.findById(existing.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("Associated DatabaseConfig not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this ColumnInfo.");
        }

        updated.setId(columnId);
        updated.setDatabaseConfigId(existing.getDatabaseConfigId());

        ColumnInfo savedColumn = columnInfoRepository.save(updated);
        pineconeColumnService.upsertColumnRecord(savedColumn, user.getUsername(), dbConfig.getId());
        return savedColumn;
    }

    public void deleteColumnInfo(ObjectId columnId, String username) {
        User user = userEntryService.getbyusername(username);

        ColumnInfo existing = columnInfoRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("ColumnInfo not found"));

        DatabaseConfig dbConfig = databaseConfigRepository.findById(existing.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("Associated DatabaseConfig not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this ColumnInfo.");
        }
        pineconeColumnService.deleteColumnRecord(columnId,user.getUsername(),dbConfig.getId());
        columnInfoRepository.deleteById(columnId);
    }

    public List<ColumnInfo> getAllColumnsForDatabase(ObjectId dbConfigId, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig dbConfig = databaseConfigRepository.findById(dbConfigId)
                .orElseThrow(() -> new RuntimeException("DatabaseConfig not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view columns for this database.");
        }

        return columnInfoRepository.findByDatabaseConfigId(dbConfigId);
    }
}
