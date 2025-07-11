package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseConfigDTO {
    private String id;
    private String userid;
    private String username;
    private String description;
    private String dbName;
    private String host;
    private int port;
    private String user;
    private String password;
}
