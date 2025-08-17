package com.fullnestjob.modules.auth.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.auth.dto.AuthDtos.LoginBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.LoginResponseDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterBodyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.UserNestedDto;
import com.fullnestjob.modules.auth.dto.AuthDtos.UpdateProfileDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ChangePasswordDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ForgotSendOtpDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.ForgotResetDTO;
import com.fullnestjob.modules.auth.service.AuthService;
import com.fullnestjob.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Message("Login successfully")
    public ResponseEntity<LoginResponseDTO> login(@Validated @RequestBody LoginBodyDTO body) {
        return ResponseEntity.ok(authService.login(body));
    }

    @PostMapping("/register")
    @Message("Register successfully")
    public ResponseEntity<Object> register(@Validated @RequestBody RegisterBodyDTO body) {
        return ResponseEntity.ok(authService.register(body));
    }

    @GetMapping("/account")
    @Message("Get account successfully")
    public ResponseEntity<java.util.Map<String, Object>> account() {
        String userId = SecurityUtils.getCurrentUserId();
        UserNestedDto user = authService.account(userId);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("user", user);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/refresh")
    @Message("Refresh token successfully")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(authService.refresh(authHeader));
    }

    @PostMapping("/logout")
    @Message("Logout successfully")
    public ResponseEntity<java.util.Map<String, Object>> logout() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        return ResponseEntity.ok(data);
    }

    // Forgot password: send OTP
    @PostMapping("/forgot/send-otp")
    @Message("Send OTP successfully")
    public ResponseEntity<java.util.Map<String, Object>> sendOtp(@Validated @RequestBody ForgotSendOtpDTO body) {
        authService.sendForgotOtp(body);
        return ResponseEntity.ok(new java.util.HashMap<>());
    }

    // Forgot password: verify + reset
    @PostMapping("/forgot/reset")
    @Message("Reset password successfully")
    public ResponseEntity<java.util.Map<String, Object>> resetForgot(@Validated @RequestBody ForgotResetDTO body) {
        authService.resetForgotPassword(body);
        return ResponseEntity.ok(new java.util.HashMap<>());
    }

    // Update profile (name, age, address)
    @PatchMapping("/profile")
    @Message("Update profile successfully")
    public ResponseEntity<UserNestedDto> updateProfile(@Validated @RequestBody UpdateProfileDTO body) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(authService.updateProfile(userId, body));
    }

    // Change password (pass, newPass, confirmNewPass)
    @PostMapping("/change-password")
    @Message("Change password successfully")
    public ResponseEntity<java.util.Map<String, Object>> changePassword(@Validated @RequestBody ChangePasswordDTO body) {
        String userId = SecurityUtils.getCurrentUserId();
        authService.changePassword(userId, body);
        return ResponseEntity.ok(new java.util.HashMap<>());
    }
}


