package com.fullnestjob.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal ap) {
            return ap.getUserId();
        }
        return principal != null ? principal.toString() : null;
    }

    public static String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal ap) {
            return ap.getEmail();
        }
        return null;
    }

    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal ap) {
            return ap.getRole();
        }
        return null;
    }

    public static boolean isAdmin() {
        String role = getCurrentRole();
        if (role == null) return false;
        String r = role.toUpperCase();
        return "ADMIN".equals(r) || "SUPER_ADMIN".equals(r);
    }
}


