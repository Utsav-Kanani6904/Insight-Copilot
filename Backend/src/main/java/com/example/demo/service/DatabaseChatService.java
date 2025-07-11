package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.DatabaseChat;
import com.example.demo.entity.QueryMessage;
import com.example.demo.repository.DatabaseChatRepository;
import com.example.demo.repository.QueryMessageRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DatabaseChatService {

    @Autowired
    private DatabaseChatRepository chatRepository;

    @Autowired
    private QueryMessageRepository queryMessageRepository;

    @Autowired
    UserEntryService userEntryService;

    @Autowired
    private ExternalSQLAPIService sqlAPIService;

    public QueryResponseDto askQuery(QueryRequestDto request, ObjectId databaseId, ObjectId userId) {
        long startTime = System.currentTimeMillis();

        try {
            // Get or create database chat
            DatabaseChat chat = getOrCreateDatabaseChat(databaseId, userId);

            // Create query message
            QueryMessage query = new QueryMessage(databaseId, userId, request.getQuery());

            // Call external SQL API
            SQLQueryResult sqlResult = sqlAPIService.generateSQL(
                    request.getQuery(),
                    databaseId
            );

            // Update query with API response
            query.setSqlQuery(sqlResult.getSqlQuery());
            query.setExplanation(sqlResult.getExplanation());
            query.setResult(sqlResult.getResult());
            query.setQueryExecutedSuccessfully(sqlResult.getQueryExecutedSuccessfully());
            query.setStatus(sqlResult.getStatus());
            query.setChartType(sqlResult.getChartType());
            query.setInsight(sqlResult.getInsight());
            query.setXAxis(sqlResult.getXAxis());
            query.setYAxis(sqlResult.getYAxis());

            if (Objects.equals(sqlResult.getQueryExecutedSuccessfully(), "False")) {
                query.setErrorMessage("Query failed: " + sqlResult.getStatus());
            }

            long processingTime = System.currentTimeMillis() - startTime;
            query.setProcessingTimeMs(processingTime);


            System.out.println("Saving query message: " + query.getUserQuery());
            QueryMessage savedQuery = queryMessageRepository.save(query);

            chat.incrementQueryCount();
            chatRepository.save(chat);
            System.out.println("Chat updated with new query count: " + chat.getTotalQueries());
            return convertToResponseDto(savedQuery);

        } catch (Exception e) {
            return handleErrorResponse(request.getQuery(), databaseId, userId, e, startTime);
        }
    }

    public ChatHistoryDto getChatHistory(ObjectId databaseId, ObjectId userId) {
        Optional<DatabaseChat> chatOpt = chatRepository.findByDatabaseIdAndUserId(databaseId, userId);

        // Get query messages for sidebar
        List<QueryMessage> messages = queryMessageRepository.findByDatabaseIdAndUserIdOrderByTimestampDesc(databaseId, userId);

        List<QuerySummaryDto> history = messages.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());

        if (chatOpt.isEmpty()) {
            // Return empty history if no chat exists yet
            return ChatHistoryDto.builder()
                    .databaseId(databaseId.toHexString())
                    .totalQueries(0)
                    .queryHistory(history)
                    .build();
        }

        DatabaseChat chat = chatOpt.get();

        return ChatHistoryDto.builder()
                .databaseId(chat.getDatabaseId().toHexString())
                .databaseName(chat.getDatabaseName())
                .lastQueryAt(chat.getLastQueryAt())
                .totalQueries(chat.getTotalQueries())
                .queryHistory(history)
                .build();
    }

    public QueryResponseDto getQueryDetails(ObjectId messageId, ObjectId userId) {
        Optional<QueryMessage> queryOpt = queryMessageRepository.findByIdAndUserId(messageId, userId);

        if (queryOpt.isEmpty()) {
            throw new RuntimeException("Query message not found or access denied");
        }

        return convertToResponseDto(queryOpt.get());
    }

    public void deleteQueryMessage(ObjectId messageId, ObjectId userId) {
        Optional<QueryMessage> queryOpt = queryMessageRepository.findByIdAndUserId(messageId, userId);

        if (queryOpt.isEmpty()) {
            throw new RuntimeException("Query message not found or access denied");
        }

        QueryMessage query = queryOpt.get();
        ObjectId databaseId = query.getDatabaseId();

        // Delete the query message
        queryMessageRepository.deleteByIdAndUserId(messageId, userId);

        // Update chat statistics
        Optional<DatabaseChat> chatOpt = chatRepository.findByDatabaseIdAndUserId(databaseId, userId);
        if (chatOpt.isPresent()) {
            DatabaseChat chat = chatOpt.get();
            long remainingQueries = queryMessageRepository.countByDatabaseIdAndUserId(databaseId, userId);
            chat.setTotalQueries((int) remainingQueries);
            chatRepository.save(chat);
        }
    }

    public void clearAllChatHistory(ObjectId databaseId, ObjectId userId) {
        // Delete all query messages for this database and user
        queryMessageRepository.deleteByDatabaseIdAndUserId(databaseId, userId);

        // Update chat statistics
        Optional<DatabaseChat> chatOpt = chatRepository.findByDatabaseIdAndUserId(databaseId, userId);
        if (chatOpt.isPresent()) {
            DatabaseChat chat = chatOpt.get();
            chat.setTotalQueries(0);
            chatRepository.save(chat);
        }
    }

    private DatabaseChat getOrCreateDatabaseChat(ObjectId databaseId, ObjectId userId) {
        Optional<DatabaseChat> existing = chatRepository.findByDatabaseIdAndUserId(databaseId, userId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new chat for this database
        // You might want to get database name from your database service
        String databaseName = "Database " + databaseId.toHexString(); // Replace with actual name

        DatabaseChat newChat = new DatabaseChat(databaseId, databaseName, userId);
        return chatRepository.save(newChat);
    }

    private QueryResponseDto handleErrorResponse(String userQuery, ObjectId databaseId, ObjectId userId, Exception e, long startTime) {
        // Save error query to database as well
        QueryMessage errorQuery = new QueryMessage(databaseId, userId, userQuery);
        errorQuery.setExplanation("Sorry, I encountered an error processing your query.");
        errorQuery.setErrorMessage(e.getMessage());
        errorQuery.setQueryExecutedSuccessfully("False");
        errorQuery.setStatus("error");
        errorQuery.setProcessingTimeMs(System.currentTimeMillis() - startTime);

        QueryMessage savedErrorQuery = queryMessageRepository.save(errorQuery);

        return convertToResponseDto(savedErrorQuery);
    }

    private QueryResponseDto convertToResponseDto(QueryMessage query) {
        return QueryResponseDto.builder()
                .messageId(query.getId().toHexString())
                .userQuery(query.getUserQuery())
                .sqlQuery(query.getSqlQuery())
                .explanation(query.getExplanation())
                .result(query.getResult())
                .queryExecutedSuccessfully(query.getQueryExecutedSuccessfully())
                .status(query.getStatus())
                .chartType(query.getChartType())
                .insight(query.getInsight())
                .xAxis(query.getXAxis())
                .yAxis(query.getYAxis())
                .errorMessage(query.getErrorMessage())
                .timestamp(query.getTimestamp())
                .processingTimeMs(query.getProcessingTimeMs())
                .build();
    }

    private QuerySummaryDto convertToSummaryDto(QueryMessage query) {
        return QuerySummaryDto.builder()
                .messageId(query.getId().toHexString())
                .userQuery(query.getUserQuery())
                .status(query.getStatus())
                .timestamp(query.getTimestamp())
                .build();
    }
}
