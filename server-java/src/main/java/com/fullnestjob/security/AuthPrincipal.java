package com.fullnestjob.security;

public class AuthPrincipal {
    private final String userId;
    private final String email;
    private final String name;
    private final String role;

    public AuthPrincipal(String userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return userId;
    }
}


