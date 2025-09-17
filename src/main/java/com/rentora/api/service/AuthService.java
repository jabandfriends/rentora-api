package com.rentora.api.service;

import com.rentora.api.model.dto.Authentication.*;
import com.rentora.api.model.entity.User;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.security.JwtService;
import com.rentora.api.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {


    private final AuthenticationManager authenticationManager;


    private final UserRepository userRepository;


    private final  PasswordEncoder passwordEncoder;

    private final LoginAttemptService loginAttemptService;
    private  final  JwtService jwtService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;


    public LoginResponse login(LoginRequest loginRequest) throws BadRequestException {
        User user = userRepository.findByEmailWithApartments(loginRequest.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BadRequestException("Account is temporarily locked due to too many failed login attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase().trim(),
                            loginRequest.getPassword()
                    )
            );

            // Reset login attempts on successful login
            if (user.getLoginAttempts() > 0) {
                user.setLoginAttempts(0);
                user.setLockedUntil(null);
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(authentication);

            UserInfo userInfo = createUserInfo(user);

            return new LoginResponse(jwt, jwtService.getExpirationTime(), userInfo);

        } catch (Exception e) {
            // Increment login attempts
            loginAttemptService.saveFailedAttempt(user, MAX_LOGIN_ATTEMPTS, LOCKOUT_DURATION_MINUTES);
            throw new BadRequestException("Invalid email or password");
        }
    }

    public UserInfo createUser(CreateUserRequest request) throws BadRequestException {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalId(request.getNationalId());
        user.setEmergencyContactName(request.getEmergencyContactName());
        user.setEmergencyContactPhone(request.getEmergencyContactPhone());

        // Parse birth date if provided
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty()) {
            try {
                user.setBirthDate(LocalDate.parse(request.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception e) {
                throw new BadRequestException("Invalid birth date format. Use YYYY-MM-DD");
            }
        }

        User savedUser = userRepository.save(user);
        log.info("New user created: {}", savedUser.getEmail());

        return createUserInfo(savedUser);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    public UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithApartments(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return createUserInfo(user);
    }

    private UserInfo createUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId().toString());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setProfileImageUrl(user.getProfileImageUrl());
        userInfo.setMustChangePassword(user.getMustChangePassword() != null && user.getMustChangePassword());
        userInfo.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null);

        // Map apartment roles
        List<UserInfo.ApartmentRole> apartmentRoles = user.getApartmentUsers() != null
                ? user.getApartmentUsers().stream()
                .filter(au -> au != null && Boolean.TRUE.equals(au.getIsActive()))
                .map(au -> {
                    UserInfo.ApartmentRole role = new UserInfo.ApartmentRole();
                    if (au.getApartment() != null) {
                        role.setApartmentId(au.getApartment().getId() != null ? au.getApartment().getId().toString() : null);
                        role.setApartmentName(au.getApartment().getName());
                    }
                    role.setRole(au.getRole() != null ? au.getRole().name() : null);
                    
                    return role;
                })
                .collect(Collectors.toList())
                : List.of(); // empty list if no apartmentUsers

        userInfo.setApartmentRoles(apartmentRoles);
        return userInfo;
    }

    private List<String> parsePermissions(String permissionsJson) {

        if (permissionsJson == null || permissionsJson.equals("[]")) {
            return List.of();
        }

        return List.of();
    }
}
