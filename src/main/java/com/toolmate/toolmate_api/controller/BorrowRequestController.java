package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.BorrowRequestRequest;
import com.toolmate.toolmate_api.dto.response.BorrowRequestResponse;
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
@Tag(name = "Borrow Requests", description = "Manage tool borrow requests")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @PostMapping
    @Operation(summary = "Create a new borrow request")
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

    @PutMapping("/{id}/status")
    @Operation(summary = "Update borrow request status (ACCEPTED, REJECTED, etc.)")
    public ResponseEntity<BorrowRequestResponse> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        return ResponseEntity.ok(borrowRequestService.updateRequestStatus(id, status, authentication.getName()));
    }
}