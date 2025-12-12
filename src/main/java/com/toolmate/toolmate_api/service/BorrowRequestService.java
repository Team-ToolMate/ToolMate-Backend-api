package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.request.BorrowRequestRequest;
import com.toolmate.toolmate_api.dto.response.BorrowRequestResponse;
import com.toolmate.toolmate_api.dto.response.StatusHistoryDTO;
import com.toolmate.toolmate_api.dto.response.OwnerDTO;
import com.toolmate.toolmate_api.dto.response.ToolResponse;
import com.toolmate.toolmate_api.dto.response.UserDTO;
import com.toolmate.toolmate_api.entity.*;
import com.toolmate.toolmate_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;


//     Create new borrow request (Status: PENDING)

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

        // Create status history entry
        createStatusHistory(savedRequest, BorrowRequestStatus.PENDING, borrower, "Request created");

        // Notify owner
        notificationService.createNotification(
                tool.getOwner(),
                "New Borrow Request",
                borrower.getFullName() + " wants to borrow your " + tool.getName(),
                "REQUEST_RECEIVED",
                savedRequest.getId()
        );

        return convertToResponse(savedRequest);
    }


//     Owner accepts request (Status: PENDING → ACCEPTED)

    @Transactional
    public BorrowRequestResponse acceptRequest(Long requestId, String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        // Validate owner
        if (!borrowRequest.getTool().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the tool owner can accept this request");
        }

        // Validate current status
        if (borrowRequest.getStatus() != BorrowRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only accept pending requests");
        }

        // Update status
        borrowRequest.setStatus(BorrowRequestStatus.ACCEPTED);
        borrowRequest.getTool().setIsAvailable(false);
        toolRepository.save(borrowRequest.getTool());

        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        // Create status history
        createStatusHistory(updated, BorrowRequestStatus.ACCEPTED, owner, "Request accepted by owner");

        // Notify borrower
        notificationService.createNotification(
                borrowRequest.getBorrower(),
                "Request Accepted! ",
                "Your request for " + borrowRequest.getTool().getName() + " has been accepted. Contact: " + owner.getPhoneNumber(),
                "REQUEST_ACCEPTED",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Owner rejects request (Status: PENDING → REJECTED)

    @Transactional
    public BorrowRequestResponse rejectRequest(Long requestId, String userEmail, String reason) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        if (!borrowRequest.getTool().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the tool owner can reject this request");
        }

        if (borrowRequest.getStatus() != BorrowRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only reject pending requests");
        }

        borrowRequest.setStatus(BorrowRequestStatus.REJECTED);
        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        createStatusHistory(updated, BorrowRequestStatus.REJECTED, owner, reason);

        notificationService.createNotification(
                borrowRequest.getBorrower(),
                "Request Declined",
                "Your request for " + borrowRequest.getTool().getName() + " was declined",
                "REQUEST_REJECTED",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Borrower confirms pickup (Status: ACCEPTED → COLLECTED)

    @Transactional
    public BorrowRequestResponse confirmCollected(Long requestId, String userEmail) {
        User borrower = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        if (!borrowRequest.getBorrower().getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("Only the borrower can confirm collection");
        }

        if (borrowRequest.getStatus() != BorrowRequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("Can only collect accepted requests");
        }

        borrowRequest.setStatus(BorrowRequestStatus.COLLECTED);
        borrowRequest.setCollectedAt(LocalDateTime.now());
        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        createStatusHistory(updated, BorrowRequestStatus.COLLECTED, borrower, "Tool collected by borrower");

        notificationService.createNotification(
                borrowRequest.getTool().getOwner(),
                "Tool Collected",
                borrower.getFullName() + " has collected your " + borrowRequest.getTool().getName(),
                "TOOL_COLLECTED",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Borrower confirms return (Status: COLLECTED → RETURNED)
    @Transactional
    public BorrowRequestResponse confirmReturned(Long requestId, String userEmail) {
        User borrower = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        if (!borrowRequest.getBorrower().getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("Only the borrower can confirm return");
        }

        if (borrowRequest.getStatus() != BorrowRequestStatus.COLLECTED) {
            throw new IllegalArgumentException("Can only return collected tools");
        }

        borrowRequest.setStatus(BorrowRequestStatus.RETURNED);
        borrowRequest.setReturnedAt(LocalDateTime.now());
        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        createStatusHistory(updated, BorrowRequestStatus.RETURNED, borrower, "Tool returned by borrower");

        notificationService.createNotification(
                borrowRequest.getTool().getOwner(),
                "Tool Returned ",
                borrower.getFullName() + " has returned your " + borrowRequest.getTool().getName() + ". Please confirm receipt.",
                "TOOL_RETURNED",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Owner confirms receipt and completes transaction (Status: RETURNED → COMPLETED)

    @Transactional
    public BorrowRequestResponse confirmReceipt(Long requestId, String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        if (!borrowRequest.getTool().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the tool owner can confirm receipt");
        }

        if (borrowRequest.getStatus() != BorrowRequestStatus.RETURNED) {
            throw new IllegalArgumentException("Can only confirm receipt of returned tools");
        }

        borrowRequest.setStatus(BorrowRequestStatus.COMPLETED);
        borrowRequest.setCompletedAt(LocalDateTime.now());
        borrowRequest.getTool().setIsAvailable(true);
        borrowRequest.getTool().setTotalBorrows(borrowRequest.getTool().getTotalBorrows() + 1);

        toolRepository.save(borrowRequest.getTool());
        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        createStatusHistory(updated, BorrowRequestStatus.COMPLETED, owner, "Transaction completed");

        // Update user statistics
        User borrower = borrowRequest.getBorrower();
        borrower.setTotalBorrows(borrower.getTotalBorrows() + 1);
        owner.setTotalLends(owner.getTotalLends() + 1);
        userRepository.save(borrower);
        userRepository.save(owner);

        // Notify both to write reviews
        notificationService.createNotification(
                borrower,
                "Transaction Complete! ",
                "Please rate your experience with " + owner.getFullName(),
                "REVIEW_REMINDER",
                updated.getId()
        );

        notificationService.createNotification(
                owner,
                "Transaction Complete!",
                "Please rate your experience with " + borrower.getFullName(),
                "REVIEW_REMINDER",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Cancel request (Any status except COMPLETED → CANCELLED)

    @Transactional
    public BorrowRequestResponse cancelRequest(Long requestId, String userEmail, String reason) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        // Validate user is part of this request
        if (!borrowRequest.getBorrower().getId().equals(user.getId()) &&
                !borrowRequest.getTool().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot cancel this request");
        }

        if (borrowRequest.getStatus() == BorrowRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed transactions");
        }

        borrowRequest.setStatus(BorrowRequestStatus.CANCELLED);

        // Make tool available again
        borrowRequest.getTool().setIsAvailable(true);
        toolRepository.save(borrowRequest.getTool());

        BorrowRequest updated = borrowRequestRepository.save(borrowRequest);

        createStatusHistory(updated, BorrowRequestStatus.CANCELLED, user, reason);

        // Notify the other party
        User otherUser = borrowRequest.getBorrower().getId().equals(user.getId())
                ? borrowRequest.getTool().getOwner()
                : borrowRequest.getBorrower();

        notificationService.createNotification(
                otherUser,
                "Request Cancelled",
                user.getFullName() + " cancelled the request for " + borrowRequest.getTool().getName(),
                "REQUEST_CANCELLED",
                updated.getId()
        );

        return convertToResponse(updated);
    }


//     Get status timeline

    public List<StatusHistoryDTO> getStatusTimeline(Long requestId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow request not found"));

        // Validate user is part of this request
        if (!borrowRequest.getBorrower().getId().equals(user.getId()) &&
                !borrowRequest.getTool().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot view this timeline");
        }

        List<StatusHistory> history = statusHistoryRepository.findByBorrowRequestOrderByChangedAtAsc(borrowRequest);

        return history.stream()
                .map(h -> new StatusHistoryDTO(
                        h.getStatus().name(),
                        h.getChangedBy().getFullName(),
                        h.getNotes(),
                        h.getChangedAt()
                ))
                .collect(Collectors.toList());
    }

    // Helper methods
    private void createStatusHistory(BorrowRequest borrowRequest, BorrowRequestStatus status,
                                     User changedBy, String notes) {
        StatusHistory history = new StatusHistory();
        history.setBorrowRequest(borrowRequest);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        statusHistoryRepository.save(history);
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