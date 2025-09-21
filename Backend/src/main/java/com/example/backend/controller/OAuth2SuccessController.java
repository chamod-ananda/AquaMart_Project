package com.example.backend.controller;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OAuth2SuccessController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @GetMapping("/oauth2/success")
    public RedirectView oauth2LoginSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email not provided by OAuth2 provider");
        }

        // Ensure user exists (idempotent upsert in case OAuth2UserService wasn’t invoked)
        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(
                        User.builder()
                                .username(name != null && !name.isBlank() ? name : email)
                                .email(email)
                                .password(passwordEncoder.encode("oauth2user"))
                                .role(Role.USER)
                                .build()
                )
        );

        // Build Spring Security UserDetails (not your entity)
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails, user.getId(), List.of(user.getRole().name()));

        // Redirect to frontend (served by Live Server) with safe encoded params
        String frontendBase = "http://127.0.0.1:5500/AquaMart_FrontEnd/pages/oauth-callback.html";
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase)
                .queryParam("token", token)
                .queryParam("userId", user.getId())
                .queryParam("email", user.getEmail())
                .queryParam("username", user.getUsername())
                .queryParam("role", user.getRole().name())
                .build()
                .toUriString();

        return new RedirectView(redirectUrl, true);
    }
}
