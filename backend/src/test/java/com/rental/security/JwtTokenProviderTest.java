package com.rental.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider测试")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "test-secret-key-test-secret-key-test-secret-key-test-secret-key";
    private final long expiration = 86400000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", expiration);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("测试生成Token")
    void testGenerateToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = jwtTokenProvider.generateToken(userId, username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("测试从Token获取用户ID")
    void testGetUserIdFromToken() {
        Long userId = 123L;
        String username = "testuser";

        String token = jwtTokenProvider.generateToken(userId, username);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("测试从Token获取用户名")
    void testGetUsernameFromToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = jwtTokenProvider.generateToken(userId, username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("测试验证有效Token")
    void testValidateValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "testuser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("测试验证无效Token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid-token-12345";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("测试验证过期Token")
    void testValidateExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject("1")
                .claim("username", "testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("测试获取过期时间配置")
    void testGetExpiration() {
        assertEquals(expiration, jwtTokenProvider.getExpiration());
    }

    @Test
    @DisplayName("测试从无效Token获取用户ID返回null")
    void testGetUserIdFromInvalidToken() {
        Long userId = jwtTokenProvider.getUserIdFromToken("invalid-token");
        assertNull(userId);
    }

    @Test
    @DisplayName("测试从无效Token获取用户名返回null")
    void testGetUsernameFromInvalidToken() {
        String username = jwtTokenProvider.getUsernameFromToken("invalid-token");
        assertNull(username);
    }

    @Test
    @DisplayName("测试生成的Token包含正确信息")
    void testGeneratedTokenContainsCorrectInfo() {
        Long userId = 999L;
        String username = "admin_user";

        String token = jwtTokenProvider.generateToken(userId, username);

        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("测试不同用户生成不同Token")
    void testDifferentUsersGenerateDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken(1L, "user1");
        String token2 = jwtTokenProvider.generateToken(2L, "user2");

        assertNotEquals(token1, token2);
    }
}
