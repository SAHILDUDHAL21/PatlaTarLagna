package com.patlatarlagna.entity;

import com.patlatarlagna.enums.Gender;
import com.patlatarlagna.enums.Lifestyle;
import com.patlatarlagna.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed user matrimonial profile.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false, length = 50)
    private String religion;

    @Column(nullable = false, length = 50)
    private String caste;

    @Column(name = "sub_caste", length = 50)
    private String subCaste;

    @Column(name = "mother_tongue", nullable = false, length = 50)
    private String motherTongue;

    @Column(nullable = false)
    private double height; // in cm

    @Column(nullable = false)
    private double weight; // in kg

    @Column(nullable = false, length = 100)
    private String education;

    @Column(nullable = false, length = 100)
    private String occupation;

    @Column(name = "annual_income", nullable = false)
    private double annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 30)
    private MaritalStatus maritalStatus;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(name = "about_me", length = 1000)
    private String aboutMe;

    @Column(length = 500)
    private String hobbies;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Lifestyle lifestyle;

    @Column(name = "family_details", length = 1000)
    private String familyDetails;

    @Column(length = 200)
    private String horoscope;

    @Builder.Default
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Photo> photos = new ArrayList<>();

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
