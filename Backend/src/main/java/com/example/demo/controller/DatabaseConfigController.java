package com.example.demo.controller;


import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.User;
import com.example.demo.service.DatabaseConfigService;
import com.example.demo.service.UserEntryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/databaseconfig")
public class DatabaseConfigController {
    @Autowired
    DatabaseConfigService databaseConfigService;

    @Autowired
    UserEntryService userEntryService;

    @PostMapping
    public ResponseEntity<?> saveConfig(@RequestBody DatabaseConfig config) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        DatabaseConfig savedConfig = databaseConfigService.saveDatabaseConfig(config, username);
        return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatabaseConfig> updateConfig(@PathVariable ObjectId id,
                                                       @RequestBody DatabaseConfig updatedConfig) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try{
            DatabaseConfig newConfig = databaseConfigService.updateDatabaseConfig(id,updatedConfig,username);
            return new ResponseEntity<>(newConfig, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConfig(@PathVariable ObjectId id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try{
            databaseConfigService.deleteDatabaseConfig(id,username);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllConfigs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<DatabaseConfig> allDatbaseofUser = databaseConfigService.getAllConfigsForUser(username);
        return new ResponseEntity<>(allDatbaseofUser, HttpStatus.OK);
    }

    @GetMapping("/{myid}")
    public ResponseEntity<?> getwithId(@PathVariable ObjectId myid){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userEntryService.getbyusername(username);
        List<DatabaseConfig> collect = databaseConfigService.getAllConfigsForUser(username);
        if(!collect.isEmpty()){
            Optional<DatabaseConfig> databaseConfig = databaseConfigService.getDatabaseConfigbyId(myid);
            if(databaseConfig.isPresent()){
                return new ResponseEntity<>(databaseConfig,HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}
