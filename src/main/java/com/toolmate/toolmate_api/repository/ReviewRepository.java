package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.Review;
import com.toolmate.toolmate_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewee(User reviewee);
    List<Review> findByReviewer(User reviewer);
}