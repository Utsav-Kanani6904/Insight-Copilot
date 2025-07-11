package com.example.demo.controller;

import com.example.demo.entity.ColumnInfo;
import com.example.demo.service.ColumnInfoService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
public class ColumnInfoController {

    @Autowired
    private ColumnInfoService columnInfoService;

    @PostMapping("/{databaseid}")
    public ResponseEntity<?> saveColumn(@RequestBody ColumnInfo columnInfo, @PathVariable ObjectId databaseid) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            columnInfo.setDatabaseConfigId(databaseid);
            ColumnInfo saved = columnInfoService.saveColumnInfo(columnInfo, username);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColumn(@PathVariable ObjectId id, @RequestBody ColumnInfo updated) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            ColumnInfo updatedCol = columnInfoService.updateColumnInfo(id, updated, username);

            return new ResponseEntity<>(updatedCol, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColumn(@PathVariable ObjectId id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            columnInfoService.deleteColumnInfo(id, username);

            return new ResponseEntity<>("ColumnInfo deleted successfully", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/by-database/{dbConfigId}")
    public ResponseEntity<?> getAllColumnsForDatabase(@PathVariable ObjectId dbConfigId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<ColumnInfo> columns = columnInfoService.getAllColumnsForDatabase(dbConfigId, username);

            return new ResponseEntity<>(columns, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
