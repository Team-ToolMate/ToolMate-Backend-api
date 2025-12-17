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


    // Create new borrow request (Status: PENDING)
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

        // ✅ SEND NOTIFICATION
        notificationService.notifyRequestReceived(
                tool.getOwner(),
                borrower.getFullName(),
                tool.getName(),
                savedRequest.getId()
        );

        return convertToResponse(savedRequest);
    }


    // Owner accepts request (Status: PENDING → ACCEPTED)
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

        // ✅ SEND NOTIFICATION
        notificationService.notifyRequestAccepted(
                borrowRequest.getBorrower(),
                borrowRequest.getTool().getName(),
                owner.getFullName(),
                owner.getPhoneNumber(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Owner rejects request (Status: PENDING → REJECTED)
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

        // ✅ SEND NOTIFICATION
        notificationService.notifyRequestRejected(
                borrowRequest.getBorrower(),
                borrowRequest.getTool().getName(),
                owner.getFullName(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Borrower confirms pickup (Status: ACCEPTED → COLLECTED)
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

        // ✅ SEND NOTIFICATION
        notificationService.notifyToolCollected(
                borrowRequest.getTool().getOwner(),
                borrower.getFullName(),
                borrowRequest.getTool().getName(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Borrower confirms return (Status: COLLECTED → RETURNED)
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

        // ✅ SEND NOTIFICATION
        notificationService.notifyToolReturned(
                borrowRequest.getTool().getOwner(),
                borrower.getFullName(),
                borrowRequest.getTool().getName(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Owner confirms receipt and completes transaction (Status: RETURNED → COMPLETED)
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

        // ✅ SEND TRANSACTION COMPLETED NOTIFICATIONS TO BOTH PARTIES
        notificationService.notifyTransactionCompleted(
                borrower,
                owner.getFullName(),
                borrowRequest.getTool().getName(),
                borrowRequest.getId()
        );

        notificationService.notifyTransactionCompleted(
                owner,
                borrower.getFullName(),
                borrowRequest.getTool().getName(),
                borrowRequest.getId()
        );

        // ✅ SEND REVIEW REMINDERS TO BOTH PARTIES
        notificationService.notifyReviewReminder(
                borrower,
                owner.getFullName(),
                borrowRequest.getId()
        );

        notificationService.notifyReviewReminder(
                owner,
                borrower.getFullName(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Cancel request (Any status except COMPLETED → CANCELLED)
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

        // ✅ SEND NOTIFICATION TO OTHER PARTY
        User otherUser = borrowRequest.getBorrower().getId().equals(user.getId())
                ? borrowRequest.getTool().getOwner()
                : borrowRequest.getBorrower();

        notificationService.notifyRequestCancelled(
                otherUser,
                user.getFullName(),
                borrowRequest.getTool().getName(),
                borrowRequest.getId()
        );

        return convertToResponse(updated);
    }


    // Get status timeline
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