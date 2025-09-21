package com.example.backend.service.Impl;

import com.example.backend.dto.AuthDTO;
import com.example.backend.dto.AuthResponseDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    // Register user - always default role USER
    public AuthResponseDTO register(UserDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            return AuthResponseDTO.builder()
                    .message("User already exists with this email!")
                    .build();
        }

        User user = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Role.USER) // 🚨 always USER
                .build();

        userRepository.save(user);

        return AuthResponseDTO.builder()
                .message("User registered successfully!")
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    // Authenticate user
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
            );

            User user = userRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal(),
                    user.getId(),
                    List.of(user.getRole().name()));

            return AuthResponseDTO.builder()
                    .token(token)
                    .message("Authentication successful")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            return AuthResponseDTO.builder()
                    .message("Invalid email or password")
                    .build();
        }
    }
}
