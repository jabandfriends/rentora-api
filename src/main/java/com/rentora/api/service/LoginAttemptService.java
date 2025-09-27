package com.rentora.api.service;

import com.rentora.api.model.entity.User;
import com.rentora.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {


    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedAttempt(User user, int maxAttempts, int lockMinutes) {
        int attempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        }

        userRepository.save(user);
        System.out.println("Failed login attempt for user: " + user.getEmail() + ", attempts: " + user.getLoginAttempts());

    }
}