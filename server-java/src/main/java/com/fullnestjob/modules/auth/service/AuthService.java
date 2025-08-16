package com.fullnestjob.modules.auth.service;

import com.fullnestjob.modules.auth.dto.AuthDtos.LoginBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.LoginResponseDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.UserNestedDto;
import com.fullnestjob.modules.auth.dto.AuthDtos.UserNestedPermissionDto;
import com.fullnestjob.modules.auth.dto.AuthDtos.UserNestedRoleDto;
import com.fullnestjob.modules.permissions.entity.Permission;
import com.fullnestjob.modules.roles.entity.Role;
import com.fullnestjob.modules.users.entity.User;
import com.fullnestjob.modules.users.repo.UserRepository;
import com.fullnestjob.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterBodyDTO body) {
        if (userRepository.findByEmail(body.email).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Email already exists");
        }
        User u = new User();
        u.setName(body.name);
        u.setEmail(body.email);
        u.setPassword(passwordEncoder.encode(body.password));
        u.setAge(body.age);
        u.setGender(body.gender);
        u.setAddress(body.address);
        return userRepository.save(u);
    }

    public LoginResponseDTO login(LoginBodyDTO body) {
        User u = userRepository.findByEmail(body.username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(body.password, u.getPassword())) {
            // Nếu dữ liệu gửi từ form (body) nhưng sai, trả 422 thay vì 401
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Invalid credentials");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("_id", u.get_id());
        claims.put("email", u.getEmail());
        claims.put("name", u.getName());
        claims.put("role", u.getRole() != null ? u.getRole().getName() : null);
        claims.put("sub", "token login");
        String access = jwtService.generateAccessToken(claims);

        LoginResponseDTO dto = new LoginResponseDTO();
        dto.access_token = access;
        dto.user = toUserNested(u);
        return dto;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }
        String token = authHeader.substring(7);
        // For simplicity, reissue a new access token using the same user info resolved from the token claims
        // In a real-world scenario, you would verify a refresh token stored in DB and issue new access token
        io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> claims = io.jsonwebtoken.Jwts.parser()
                .verifyWith(jwtService.getSigningKeyForVerify())
                .build().parseSignedClaims(token);
        String userId = claims.getPayload().get("_id", String.class);
        User u = userRepository.findById(userId).orElseThrow();

        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("_id", u.get_id());
        newClaims.put("email", u.getEmail());
        newClaims.put("name", u.getName());
        newClaims.put("role", u.getRole() != null ? u.getRole().getName() : null);
        newClaims.put("sub", "token refresh");

        String newAccess = jwtService.generateAccessToken(newClaims);
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.access_token = newAccess;
        dto.user = toUserNested(u);
        return dto;
    }

    public UserNestedDto account(String userId) {
        User u = userRepository.findById(userId).orElseThrow();
        return toUserNested(u);
    }

    private UserNestedDto toUserNested(User u) {
        UserNestedDto dto = new UserNestedDto();
        dto._id = u.get_id();
        dto.email = u.getEmail();
        dto.name = u.getName();
        if (u.getRole() != null) {
            UserNestedRoleDto r = new UserNestedRoleDto();
            r._id = u.getRole().get_id();
            r.name = u.getRole().getName();
            dto.role = r;
            if (u.getRole().getPermissions() != null) {
                dto.permissions = u.getRole().getPermissions().stream().map(this::toPerm).collect(Collectors.toList());
            }
        }
        return dto;
    }

    private UserNestedPermissionDto toPerm(Permission p) {
        UserNestedPermissionDto d = new UserNestedPermissionDto();
        d._id = p.get_id();
        d.name = p.getName();
        d.apiPath = p.getApiPath();
        d.method = p.getMethod();
        d.module = p.getModule();
        return d;
    }
}


