package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.*;
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

        borrowRequest.setStatus(BorrowRequestStatus.valueOf(status));

        if (status.equals("ACCEPTED")) {
            borrowRequest.getTool().setIsAvailable(false);
            toolRepository.save(borrowRequest.getTool());
        } else if (status.equals("COMPLETED") || status.equals("CANCELLED") || status.equals("REJECTED")) {
            borrowRequest.getTool().setIsAvailable(true);
            toolRepository.save(borrowRequest.getTool());
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