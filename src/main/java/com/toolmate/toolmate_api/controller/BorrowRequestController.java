package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.BorrowRequestRequest;
import com.toolmate.toolmate_api.dto.response.BorrowRequestResponse;
import com.toolmate.toolmate_api.dto.response.StatusHistoryDTO;
import com.toolmate.toolmate_api.service.BorrowRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-requests")
@Tag(name = "Borrow Requests", description = "Complete borrowing lifecycle management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @PostMapping
    @Operation(summary = "Create new borrow request (Status: PENDING)")
    public ResponseEntity<BorrowRequestResponse> createBorrowRequest(
            @RequestBody BorrowRequestRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.createBorrowRequest(request, authentication.getName()));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get my borrow requests")
    public ResponseEntity<List<BorrowRequestResponse>> getMyBorrowRequests(Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.getMyBorrowRequests(authentication.getName()));
    }

    @GetMapping("/for-my-tools")
    @Operation(summary = "Get borrow requests for my tools")
    public ResponseEntity<List<BorrowRequestResponse>> getRequestsForMyTools(Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.getRequestsForMyTools(authentication.getName()));
    }

    // ========== LIFECYCLE ACTIONS ==========

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept request (PENDING - ACCEPTED) [Owner only]")
    public ResponseEntity<BorrowRequestResponse> acceptRequest(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.acceptRequest(id, authentication.getName()));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject request (PENDING - REJECTED) [Owner only]")
    public ResponseEntity<BorrowRequestResponse> rejectRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.rejectRequest(id, authentication.getName(), reason));
    }

    @PutMapping("/{id}/collected")
    @Operation(summary = "Confirm tool collected (ACCEPTED - COLLECTED) [Borrower only]")
    public ResponseEntity<BorrowRequestResponse> confirmCollected(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.confirmCollected(id, authentication.getName()));
    }

    @PutMapping("/{id}/returned")
    @Operation(summary = "Confirm tool returned (COLLECTED - RETURNED) [Borrower only]")
    public ResponseEntity<BorrowRequestResponse> confirmReturned(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.confirmReturned(id, authentication.getName()));
    }

    @PutMapping("/{id}/confirm-receipt")
    @Operation(summary = "Confirm tool received (RETURNED - COMPLETED) [Owner only]")
    public ResponseEntity<BorrowRequestResponse> confirmReceipt(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.confirmReceipt(id, authentication.getName()));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel request (Any status - CANCELLED)")
    public ResponseEntity<BorrowRequestResponse> cancelRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.cancelRequest(id, authentication.getName(), reason));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get complete status timeline")
    public ResponseEntity<List<StatusHistoryDTO>> getStatusTimeline(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.getStatusTimeline(id, authentication.getName()));
    }
}