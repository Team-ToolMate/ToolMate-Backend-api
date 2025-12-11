package com.toolmate.toolmate_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String type; // REQUEST_RECEIVED, REQUEST_ACCEPTED, REQUEST_REJECTED, TRANSACTION_COMPLETED, REVIEW_REMINDER

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = true)
    private Long relatedId; // borrow request id or tool id

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}