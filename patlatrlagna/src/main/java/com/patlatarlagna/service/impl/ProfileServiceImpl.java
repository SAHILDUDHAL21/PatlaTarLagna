package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.PhotoDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Photo;
import com.patlatarlagna.entity.Profile;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.PhotoMapper;
import com.patlatarlagna.mapper.ProfileMapper;
import com.patlatarlagna.repository.PhotoRepository;
import com.patlatarlagna.repository.ProfileRepository;
import com.patlatarlagna.repository.UserRepository;
import com.patlatarlagna.service.FileStorageService;
import com.patlatarlagna.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final FileStorageService fileStorageService;
    private final ProfileMapper profileMapper;
    private final PhotoMapper photoMapper;

    public ProfileServiceImpl(UserRepository userRepository, ProfileRepository profileRepository,
                              PhotoRepository photoRepository, FileStorageService fileStorageService,
                              ProfileMapper profileMapper, PhotoMapper photoMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.photoRepository = photoRepository;
        this.fileStorageService = fileStorageService;
        this.profileMapper = profileMapper;
        this.photoMapper = photoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public ProfileDto createProfile(Long userId, ProfileDto profileDto) {
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Profile already exists for user: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = profileMapper.toEntity(profileDto);
        profile.setUser(user);
        profile.setVerified(false); // verification required by admin

        Profile saved = profileRepository.save(profile);
        return profileMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(Long userId, ProfileDto profileDto) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        profile.setName(profileDto.getName());
        profile.setAge(profileDto.getAge());
        profile.setGender(profileDto.getGender());
        profile.setReligion(profileDto.getReligion());
        profile.setCaste(profileDto.getCaste());
        profile.setSubCaste(profileDto.getSubCaste());
        profile.setMotherTongue(profileDto.getMotherTongue());
        profile.setHeight(profileDto.getHeight());
        profile.setWeight(profileDto.getWeight());
        profile.setEducation(profileDto.getEducation());
        profile.setOccupation(profileDto.getOccupation());
        profile.setAnnualIncome(profileDto.getAnnualIncome());
        profile.setMaritalStatus(profileDto.getMaritalStatus());
        profile.setCity(profileDto.getCity());
        profile.setState(profileDto.getState());
        profile.setCountry(profileDto.getCountry());
        profile.setAboutMe(profileDto.getAboutMe());
        profile.setHobbies(profileDto.getHobbies());
        profile.setLifestyle(profileDto.getLifestyle());
        profile.setFamilyDetails(profileDto.getFamilyDetails());
        profile.setHoroscope(profileDto.getHoroscope());

        Profile saved = profileRepository.save(profile);
        return profileMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PhotoDto uploadPhoto(Long userId, MultipartFile file, boolean isMain) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create profile first."));

        if (profile.getPhotos().size() >= 5) {
            throw new BadRequestException("You can upload at most 5 photos.");
        }

        String fileName = fileStorageService.storeFile(file);
        
        // If this is set to main, set all other photos to not main
        if (isMain) {
            profile.getPhotos().forEach(p -> p.setMain(false));
        }

        Photo photo = Photo.builder()
                .profile(profile)
                .photoUrl("/api/v1/profiles/photos/download/" + fileName)
                .main(isMain || profile.getPhotos().isEmpty()) // first photo defaults to main
                .build();

        Photo saved = photoRepository.save(photo);
        return photoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("Unauthorized to delete this photo");
        }

        // Delete from local disk
        String filename = photo.getPhotoUrl().substring(photo.getPhotoUrl().lastIndexOf("/") + 1);
        fileStorageService.deleteFile(filename);

        profile.getPhotos().remove(photo);
        photoRepository.delete(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDto> getProfileVisitors(Long userId) {
        // Return mock visitors list: other registered users in the database
        return profileRepository.findAll().stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .limit(3)
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }
}
