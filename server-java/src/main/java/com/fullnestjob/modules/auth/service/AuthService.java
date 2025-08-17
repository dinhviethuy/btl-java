package com.fullnestjob.modules.auth.service;

import com.fullnestjob.modules.auth.dto.AuthDtos.LoginBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.LoginResponseDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.UserNestedDto;
import com.fullnestjob.modules.auth.dto.AuthDtos.UpdateProfileDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ChangePasswordDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ForgotSendOtpDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ForgotResetDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterSendOtpDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterVerifyDTO;
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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fullnestjob.modules.auth.entity.RegistrationOtp;
import com.fullnestjob.modules.auth.repo.RegistrationOtpRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final RegistrationOtpRepository registrationOtpRepository;
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String fromEmail;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JavaMailSender mailSender, RegistrationOtpRepository registrationOtpRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.registrationOtpRepository = registrationOtpRepository;
    }

    public JwtService getJwtService() { return jwtService; }

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

    @Transactional
    public LoginResponseDTO login(LoginBodyDTO body) {
        User u = userRepository.findByEmail(body.username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(body.password, u.getPassword())) {
            // Nếu dữ liệu gửi từ form (body) nhưng sai, trả 422 thay vì 401
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Invalid credentials");
        }
        Map<String, Object> baseClaims = new HashMap<>();
        baseClaims.put("_id", u.get_id());
        baseClaims.put("email", u.getEmail());
        baseClaims.put("name", u.getName());
        baseClaims.put("role", u.getRole() != null ? u.getRole().getName() : null);
        Map<String, Object> accessClaims = new HashMap<>(baseClaims);
        accessClaims.put("sub", "access");
        Map<String, Object> refreshClaims = new HashMap<>(baseClaims);
        refreshClaims.put("sub", "refresh");
        String access = jwtService.generateAccessToken(accessClaims);
        String refresh = jwtService.generateRefreshToken(refreshClaims);
        // save refresh token to DB
        u.setRefreshToken(refresh);
        userRepository.save(u);

        LoginResponseDTO dto = new LoginResponseDTO();
        dto.access_token = access;
        dto.refresh_token = refresh;
        dto.user = toUserNested(u);
        return dto;
    }

    @Transactional
    public LoginResponseDTO refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        String token = authHeader.substring(7);
        if (token == null || token.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        // Parse claims to get user id
        String userId;
        try {
            io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(jwtService.getSigningKeyForVerify())
                    .build().parseSignedClaims(token);
            userId = claims.getPayload().get("_id", String.class);
            String sub = claims.getPayload().getSubject();
            // Backward-compatible: accept legacy tokens with sub "token login"
            if (sub == null || !(sub.equals("refresh") || sub.equals("token login"))) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            // If refresh token is expired, cannot refresh
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        User u = userRepository.findById(userId).orElseThrow();
        // Validate token matches stored refresh token (allow first-time migration when null)
        if (u.getRefreshToken() == null) {
            u.setRefreshToken(token);
            userRepository.save(u);
        } else if (!u.getRefreshToken().equals(token)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Map<String, Object> baseClaims = new HashMap<>();
        baseClaims.put("_id", u.get_id());
        baseClaims.put("email", u.getEmail());
        baseClaims.put("name", u.getName());
        baseClaims.put("role", u.getRole() != null ? u.getRole().getName() : null);
        Map<String, Object> accessClaims = new HashMap<>(baseClaims);
        accessClaims.put("sub", "access");
        Map<String, Object> refreshClaims = new HashMap<>(baseClaims);
        refreshClaims.put("sub", "refresh");

        String newAccess = jwtService.generateAccessToken(accessClaims);
        String newRefresh = jwtService.generateRefreshToken(refreshClaims);
        u.setRefreshToken(newRefresh);
        userRepository.saveAndFlush(u);

        LoginResponseDTO dto = new LoginResponseDTO();
        dto.access_token = newAccess;
        dto.refresh_token = newRefresh;
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
        dto.age = u.getAge();
        dto.address = u.getAddress();
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

    @Transactional
    public void sendForgotOtp(ForgotSendOtpDTO body) {
        if (body == null || body.email == null || body.email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Email is required");
        }
        User u = userRepository.findByEmail(body.email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Email not found"));

        String otp = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MINUTE, 10);
        u.setOtpCode(otp);
        u.setOtpExpiredAt(cal.getTime());
        userRepository.save(u);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(u.getEmail());
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\nIt will expire in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            // If mail fails, still allow client to proceed, but log
            System.err.println("Failed to send OTP mail: " + e.getMessage());
        }
    }

    @Transactional
    public void resetForgotPassword(ForgotResetDTO body) {
        if (body == null || body.email == null || body.otp == null || body.newPassword == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        User u = userRepository.findByEmail(body.email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Email not found"));
        if (u.getOtpCode() == null || u.getOtpExpiredAt() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "OTP not requested");
        }
        java.util.Date now = new java.util.Date();
        if (!body.otp.equals(u.getOtpCode()) || now.after(u.getOtpExpiredAt())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        u.setPassword(passwordEncoder.encode(body.newPassword));
        u.setOtpCode(null);
        u.setOtpExpiredAt(null);
        userRepository.save(u);
    }

    @Transactional
    public void sendRegisterOtp(RegisterSendOtpDTO body) {
        if (body == null || body.email == null || body.password == null || body.confirmPassword == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        if (!body.password.equals(body.confirmPassword)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }
        if (userRepository.findByEmail(body.email).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Email already exists");
        }
        // Create/update registration OTP record
        RegistrationOtp otpRecord = registrationOtpRepository.findByEmail(body.email).orElse(new RegistrationOtp());
        otpRecord.setEmail(body.email);
        otpRecord.setName(body.name);
        otpRecord.setPasswordHash(passwordEncoder.encode(body.password));
        otpRecord.setAge(body.age);
        otpRecord.setGender(body.gender);
        otpRecord.setAddress(body.address);
        String otp = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MINUTE, 10);
        otpRecord.setOtpCode(otp);
        otpRecord.setOtpExpiredAt(cal.getTime());
        registrationOtpRepository.save(otpRecord);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(body.email);
            message.setSubject("Your Registration OTP Code");
            message.setText("Your OTP code is: " + otp + "\nIt will expire in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send registration OTP mail: " + e.getMessage());
        }
    }

    @Transactional
    public User verifyRegister(RegisterSendOtpDTO data, RegisterVerifyDTO verify) {
        if (!data.password.equals(data.confirmPassword)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }
        if (userRepository.findByEmail(data.email).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Email already exists");
        }
        // Find OTP record
        RegistrationOtp temp = registrationOtpRepository.findByEmail(data.email).orElse(null);
        if (temp == null || temp.getOtpCode() == null || temp.getOtpExpiredAt() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "OTP not requested");
        }
        java.util.Date now = new java.util.Date();
        if (!verify.otp.equals(temp.getOtpCode()) || now.after(temp.getOtpExpiredAt())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        // Create final user
        User u = new User();
        u.setName(temp.getName());
        u.setEmail(temp.getEmail());
        u.setPassword(temp.getPasswordHash());
        u.setAge(temp.getAge());
        u.setGender(temp.getGender());
        u.setAddress(temp.getAddress());
        User created = userRepository.save(u);
        // cleanup OTP record to prevent reuse
        try { registrationOtpRepository.deleteByEmail(temp.getEmail()); } catch (Exception ignored) {}
        return created;
    }

    @Transactional
    public UserNestedDto updateProfile(String userId, UpdateProfileDTO body) {
        User u = userRepository.findById(userId).orElseThrow();
        if (body.name != null && !body.name.isBlank()) u.setName(body.name);
        if (body.age != null) u.setAge(body.age);
        if (body.address != null) u.setAddress(body.address);
        User saved = userRepository.save(u);
        return toUserNested(saved);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordDTO body) {
        if (body == null || body.pass == null || body.newPass == null || body.confirmNewPass == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        if (!body.newPass.equals(body.confirmNewPass)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }
        User u = userRepository.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(body.pass, u.getPassword())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        u.setPassword(passwordEncoder.encode(body.newPass));
        userRepository.save(u);
    }

    @Transactional
    public void logout(String userId) {
        if (userId == null || userId.isBlank()) return;
        User u = userRepository.findById(userId).orElse(null);
        if (u != null) {
            u.setRefreshToken(null);
            userRepository.save(u);
        }
    }
}


