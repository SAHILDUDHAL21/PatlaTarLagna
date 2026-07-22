package com.patlatarlagna.config;

import com.patlatarlagna.entity.MatchPreference;
import com.patlatarlagna.entity.Profile;
import com.patlatarlagna.entity.Role;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.enums.Gender;
import com.patlatarlagna.enums.Lifestyle;
import com.patlatarlagna.enums.MaritalStatus;
import com.patlatarlagna.enums.RoleType;
import com.patlatarlagna.repository.MatchPreferenceRepository;
import com.patlatarlagna.repository.ProfileRepository;
import com.patlatarlagna.repository.RoleRepository;
import com.patlatarlagna.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final MatchPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, RoleRepository roleRepository,
                          ProfileRepository profileRepository, MatchPreferenceRepository preferenceRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
        this.preferenceRepository = preferenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name(RoleType.ADMIN).build());
            roleRepository.save(Role.builder().name(RoleType.NORMAL_USER).build());
        }

        Role adminRole = roleRepository.findByName(RoleType.ADMIN).orElse(null);
        Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElse(null);

        // 2. Seed Admin
        if (userRepository.findByEmail("admin@patlatarlagna.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@patlatarlagna.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            
            // Build admin profile
            Profile adminProfile = Profile.builder()
                    .user(admin)
                    .name("System Administrator")
                    .age(30)
                    .gender(Gender.OTHER)
                    .religion("Universal")
                    .caste("Admin")
                    .motherTongue("English")
                    .height(175.0)
                    .weight(70.0)
                    .education("M.S. Software Engineering")
                    .occupation("System Admin")
                    .annualIncome(1500000.0)
                    .maritalStatus(MaritalStatus.NEVER_MARRIED)
                    .city("Pune")
                    .state("Maharashtra")
                    .country("India")
                    .aboutMe("System administrator for PatlaTarLagna matrimonial matchmaking platform.")
                    .verified(true)
                    .build();
            profileRepository.save(adminProfile);
        }

        // 3. Seed Mock Users for Matching and Searching
        seedMockUser("priya@example.com", "priya123", "Priya Sharma", 25, Gender.FEMALE, "Hindu", "Brahmin", "Hindi", 162, 52, "B.Tech", "Software Engineer", 1200000, MaritalStatus.NEVER_MARRIED, "Pune", "Maharashtra", "India", "Software dev seeking partner who likes tech.", normalRole, 24, 28, 170.0, 185.0);
        seedMockUser("rahul@example.com", "rahul123", "Rahul Patil", 27, Gender.MALE, "Hindu", "Maratha", "Marathi", 178, 72, "MBA", "Product Manager", 1800000, MaritalStatus.NEVER_MARRIED, "Mumbai", "Maharashtra", "India", "Looking for simple and educated match.", normalRole, 23, 27, 155.0, 168.0);
        seedMockUser("sneha@example.com", "sneha123", "Sneha Kulkarni", 26, Gender.FEMALE, "Hindu", "Brahmin", "Marathi", 158, 50, "M.B.B.S", "Doctor", 2000000, MaritalStatus.NEVER_MARRIED, "Pune", "Maharashtra", "India", "Cardiologist looking for compatible doctor or engineer.", normalRole, 26, 30, 172.0, 185.0);
        seedMockUser("amit@example.com", "amit123", "Amit Deshmukh", 29, Gender.MALE, "Hindu", "Maratha", "Marathi", 175, 75, "B.Com", "Business Owner", 2400000, MaritalStatus.NEVER_MARRIED, "Pune", "Maharashtra", "India", "Entrepreneur seeking a life partner.", normalRole, 24, 28, 155.0, 170.0);
    }

    private void seedMockUser(String email, String rawPassword, String name, int age, Gender gender,
                              String religion, String caste, String motherTongue, double height, double weight,
                              String education, String occupation, double income, MaritalStatus maritalStatus,
                              String city, String state, String country, String about, Role role,
                              int prefMinAge, int prefMaxAge, double prefMinHeight, double prefMaxHeight) {
        
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .roles(Set.of(role))
                    .build();
            userRepository.save(user);

            Profile profile = Profile.builder()
                    .user(user)
                    .name(name)
                    .age(age)
                    .gender(gender)
                    .religion(religion)
                    .caste(caste)
                    .motherTongue(motherTongue)
                    .height(height)
                    .weight(weight)
                    .education(education)
                    .occupation(occupation)
                    .annualIncome(income)
                    .maritalStatus(maritalStatus)
                    .city(city)
                    .state(state)
                    .country(country)
                    .aboutMe(about)
                    .verified(true)
                    .lifestyle(Lifestyle.VEGETARIAN)
                    .build();
            profileRepository.save(profile);

            MatchPreference pref = MatchPreference.builder()
                    .user(user)
                    .preferredAgeMin(prefMinAge)
                    .preferredAgeMax(prefMaxAge)
                    .preferredHeightMin(prefMinHeight)
                    .preferredHeightMax(prefMaxHeight)
                    .religion(religion)
                    .caste(caste)
                    .build();
            preferenceRepository.save(pref);
        }
    }
}
