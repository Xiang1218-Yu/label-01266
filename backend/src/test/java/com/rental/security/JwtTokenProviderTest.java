package com.rental.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "rental-housing-secret-key-2026-very-long-secret-key-for-jwt-token");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 86400000L);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("生成Token")
    void generateToken() {
        String token = jwtTokenProvider.generateToken(1L, "admin");

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("从Token获取用户ID")
    void getUserIdFromToken() {
        String token = jwtTokenProvider.generateToken(1L, "admin");

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("从Token获取用户名")
    void getUsernameFromToken() {
        String token = jwtTokenProvider.generateToken(1L, "admin");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("验证Token - 有效Token")
    void validateToken_valid() {
        String token = jwtTokenProvider.generateToken(1L, "admin");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void validateToken_invalid() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    @DisplayName("验证Token - 空Token")
    void validateToken_empty() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("从无效Token获取用户ID返回null")
    void getUserIdFromToken_invalid() {
        Long userId = jwtTokenProvider.getUserIdFromToken("invalid-token");

        assertNull(userId);
    }

    @Test
    @DisplayName("从无效Token获取用户名返回null")
    void getUsernameFromToken_invalid() {
        String username = jwtTokenProvider.getUsernameFromToken("invalid-token");

        assertNull(username);
    }

    @Test
    @DisplayName("获取过期时间")
    void getExpiration() {
        assertEquals(86400000L, jwtTokenProvider.getExpiration());
    }
}
