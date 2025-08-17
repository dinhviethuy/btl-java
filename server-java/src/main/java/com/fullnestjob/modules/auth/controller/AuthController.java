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
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterSendOtpDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterVerifyDTO;
import com.fullnestjob.modules.auth.dto.AuthDtos.RegisterVerifyAllDTO;
import com.fullnestjob.modules.auth.service.AuthService;
import com.fullnestjob.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.refresh.name:refresh_token}")
    private String refreshCookieName;
    @Value("${app.cookie.refresh.path:/}")
    private String refreshCookiePath;
    @Value("${app.cookie.refresh.same-site:Lax}")
    private String refreshCookieSameSite;
    @Value("${app.cookie.refresh.secure:false}")
    private boolean refreshCookieSecure;
    @Value("${app.cookie.refresh.domain:}")
    private String refreshCookieDomain;

    private void addSetCookie(jakarta.servlet.http.HttpServletResponse resp, String value, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(refreshCookieName).append("=").append(value != null ? value : "");
        sb.append("; Path=").append(refreshCookiePath != null && !refreshCookiePath.isBlank() ? refreshCookiePath : "/");
        sb.append("; HttpOnly");
        if (refreshCookieSecure) sb.append("; Secure");
        String sameSite = (refreshCookieSameSite == null || refreshCookieSameSite.isBlank()) ? "Lax" : refreshCookieSameSite;
        sb.append("; SameSite=").append(sameSite);
        sb.append("; Max-Age=").append(maxAge);
        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            sb.append("; Domain=").append(refreshCookieDomain);
        }
        resp.addHeader("Set-Cookie", sb.toString());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Message("Login successfully")
    public ResponseEntity<LoginResponseDTO> login(@Validated @RequestBody LoginBodyDTO body, jakarta.servlet.http.HttpServletResponse resp) {
        LoginResponseDTO dto = authService.login(body);
        // set refresh token as HttpOnly cookie
        if (dto.refresh_token != null) {
            addSetCookie(resp, "", 0);
            addSetCookie(resp, dto.refresh_token, authService.getJwtService().getRefreshTokenMaxAgeSeconds());
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register")
    @Message("Register successfully")
    public ResponseEntity<Object> register(@Validated @RequestBody RegisterBodyDTO body) {
        return ResponseEntity.ok(authService.register(body));
    }

    @PostMapping("/register/send-otp")
    @Message("Send register OTP successfully")
    public ResponseEntity<java.util.Map<String, Object>> registerSendOtp(@Validated @RequestBody RegisterSendOtpDTO body) {
        authService.sendRegisterOtp(body);
        return ResponseEntity.ok(new java.util.HashMap<>());
    }

    @PostMapping("/register/verify")
    @Message("Register verify successfully")
    public ResponseEntity<Object> registerVerify(@Validated @RequestBody RegisterVerifyAllDTO body) {
        RegisterSendOtpDTO data = new RegisterSendOtpDTO();
        data.name = body.name;
        data.email = body.email;
        data.password = body.password;
        data.confirmPassword = body.confirmPassword;
        data.age = body.age;
        data.gender = body.gender;
        data.address = body.address;
        RegisterVerifyDTO verify = new RegisterVerifyDTO();
        verify.email = body.email;
        verify.otp = body.otp;
        return ResponseEntity.ok(authService.verifyRegister(data, verify));
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
    public ResponseEntity<LoginResponseDTO> refresh(@RequestHeader(name = "Authorization", required = false) String authHeader,
                                                    @org.springframework.web.bind.annotation.CookieValue(value = "refresh_token", required = false) String refreshCookie,
                                                    jakarta.servlet.http.HttpServletResponse resp) {
        // Prefer cookie; fallback to Authorization header (for backward compat)
        LoginResponseDTO dto;
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            dto = authService.refresh("Bearer " + refreshCookie);
        } else {
            dto = authService.refresh(authHeader);
        }
        if (dto.refresh_token != null) {
            addSetCookie(resp, "", 0);
            addSetCookie(resp, dto.refresh_token, authService.getJwtService().getRefreshTokenMaxAgeSeconds());
            dto.refresh_token = null;
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    @Message("Logout successfully")
    public ResponseEntity<java.util.Map<String, Object>> logout(
            @org.springframework.web.bind.annotation.CookieValue(value = "refresh_token", required = false) String refreshCookie,
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            jakarta.servlet.http.HttpServletResponse resp) {
        String token = null;
        if (refreshCookie != null && !refreshCookie.isBlank()) token = refreshCookie;
        else if (authHeader != null && authHeader.startsWith("Bearer ")) token = authHeader.substring(7);
        if (token != null) {
            try {
                io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> claims = io.jsonwebtoken.Jwts.parser()
                        .verifyWith(authService.getJwtService().getSigningKeyForVerify())
                        .build().parseSignedClaims(token);
                String userId = claims.getPayload().get("_id", String.class);
                if (userId != null) authService.logout(userId);
            } catch (Exception ignored) {}
        }
        // delete cookie
        addSetCookie(resp, "", 0);
        return ResponseEntity.ok(new java.util.HashMap<>());
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


