package com.patlatarlagna.service;

import com.patlatarlagna.dto.PhotoDto;
import com.patlatarlagna.dto.ProfileDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProfileService {
    ProfileDto getProfileByUserId(Long userId);
    ProfileDto createProfile(Long userId, ProfileDto profileDto);
    ProfileDto updateProfile(Long userId, ProfileDto profileDto);
    PhotoDto uploadPhoto(Long userId, MultipartFile file, boolean isMain);
    void deletePhoto(Long userId, Long photoId);
    List<ProfileDto> getProfileVisitors(Long userId);
}
