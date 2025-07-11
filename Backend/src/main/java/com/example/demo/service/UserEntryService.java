package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserEntryService {

    @Autowired
    private UserEntryRepository userEntryRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getalluser(){
        return userEntryRepository.findAll();
    }

    public Optional<User> getuserbyId(ObjectId id){
        return userEntryRepository.findById(id);
    }

    public User getbyusername(String username){
        return userEntryRepository.findByusername(username);
    }

    public void saveuser(User user){
        userEntryRepository.save(user);
    }

    public void saveAdmin(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        userEntryRepository.save(user);
    }

    public void registeruser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER"));
        userEntryRepository.save(user);
    }

    public void deleteuser(String username){
        userEntryRepository.deleteByusername(username);
    }
}
