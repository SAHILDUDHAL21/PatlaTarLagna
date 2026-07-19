package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Block;
import com.patlatarlagna.entity.Profile;
import com.patlatarlagna.enums.Gender;
import com.patlatarlagna.mapper.ProfileMapper;
import com.patlatarlagna.repository.BlockRepository;
import com.patlatarlagna.repository.ProfileRepository;
import com.patlatarlagna.service.SearchService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final ProfileRepository profileRepository;
    private final BlockRepository blockRepository;
    private final ProfileMapper profileMapper;

    public SearchServiceImpl(ProfileRepository profileRepository, BlockRepository blockRepository,
                             ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.blockRepository = blockRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileDto> searchProfiles(
            Long currentUserId, String gender, String religion, String caste,
            Integer minAge, Integer maxAge, Double minHeight, Double maxHeight,
            String education, String occupation, Double minSalary, Double maxSalary,
            String city, String state, String country, Pageable pageable) {

        // Find users blocked by current user or blocking current user
        List<Long> excludedUserIds = new ArrayList<>();
        excludedUserIds.add(currentUserId); // exclude self
        
        blockRepository.findAll().forEach(block -> {
            if (block.getBlocker().getId().equals(currentUserId)) {
                excludedUserIds.add(block.getBlocked().getId());
            } else if (block.getBlocked().getId().equals(currentUserId)) {
                excludedUserIds.add(block.getBlocker().getId());
            }
        });

        Specification<Profile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude users
            predicates.add(root.get("user").get("id").in(excludedUserIds).not());

            if (gender != null && !gender.isBlank()) {
                try {
                    Gender genderEnum = Gender.valueOf(gender.toUpperCase());
                    predicates.add(cb.equal(root.get("gender"), genderEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            if (religion != null && !religion.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("religion")), religion.toLowerCase()));
            }

            if (caste != null && !caste.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("caste")), caste.toLowerCase()));
            }

            if (minAge != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
            }
            if (maxAge != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), maxAge));
            }

            if (minHeight != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("height"), minHeight));
            }
            if (maxHeight != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("height"), maxHeight));
            }

            if (education != null && !education.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("education")), "%" + education.toLowerCase() + "%"));
            }

            if (occupation != null && !occupation.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("occupation")), "%" + occupation.toLowerCase() + "%"));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("annualIncome"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("annualIncome"), maxSalary));
            }

            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }

            if (state != null && !state.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("state")), state.toLowerCase()));
            }

            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return profileRepository.findAll(spec, pageable).map(profileMapper::toDto);
    }
}
