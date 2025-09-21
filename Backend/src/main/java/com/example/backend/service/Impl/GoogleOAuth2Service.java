package com.example.backend.service.Impl;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        log.info("Google OAuth2 user attempting to login with email: {}", email);

        // Save user if not exists
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("New Google user detected. Creating account for: {}", email);
                    return saveNewGoogleUser(name, email);
                });

        log.info("Google OAuth2 user authenticated successfully: {} with role: {}", user.getEmail(), user.getRole());

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }

    @Transactional
    public User saveNewGoogleUser(String name, String email) {
        log.info("Saving new Google user: {} with email: {}", name, email);

        User newUser = User.builder()
                .username(name)
                .email(email)
                .password(passwordEncoder.encode("oauth2user")) // default password for Google users
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Google user account created successfully with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }
}
