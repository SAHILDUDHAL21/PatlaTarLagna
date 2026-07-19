package com.patlatarlagna.dto;

import com.patlatarlagna.enums.Lifestyle;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPreferenceDto {
    private Long id;
    private Long userId;
    private Integer preferredAgeMin;
    private Integer preferredAgeMax;
    private Double preferredHeightMin;
    private Double preferredHeightMax;
    private String religion;
    private String caste;
    private String education;
    private String occupation;
    private Double incomeMin;
    private Double incomeMax;
    private String city;
    private String state;
    private String country;
    private Lifestyle lifestyle;
}
