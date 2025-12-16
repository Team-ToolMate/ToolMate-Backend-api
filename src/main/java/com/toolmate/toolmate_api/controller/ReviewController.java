package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.ReviewRequest;
import com.toolmate.toolmate_api.dto.response.ReviewResponse;
import com.toolmate.toolmate_api.service.ReviewService;
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
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "User rating and review system")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a review for a user")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.createReview(request, authentication.getName()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all reviews for a user")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }
}