package com.toolmate.toolmate_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "condition_checklists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "borrow_request_id", nullable = false)
    private BorrowRequest borrowRequest;

    @Column(nullable = false)
    private String checkType; // BEFORE or AFTER

    @Column(nullable = false)
    private Boolean workingProperly = false;

    @Column(nullable = false)
    private Boolean hasScratchesOrDamage = false;

    @Column(nullable = false)
    private Boolean hasBrokenParts = false;

    @Column(nullable = false)
    private Boolean needsCharging = false;

    @Column(nullable = false)
    private Boolean cleanAndMaintained = false;

    @ElementCollection
    @CollectionTable(name = "checklist_photos", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    @Column(length = 500)
    private String additionalNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}