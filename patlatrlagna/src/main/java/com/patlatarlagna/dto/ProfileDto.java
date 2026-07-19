package com.patlatarlagna.dto;

import com.patlatarlagna.enums.Gender;
import com.patlatarlagna.enums.Lifestyle;
import com.patlatarlagna.enums.MaritalStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {

    private Long id;
    private Long userId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private int age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Religion is required")
    private String religion;

    @NotBlank(message = "Caste is required")
    private String caste;

    private String subCaste;

    @NotBlank(message = "Mother tongue is required")
    private String motherTongue;

    @Min(value = 50, message = "Height must be realistic")
    private double height; // in cm

    @Min(value = 30, message = "Weight must be realistic")
    private double weight; // in kg

    @NotBlank(message = "Education is required")
    private String education;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    @Min(value = 0, message = "Annual income cannot be negative")
    private double annualIncome;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @Size(max = 1000, message = "About me must not exceed 1000 characters")
    private String aboutMe;

    private String hobbies;

    private Lifestyle lifestyle;

    private String familyDetails;

    private String horoscope;

    private List<PhotoDto> photos;

    private boolean verified;
}
