package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByBorrowRequestOrderByChangedAtAsc(BorrowRequest borrowRequest);
}