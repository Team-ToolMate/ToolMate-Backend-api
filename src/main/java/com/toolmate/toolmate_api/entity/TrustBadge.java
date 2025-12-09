package com.toolmate.toolmate_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trust_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String badgeName;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String iconUrl;

    @Column(nullable = false)
    private Integer timesEarned = 1;

    @Column(nullable = false)
    private LocalDateTime earnedAt = LocalDateTime.now();
}