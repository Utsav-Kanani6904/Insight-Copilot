package com.example.demo.service;

import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.TableInfo;
import com.example.demo.entity.User;
import com.example.demo.repository.DatabaseConfigRepository;
import com.example.demo.repository.TableInfoRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableInfoService {
    @Autowired
    TableInfoRepository tableInfoRepo;

    @Autowired
    DatabaseConfigRepository dbConfigRepo;

    @Autowired
    PineconeNamespaceService pineconeTableInfoService;

    @Autowired
    UserEntryService userEntryService;

    public TableInfo saveTableInfo(TableInfo info, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig dbConfig = dbConfigRepo.findById(info.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("Database not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to add table to this database.");
        }

        TableInfo savedtable = tableInfoRepo.save(info);
        pineconeTableInfoService.upsertTableRecord(savedtable, user.getUsername(), dbConfig.getId());
        return savedtable;
    }

    public TableInfo updateTableInfo(ObjectId tableId, TableInfo updatedInfo, String username) {
        User user = userEntryService.getbyusername(username);

        TableInfo existing = tableInfoRepo.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        DatabaseConfig dbConfig = dbConfigRepo.findById(existing.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("Database not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this table.");
        }

        updatedInfo.setId(tableId);
        updatedInfo.setDatabaseConfigId(existing.getDatabaseConfigId()); // don’t allow change of db

        TableInfo savedtable = tableInfoRepo.save(updatedInfo);
        pineconeTableInfoService.upsertTableRecord(savedtable, user.getUsername(), dbConfig.getId());
        return savedtable;
    }

    public void deleteTableInfo(ObjectId tableId, String username) {
        User user = userEntryService.getbyusername(username);

        TableInfo existing = tableInfoRepo.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        DatabaseConfig dbConfig = dbConfigRepo.findById(existing.getDatabaseConfigId())
                .orElseThrow(() -> new RuntimeException("Database not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this table.");
        }

        pineconeTableInfoService.deleteTableRecord(tableId, user.getUsername(), dbConfig.getId());
        tableInfoRepo.deleteById(tableId);
    }

    public List<TableInfo> getAllTablesByDbId(ObjectId dbConfigId, String username) {
        User user = userEntryService.getbyusername(username);

        DatabaseConfig dbConfig = dbConfigRepo.findById(dbConfigId)
                .orElseThrow(() -> new RuntimeException("Database not found"));

        if (!dbConfig.getUserid().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view tables of this database.");
        }

        return tableInfoRepo.findByDatabaseConfigId(dbConfigId);
    }

}
