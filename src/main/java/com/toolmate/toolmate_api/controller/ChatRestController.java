package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.response.ChatMessage;
import com.toolmate.toolmate_api.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Real-Time Chat", description = "WebSocket-based real-time messaging")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/history/{borrowRequestId}")
    @Operation(summary = "Get chat history for a borrow request")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable Long borrowRequestId,
            Authentication authentication) {
        List<ChatMessage> history = chatService.getChatHistory(
                borrowRequestId,
                authentication.getName()
        );
        return ResponseEntity.ok(history);
    }

    @GetMapping("/unread-count/{borrowRequestId}")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long borrowRequestId,
            Authentication authentication) {
        // Extract user ID from authentication (you'll need to implement this)
        Long userId = getUserIdFromAuth(authentication);

        long count = chatService.getUnreadMessageCount(borrowRequestId, userId);
        return ResponseEntity.ok(count);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // Implement this to get user ID from authentication
        // You might need to enhance your UserDetailsService to include user ID
        return 1L; // Placeholder
    }
}