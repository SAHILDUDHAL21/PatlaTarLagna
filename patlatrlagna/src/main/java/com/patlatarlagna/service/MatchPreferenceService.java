package com.patlatarlagna.service;

import com.patlatarlagna.dto.MatchPreferenceDto;

public interface MatchPreferenceService {
    MatchPreferenceDto getPreferenceByUserId(Long userId);
    MatchPreferenceDto updatePreference(Long userId, MatchPreferenceDto dto);
}
