package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.request.ReviewRequest;
import com.toolmate.toolmate_api.dto.response.ReviewResponse;
import com.toolmate.toolmate_api.entity.Review;
import com.toolmate.toolmate_api.entity.Tool;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.ReviewRepository;
import com.toolmate.toolmate_api.repository.ToolRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ToolRepository toolRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String userEmail) {
        User reviewer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewee not found"));

        Tool tool = null;
        if (request.getToolId() != null) {
            tool = toolRepository.findById(request.getToolId())
                    .orElseThrow(() -> new IllegalArgumentException("Tool not found"));
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setTool(tool);
        review.setRating(request.getRating());
        review.setTags(request.getTags());
        review.setComment(request.getComment());
        review.setItemConditionOnReturn(request.getItemConditionOnReturn());

        Review savedReview = reviewRepository.save(review);

        // Update reviewee's rating
        updateUserRating(reviewee.getId());

        return new ReviewResponse(
                savedReview.getId(),
                reviewer.getFullName(),
                savedReview.getRating(),
                savedReview.getTags(),
                savedReview.getComment(),
                savedReview.getItemConditionOnReturn(),
                savedReview.getCreatedAt().toString()
        );
    }

    public List<ReviewResponse> getUserReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return reviewRepository.findByReviewee(user).stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getReviewer().getFullName(),
                        review.getRating(),
                        review.getTags(),
                        review.getComment(),
                        review.getItemConditionOnReturn(),
                        review.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    private void updateUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Review> reviews = reviewRepository.findByReviewee(user);

        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            user.setRating(averageRating);
            userRepository.save(user);
        }
    }
}