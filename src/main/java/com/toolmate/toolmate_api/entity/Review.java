package com.toolmate.toolmate_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(length = 1000)
    private String comment;

    private String itemConditionOnReturn; // Same, Damaged

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}