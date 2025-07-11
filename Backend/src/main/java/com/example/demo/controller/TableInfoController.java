package com.example.demo.controller;

import com.example.demo.entity.TableInfo;
import com.example.demo.service.TableInfoService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableInfoController {

    @Autowired
    TableInfoService tableInfoService;

    @PostMapping("/{databaseid}")
    public ResponseEntity<?> saveTable(@RequestBody TableInfo info, @PathVariable ObjectId databaseid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        info.setDatabaseConfigId(databaseid);
        TableInfo saved = tableInfoService.saveTableInfo(info, username);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{databaseid}/{id}")
    public ResponseEntity<?> updateTable(@PathVariable ObjectId id, @PathVariable ObjectId databaseid ,@RequestBody TableInfo info) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        info.setDatabaseConfigId(databaseid);
        TableInfo updated = tableInfoService.updateTableInfo(id, info, username);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTable(@PathVariable ObjectId id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        tableInfoService.deleteTableInfo(id, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/by-database/{databaseid}")
    public ResponseEntity<?> getTablesByDb(@PathVariable ObjectId databaseid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<TableInfo> tables = tableInfoService.getAllTablesByDbId(databaseid, username);
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }
}