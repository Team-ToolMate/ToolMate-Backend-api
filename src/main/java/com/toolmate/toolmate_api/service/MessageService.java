package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.request.MessageRequest;
import com.toolmate.toolmate_api.dto.response.MessageResponse;
import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.Message;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.BorrowRequestRepository;
import com.toolmate.toolmate_api.repository.MessageRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;

    public MessageResponse sendMessage(MessageRequest request, String userEmail) {
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(request.getBorrowRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        Message message = new Message();
        message.setBorrowRequest(borrowRequest);
        message.setSender(sender);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        return new MessageResponse(
                savedMessage.getId(),
                sender.getId(),
                sender.getFullName(),
                savedMessage.getContent(),
                savedMessage.getIsRead(),
                savedMessage.getSentAt().toString()
        );
    }

    public List<MessageResponse> getMessages(Long borrowRequestId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        // Check if user is part of this conversation
        if (!borrowRequest.getBorrower().getId().equals(user.getId()) &&
                !borrowRequest.getTool().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not part of this conversation");
        }

        return messageRepository.findByBorrowRequestOrderBySentAtAsc(borrowRequest).stream()
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getSender().getFullName(),
                        message.getContent(),
                        message.getIsRead(),
                        message.getSentAt().toString()
                ))
                .collect(Collectors.toList());
    }
}