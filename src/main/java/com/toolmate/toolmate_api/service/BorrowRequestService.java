package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.request.BorrowRequestRequest;
import com.toolmate.toolmate_api.dto.response.BorrowRequestResponse;
import com.toolmate.toolmate_api.dto.response.OwnerDTO;
import com.toolmate.toolmate_api.dto.response.ToolResponse;
import com.toolmate.toolmate_api.dto.response.UserDTO;
import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.BorrowRequestStatus;
import com.toolmate.toolmate_api.entity.Tool;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.BorrowRequestRepository;
import com.toolmate.toolmate_api.repository.ToolRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public BorrowRequestResponse createBorrowRequest(BorrowRequestRequest request, String userEmail) {
        User borrower = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tool tool = toolRepository.findById(request.getToolId())
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        if (!tool.getIsAvailable()) {
            throw new IllegalArgumentException("Tool is not available");
        }

        if (tool.getOwner().getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("You cannot borrow your own tool");
        }

        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setTool(tool);
        borrowRequest.setBorrower(borrower);
        borrowRequest.setStartDate(request.getStartDate());
        borrowRequest.setEndDate(request.getEndDate());
        borrowRequest.setMessage(request.getMessage());
        borrowRequest.setStatus(BorrowRequestStatus.PENDING);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        // Send notification to tool owner
        notificationService.createNotification(
                tool.getOwner(),
                "New Borrow Request",
                borrower.getFullName() + " wants to borrow your " + tool.getName(),
                "REQUEST_RECEIVED",
                savedRequest.getId()
        );

        return convertToResponse(savedRequest);
    }

    public List<BorrowRequestResponse> getMyBorrowRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return borrowRequestRepository.findByBorrower(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getRequestsForMyTools(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return borrowRequestRepository.findByToolOwner(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BorrowRequestResponse updateRequestStatus(Long requestId, String status, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        if (!borrowRequest.getTool().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the tool owner can update the request status");
        }

        // Validate status
        BorrowRequestStatus newStatus;
        try {
            newStatus = BorrowRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        borrowRequest.setStatus(newStatus);

        // Update tool availability based on status
        if (newStatus == BorrowRequestStatus.ACCEPTED) {
            borrowRequest.getTool().setIsAvailable(false);
            toolRepository.save(borrowRequest.getTool());

            // Notify borrower that request is accepted
            notificationService.createNotification(
                    borrowRequest.getBorrower(),
                    "Request Accepted",
                    "Your request for " + borrowRequest.getTool().getName() + " has been accepted",
                    "REQUEST_ACCEPTED",
                    borrowRequest.getId()
            );

        } else if (newStatus == BorrowRequestStatus.COMPLETED ||
                newStatus == BorrowRequestStatus.CANCELLED ||
                newStatus == BorrowRequestStatus.REJECTED) {
            borrowRequest.getTool().setIsAvailable(true);
            toolRepository.save(borrowRequest.getTool());

            if (newStatus == BorrowRequestStatus.COMPLETED) {
                // Notify both to write reviews
                notificationService.createNotification(
                        borrowRequest.getBorrower(),
                        "Review Reminder",
                        "Please review your experience with " + borrowRequest.getTool().getOwner().getFullName(),
                        "REVIEW_REMINDER",
                        borrowRequest.getId()
                );
                notificationService.createNotification(
                        borrowRequest.getTool().getOwner(),
                        "Review Reminder",
                        "Please review your experience with " + borrowRequest.getBorrower().getFullName(),
                        "REVIEW_REMINDER",
                        borrowRequest.getId()
                );
            }
        }

        BorrowRequest updatedRequest = borrowRequestRepository.save(borrowRequest);
        return convertToResponse(updatedRequest);
    }

    private BorrowRequestResponse convertToResponse(BorrowRequest borrowRequest) {
        Tool tool = borrowRequest.getTool();
        User borrower = borrowRequest.getBorrower();

        ToolResponse toolResponse = new ToolResponse(
                tool.getId(),
                tool.getName(),
                tool.getDescription(),
                tool.getCategory(),
                tool.getCondition(),
                tool.getImageUrls(),
                tool.getIsAvailable(),
                tool.getRentalFee(),
                tool.getRateType(),
                tool.getIsFullyCharged(),
                0.0,
                tool.getRating(),
                tool.getTotalBorrows(),
                new OwnerDTO(
                        tool.getOwner().getId(),
                        tool.getOwner().getFullName(),
                        tool.getOwner().getRating(),
                        tool.getOwner().getProfileImageUrl()
                ),
                tool.getCreatedAt()
        );

        UserDTO borrowerDTO = new UserDTO(
                borrower.getId(),
                borrower.getFullName(),
                borrower.getEmail(),
                borrower.getPhoneNumber(),
                borrower.getLatitude(),
                borrower.getLongitude(),
                borrower.getAddress(),
                borrower.getProfileImageUrl(),
                borrower.getRating(),
                borrower.getTotalBorrows(),
                borrower.getTotalLends()
        );

        return new BorrowRequestResponse(
                borrowRequest.getId(),
                toolResponse,
                borrowerDTO,
                borrowRequest.getStartDate(),
                borrowRequest.getEndDate(),
                borrowRequest.getStatus().name(),
                borrowRequest.getMessage(),
                borrowRequest.getCreatedAt()
        );
    }
}
