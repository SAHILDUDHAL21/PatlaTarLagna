package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.MatchPreferenceDto;
import com.patlatarlagna.entity.MatchPreference;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.MatchPreferenceMapper;
import com.patlatarlagna.repository.MatchPreferenceRepository;
import com.patlatarlagna.repository.UserRepository;
import com.patlatarlagna.service.MatchPreferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchPreferenceServiceImpl implements MatchPreferenceService {

    private final MatchPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final MatchPreferenceMapper preferenceMapper;

    public MatchPreferenceServiceImpl(MatchPreferenceRepository preferenceRepository, UserRepository userRepository,
                                      MatchPreferenceMapper preferenceMapper) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.preferenceMapper = preferenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchPreferenceDto getPreferenceByUserId(Long userId) {
        MatchPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create default preferences if none exist yet
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    MatchPreference newPref = MatchPreference.builder().user(user).build();
                    return preferenceRepository.save(newPref);
                });
        return preferenceMapper.toDto(preference);
    }

    @Override
    @Transactional
    public MatchPreferenceDto updatePreference(Long userId, MatchPreferenceDto dto) {
        MatchPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found for user: " + userId));

        preference.setPreferredAgeMin(dto.getPreferredAgeMin());
        preference.setPreferredAgeMax(dto.getPreferredAgeMax());
        preference.setPreferredHeightMin(dto.getPreferredHeightMin());
        preference.setPreferredHeightMax(dto.getPreferredHeightMax());
        preference.setReligion(dto.getReligion());
        preference.setCaste(dto.getCaste());
        preference.setEducation(dto.getEducation());
        preference.setOccupation(dto.getOccupation());
        preference.setIncomeMin(dto.getIncomeMin());
        preference.setIncomeMax(dto.getIncomeMax());
        preference.setCity(dto.getCity());
        preference.setState(dto.getState());
        preference.setCountry(dto.getCountry());
        preference.setLifestyle(dto.getLifestyle());

        MatchPreference saved = preferenceRepository.save(preference);
        return preferenceMapper.toDto(saved);
    }
}
