package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.ConditionChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConditionChecklistRepository extends JpaRepository<ConditionChecklist, Long> {
    ConditionChecklist findByBorrowRequestAndCheckType(BorrowRequest borrowRequest, String checkType);
}