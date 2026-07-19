package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.PhotoDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileDto>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileDto profileDto = profileService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profileDto));
    }

    @PostMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileDto>> createProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileDto profileDto) {
        ProfileDto created = profileService.createProfile(userDetails.getId(), profileDto);
        return ResponseEntity.ok(ApiResponse.success("Profile created successfully", created));
    }

    @PutMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileDto>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileDto profileDto) {
        ProfileDto updated = profileService.updateProfile(userDetails.getId(), profileDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileDto>> getUserProfile(@PathVariable Long userId) {
        ProfileDto profileDto = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profileDto));
    }

    @PostMapping(value = "/photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PhotoDto>> uploadPhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain) {
        PhotoDto photoDto = profileService.uploadPhoto(userDetails.getId(), file, isMain);
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded successfully", photoDto));
    }

    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deletePhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long photoId) {
        profileService.deletePhoto(userDetails.getId(), photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully"));
    }

    @GetMapping("/visitors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProfileDto>>> getProfileVisitors(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProfileDto> visitors = profileService.getProfileVisitors(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile visitors fetched successfully", visitors));
    }

    @GetMapping("/photos/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
