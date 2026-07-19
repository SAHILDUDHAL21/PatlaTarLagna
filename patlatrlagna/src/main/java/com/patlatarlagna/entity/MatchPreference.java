package com.patlatarlagna.entity;

import com.patlatarlagna.enums.Lifestyle;
import jakarta.persistence.*;
import lombok.*;

/**
 * Matching preferences for preferred partner search.
 */
@Entity
@Table(name = "match_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_age_min")
    private Integer preferredAgeMin;

    @Column(name = "preferred_age_max")
    private Integer preferredAgeMax;

    @Column(name = "preferred_height_min")
    private Double preferredHeightMin;

    @Column(name = "preferred_height_max")
    private Double preferredHeightMax;

    @Column(length = 50)
    private String religion;

    @Column(length = 50)
    private String caste;

    @Column(length = 100)
    private String education;

    @Column(length = 100)
    private String occupation;

    @Column(name = "income_min")
    private Double incomeMin;

    @Column(name = "income_max")
    private Double incomeMax;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Lifestyle lifestyle;
}
