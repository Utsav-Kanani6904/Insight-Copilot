package com.example.demo.controller;

import com.example.demo.entity.TableInfo;
import com.example.demo.entity.User;
import com.example.demo.service.PineconeService;
import com.example.demo.service.UserEntryService;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.SearchRecordsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserEntryService userEntryService;

    @Autowired
    PineconeService pineconeService;

    @GetMapping("/all-user")
    public ResponseEntity<?> getalluser(){
        List<User> alluser = userEntryService.getalluser();
        if(alluser != null && !alluser.isEmpty()) {
            return new ResponseEntity<>(alluser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/create-admin-user")
    public ResponseEntity<?> createadmin(@RequestBody User user){
        userEntryService.saveAdmin(user);
        return new ResponseEntity<>(user,HttpStatus.CREATED);
    }

    @GetMapping("/find-table")
    public SearchRecordsResponse getquery() throws ApiException {
        SearchRecordsResponse searchRecordsResponse = pineconeService.makeQuery();
        return searchRecordsResponse;
    }

    @PostMapping("/upload-table")
    public ResponseEntity<?> uploadTableInfo(@RequestBody List<TableInfo> tableInfoList) throws Exception {
        pineconeService.upsertTableRecord(tableInfoList);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
