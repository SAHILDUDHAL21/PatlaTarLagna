package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.*;
import com.patlatarlagna.entity.RefreshToken;
import com.patlatarlagna.entity.Role;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.enums.RoleType;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.repository.ProfileRepository;
import com.patlatarlagna.repository.RoleRepository;
import com.patlatarlagna.repository.UserRepository;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.security.jwt.JwtTokenProvider;
import com.patlatarlagna.service.AuthService;
import com.patlatarlagna.service.EmailService;
import com.patlatarlagna.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           RoleRepository roleRepository, ProfileRepository profileRepository,
                           RefreshTokenService refreshTokenService, JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder encoder, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is not activated. Please verify your email first.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateJwtToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        boolean hasProfile = profileRepository.findByUserId(userDetails.getId()).isPresent();

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .email(userDetails.getUsername())
                .roles(roles)
                .hasProfile(hasProfile)
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Initialize default role
        Role userRole = roleRepository.findByName(RoleType.NORMAL_USER)
                .orElseThrow(() -> new RuntimeException("Error: Default User Role not found."));

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(encoder.encode(registerRequest.getPassword()))
                .enabled(false) // disabled until verified
                .roles(Set.of(userRole))
                .verificationOtp(otp)
                .verificationOtpExpiry(expiry)
                .build();

        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new BadRequestException("Account is already verified and active");
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp)) {
            throw new BadRequestException("Invalid verification OTP");
        }

        if (user.getVerificationOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification OTP is expired. Please sign up again or request new OTP");
        }

        user.setEnabled(true);
        user.setVerificationOtp(null);
        user.setVerificationOtpExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        user.setResetOtp(otp);
        user.setResetOtpExpiry(expiry);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(resetPasswordRequest.getOtp())) {
            throw new BadRequestException("Invalid reset OTP");
        }

        if (user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset OTP is expired. Request a new one.");
        }

        user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!encoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public JwtResponse refreshJwtToken(TokenRefreshRequest refreshRequest) {
        String requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
                    List<String> roles = user.getRoles().stream()
                            .map(role -> "ROLE_" + role.getName().name())
                            .collect(Collectors.toList());
                    boolean hasProfile = profileRepository.findByUserId(user.getId()).isPresent();
                    
                    return JwtResponse.builder()
                            .token(token)
                            .refreshToken(requestRefreshToken)
                            .id(user.getId())
                            .email(user.getEmail())
                            .roles(roles)
                            .hasProfile(hasProfile)
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }
}
