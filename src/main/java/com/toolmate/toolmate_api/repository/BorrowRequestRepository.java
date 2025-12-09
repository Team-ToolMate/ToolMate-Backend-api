package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.BorrowRequestStatus;
import com.toolmate.toolmate_api.entity.Tool;
import com.toolmate.toolmate_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByBorrower(User borrower);
    List<BorrowRequest> findByTool(Tool tool);

    @Query("SELECT br FROM BorrowRequest br WHERE br.tool.owner = :owner")
    List<BorrowRequest> findByToolOwner(User owner);

    List<BorrowRequest> findByStatus(BorrowRequestStatus status);
    List<BorrowRequest> findByBorrowerAndStatus(User borrower, BorrowRequestStatus status);
}