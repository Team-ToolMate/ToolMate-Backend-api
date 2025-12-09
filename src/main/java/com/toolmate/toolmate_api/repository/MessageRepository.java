package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.BorrowRequest;
import com.toolmate.toolmate_api.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByBorrowRequestOrderBySentAtAsc(BorrowRequest borrowRequest);
    List<Message> findByBorrowRequestAndIsReadFalse(BorrowRequest borrowRequest);
}