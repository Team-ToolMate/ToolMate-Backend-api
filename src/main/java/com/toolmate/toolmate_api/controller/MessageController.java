package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.MessageRequest;
import com.toolmate.toolmate_api.dto.response.MessageResponse;
import com.toolmate.toolmate_api.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "In-app messaging between borrowers and lenders")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Send a message")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.sendMessage(request, authentication.getName()));
    }

    @GetMapping("/borrow-request/{borrowRequestId}")
    @Operation(summary = "Get all messages for a borrow request")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long borrowRequestId,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getMessages(borrowRequestId, authentication.getName()));
    }
}