package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserEntryController {
    @Autowired
    private UserEntryService userEntryService;

    @PutMapping
    public ResponseEntity<?> updateuser(@RequestBody User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User newUser = userEntryService.getbyusername(username);
        if(newUser != null){
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            userEntryService.registeruser(newUser);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteuser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userEntryService.deleteuser(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
