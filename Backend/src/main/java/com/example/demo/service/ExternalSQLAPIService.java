package com.example.demo.service;

import com.example.demo.dto.DatabaseConfigDTO;
import com.example.demo.dto.SQLQueryResult;
import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.TableInfo;
import com.example.demo.entity.User;
import com.example.demo.repository.TableInfoRepository;
import com.example.demo.repository.UserEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExternalSQLAPIService {

    @Autowired
    TableInfoRepository tableInfoRepository;

    @Autowired
    DatabaseConfigService databaseConfigService;

    @Autowired
    UserEntryRepository userEntryRepository;

    private final RestTemplate restTemplate;

    @Value("${sql_generation_url}")
    String sqlGenerationUrl;

    public ExternalSQLAPIService() {
        this.restTemplate = new RestTemplate();
    }

    public SQLQueryResult generateSQL(String naturalLanguageQuery, ObjectId databaseId) {
        try {
            String url = sqlGenerationUrl;

            List<TableInfo> tableInfoList = tableInfoRepository.findByDatabaseConfigId(databaseId);

            Optional<DatabaseConfig> tempDatabase = databaseConfigService.getDatabaseConfigbyId(databaseId);

            if(tempDatabase.isEmpty()){
                throw new RuntimeException("Database is Not valid");
            }

            List<String> tableDescriptions = tableInfoList.stream()
                    .map(table -> table.getTableName() + " : " + table.getDescription())
                    .collect(Collectors.toList());

            DatabaseConfig config = tempDatabase.get();

            Optional<User> user = userEntryRepository.findById(config.getUserid());
            if (user.isEmpty()) {
                throw new RuntimeException("User not found for the given database config.");
            }

            String username = user.get().getUsername();

            DatabaseConfigDTO databaseInfo = DatabaseConfigDTO.builder()
                    .id(config.getId().toHexString())
                    .username(username)
                    .userid(config.getUserid().toHexString())
                    .description(config.getDescription())
                    .dbName(config.getDbName())
                    .host(config.getHost())
                    .port(config.getPort())
                    .user(config.getUser())
                    .password(config.getPassword())
                    .build();


            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", naturalLanguageQuery);
            requestBody.put("tables", tableDescriptions);
            requestBody.put("database", databaseInfo);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<SQLQueryResult> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SQLQueryResult.class
            );

            return response.getBody();

        } catch (Exception e) {
            SQLQueryResult errorResult = new SQLQueryResult();
            errorResult.setSqlQuery("");
            errorResult.setExplanation("Error: " + e.getMessage());
            errorResult.setQueryExecutedSuccessfully("False");
            errorResult.setStatus("error");
            return errorResult;
        }
    }
}

