package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.TrustBadge;
import com.toolmate.toolmate_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrustBadgeRepository extends JpaRepository<TrustBadge, Long> {
    List<TrustBadge> findByUser(User user);
}