package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.response.ChatMessage;
import com.toolmate.toolmate_api.dto.response.TypingIndicator;
import com.toolmate.toolmate_api.entity.Message;
import com.toolmate.toolmate_api.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * Handle incoming chat messages
     * Client sends to: /app/chat.send
     * Server broadcasts to: /topic/borrow-request/{borrowRequestId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            log.info("Received message from user: {} for borrow request: {}",
                    principal.getName(), chatMessage.getBorrowRequestId());

            // Set timestamp
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setStatus(ChatMessage.MessageStatus.SENT);

            // Save message to database
            Message savedMessage = chatService.saveMessage(chatMessage, principal.getName());
            chatMessage.setId(savedMessage.getId());

            // Send to specific conversation room
            String destination = "/topic/borrow-request/" + chatMessage.getBorrowRequestId();
            messagingTemplate.convertAndSend(destination, chatMessage);

            // Also send to recipient's personal queue
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId().toString(),
                    "/queue/messages",
                    chatMessage
            );

            log.info("Message sent successfully to: {}", destination);

        } catch (Exception e) {
            log.error("Error sending message: ", e);
        }
    }

    /**
     * Handle typing indicators
     * Client sends to: /app/chat.typing
     * Server broadcasts to: /topic/borrow-request/{borrowRequestId}/typing
     */
    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload TypingIndicator indicator) {
        String destination = "/topic/borrow-request/" +
                indicator.getBorrowRequestId() + "/typing";
        messagingTemplate.convertAndSend(destination, indicator);

        log.info("Typing indicator sent for user: {} in request: {}",
                indicator.getUserName(), indicator.getBorrowRequestId());
    }

    /**
     * Handle user joining conversation
     * Client sends to: /app/chat.join
     */
    @MessageMapping("/chat.join")
    public void joinConversation(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());

        String destination = "/topic/borrow-request/" + chatMessage.getBorrowRequestId();
        messagingTemplate.convertAndSend(destination, chatMessage);

        log.info("User {} joined conversation {}",
                chatMessage.getSenderName(), chatMessage.getBorrowRequestId());
    }

    /**
     * Handle user leaving conversation
     * Client sends to: /app/chat.leave
     */
    @MessageMapping("/chat.leave")
    public void leaveConversation(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setTimestamp(LocalDateTime.now());

        String destination = "/topic/borrow-request/" + chatMessage.getBorrowRequestId();
        messagingTemplate.convertAndSend(destination, chatMessage);

        log.info("User {} left conversation {}",
                chatMessage.getSenderName(), chatMessage.getBorrowRequestId());
    }

    /**
     * Mark messages as read
     * Client sends to: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ChatMessage chatMessage, Principal principal) {
        chatService.markMessagesAsRead(
                chatMessage.getBorrowRequestId(),
                Long.parseLong(principal.getName())
        );

        // Notify sender that message was read
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId().toString(),
                "/queue/read-receipt",
                chatMessage
        );
    }
}





// ===================== For Normal Testing ==============================


//package com.toolmate.toolmate_api.controller;
//
//import com.toolmate.toolmate_api.dto.response.ChatMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.time.LocalDateTime;
//
//@Controller
//@Slf4j
//public class ChatController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public ChatController(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    /**
//     * Handle incoming chat messages
//     * Client sends to: /app/chat.send
//     * Server broadcasts to: /topic/borrow-request/{borrowRequestId}
//     */
//    @MessageMapping("/chat.send")
//    public void sendMessage(@Payload ChatMessage chatMessage) {
//        try {
//            // Set timestamp
//            chatMessage.setTimestamp(LocalDateTime.now());
//
//            // Log for server console
//            log.info("Message from {} to borrowRequest {}: {}",
//                    chatMessage.getSenderName(),
//                    chatMessage.getBorrowRequestId(),
//                    chatMessage.getContent());
//
//            // Send to all users subscribed to the room
//            String destination = "/topic/borrow-request/" + chatMessage.getBorrowRequestId();
//            messagingTemplate.convertAndSend(destination, chatMessage);
//
//        } catch (Exception e) {
//            log.error("Error sending message: ", e);
//        }
//    }
//
//    /**
//     * Optional: Handle typing indicator
//     * Client sends to: /app/chat.typing
//     */
//    @MessageMapping("/chat.typing")
//    public void typingIndicator(@Payload ChatMessage chatMessage) {
//        String destination = "/topic/borrow-request/" + chatMessage.getBorrowRequestId() + "/typing";
//        messagingTemplate.convertAndSend(destination, chatMessage);
//        log.info("Typing indicator from {} in borrowRequest {}",
//                chatMessage.getSenderName(), chatMessage.getBorrowRequestId());
//    }
//}
