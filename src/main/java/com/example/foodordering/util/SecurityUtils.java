package com.example.foodordering.util;

import com.example.foodordering.exception.UnauthorizedException;
import com.example.foodordering.model.Role;
import com.example.foodordering.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userDetails;
    }

    public static String getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }

    public static boolean isCurrentUserAdmin() {
        try {
            return getCurrentUserDetails().getRole() == Role.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }
}
