package com.example.demo.controller;

import com.example.demo.dto.ChatHistoryDto;
import com.example.demo.dto.QueryRequestDto;
import com.example.demo.dto.QueryResponseDto;
import com.example.demo.service.DatabaseChatService;
import com.example.demo.service.UserEntryService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/database-chat")
public class DatabaseChatController {

    @Autowired
    private DatabaseChatService chatService;

    @Autowired
    private UserEntryService userEntryService;

    // Ask a query to database (databaseId in path)
    @PostMapping("/query/{databaseId}")
    public ResponseEntity<QueryResponseDto> askQuery(
            @PathVariable String databaseId,
            @Valid @RequestBody QueryRequestDto request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ObjectId dbId = new ObjectId(databaseId);
            ObjectId userObjectId = userEntryService.getbyusername(username).getId();

            QueryResponseDto response = chatService.askQuery(request, dbId, userObjectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get chat history for a database (for sidebar)
    @GetMapping("/history/{databaseId}")
    public ResponseEntity<ChatHistoryDto> getChatHistory(
            @PathVariable String databaseId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ObjectId dbId = new ObjectId(databaseId);
            ObjectId userObjectId = userEntryService.getbyusername(username).getId();

            ChatHistoryDto history = chatService.getChatHistory(dbId, userObjectId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get full query details by message ID (when user clicks on sidebar item)
    @GetMapping("/message/{messageId}")
    public ResponseEntity<?> getQueryDetails(
            @PathVariable String messageId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ObjectId msgId = new ObjectId(messageId);
            ObjectId userObjectId = userEntryService.getbyusername(username).getId();

            QueryResponseDto response = chatService.getQueryDetails(msgId, userObjectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a specific query message
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<?> deleteQueryMessage(
            @PathVariable String messageId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            ObjectId msgId = new ObjectId(messageId);
            ObjectId userObjectId = userEntryService.getbyusername(username).getId();

            chatService.deleteQueryMessage(msgId, userObjectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Clear all chat history for a database
    @DeleteMapping("/history/{databaseId}")
    public ResponseEntity<Void> clearAllChatHistory(
            @PathVariable String databaseId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            ObjectId dbId = new ObjectId(databaseId);
            ObjectId userObjectId = userEntryService.getbyusername(username).getId();

            chatService.clearAllChatHistory(dbId, userObjectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

