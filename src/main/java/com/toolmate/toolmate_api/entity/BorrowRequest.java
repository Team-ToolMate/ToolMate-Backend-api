package com.toolmate.toolmate_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrow_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowRequestStatus status = BorrowRequestStatus.PENDING;

    @Column(length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "borrowRequest", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    @OneToOne(mappedBy = "borrowRequest", cascade = CascadeType.ALL)
    private ConditionChecklist beforeChecklist;

    @OneToOne(mappedBy = "borrowRequest", cascade = CascadeType.ALL)
    private ConditionChecklist afterChecklist;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //Map with Status History

    @OneToMany(mappedBy = "borrowRequest", cascade = CascadeType.ALL)
    private List<StatusHistory> statusHistory = new ArrayList<>();

    // Add collected/returned timestamps
    @Column(nullable = true)
    private LocalDateTime collectedAt;

    @Column(nullable = true)
    private LocalDateTime returnedAt;

    @Column(nullable = true)
    private LocalDateTime completedAt;


}