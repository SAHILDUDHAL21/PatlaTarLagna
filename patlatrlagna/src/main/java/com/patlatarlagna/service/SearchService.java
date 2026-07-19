package com.patlatarlagna.service;

import com.patlatarlagna.dto.ProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<ProfileDto> searchProfiles(
            Long currentUserId,
            String gender,
            String religion,
            String caste,
            Integer minAge,
            Integer maxAge,
            Double minHeight,
            Double maxHeight,
            String education,
            String occupation,
            Double minSalary,
            Double maxSalary,
            String city,
            String state,
            String country,
            Pageable pageable
    );
}
