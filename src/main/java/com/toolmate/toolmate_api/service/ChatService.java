package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.response.ChatMessage;
import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.Message;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.BorrowRequestRepository;
import com.toolmate.toolmate_api.repository.MessageRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    /**
     * Save message to database
     */
    @Transactional
    public Message saveMessage(ChatMessage chatMessage, String userEmail) {
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(chatMessage.getBorrowRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        Message message = new Message();
        message.setBorrowRequest(borrowRequest);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setIsRead(false);
        message.setSentAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Get chat history for a borrow request
     */
    public List<ChatMessage> getChatHistory(Long borrowRequestId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        // Verify user is part of this conversation
        if (!borrowRequest.getBorrower().getId().equals(user.getId()) &&
                !borrowRequest.getTool().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not part of this conversation");
        }

        List<Message> messages = messageRepository.findByBorrowRequestOrderBySentAtAsc(borrowRequest);

        return messages.stream()
                .map(this::convertToChatMessage)
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long borrowRequestId, Long userId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        List<Message> unreadMessages = messageRepository.findByBorrowRequestAndIsReadFalse(borrowRequest);

        unreadMessages.stream()
                .filter(msg -> !msg.getSender().getId().equals(userId))
                .forEach(msg -> msg.setIsRead(true));

        messageRepository.saveAll(unreadMessages);
    }

    /**
     * Get unread message count
     */
    public long getUnreadMessageCount(Long borrowRequestId, Long userId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        List<Message> unreadMessages = messageRepository.findByBorrowRequestAndIsReadFalse(borrowRequest);

        return unreadMessages.stream()
                .filter(msg -> !msg.getSender().getId().equals(userId))
                .count();
    }

    /**
     * Convert Message entity to ChatMessage DTO
     */
    private ChatMessage convertToChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(message.getId());
        chatMessage.setBorrowRequestId(message.getBorrowRequest().getId());
        chatMessage.setSenderId(message.getSender().getId());
        chatMessage.setSenderName(message.getSender().getFullName());
        chatMessage.setSenderImageUrl(message.getSender().getProfileImageUrl());
        chatMessage.setContent(message.getContent());
        chatMessage.setTimestamp(message.getSentAt());
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatMessage.setStatus(message.getIsRead() ?
                ChatMessage.MessageStatus.READ : ChatMessage.MessageStatus.DELIVERED);
        return chatMessage;
    }
}