package com.rental.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取当前登录用户 - 已认证")
    void getCurrentUser_authenticated() {
        LoginUser loginUser = new LoginUser(1L, "admin");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("获取当前登录用户 - 未认证")
    void getCurrentUser_notAuthenticated() {
        SecurityContextHolder.clearContext();

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNull(result);
    }

    @Test
    @DisplayName("获取当前用户ID")
    void getCurrentUserId() {
        LoginUser loginUser = new LoginUser(1L, "admin");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long userId = SecurityUtils.getCurrentUserId();

        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("获取当前用户ID - 未认证返回null")
    void getCurrentUserId_notAuthenticated() {
        SecurityContextHolder.clearContext();

        Long userId = SecurityUtils.getCurrentUserId();

        assertNull(userId);
    }

    @Test
    @DisplayName("获取当前用户名")
    void getCurrentUsername() {
        LoginUser loginUser = new LoginUser(1L, "admin");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = SecurityUtils.getCurrentUsername();

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("获取当前用户名 - 未认证返回null")
    void getCurrentUsername_notAuthenticated() {
        SecurityContextHolder.clearContext();

        String username = SecurityUtils.getCurrentUsername();

        assertNull(username);
    }

    @Test
    @DisplayName("获取当前登录用户 - Principal不是LoginUser类型")
    void getCurrentUser_wrongPrincipalType() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("string-principal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNull(result);
    }
}
