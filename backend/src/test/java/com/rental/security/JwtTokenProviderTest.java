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

    @Test
    @DisplayName("测试null参数 - getUserIdFromToken(null)")
    void testGetUserIdFromTokenNull() {
        Long userId = jwtTokenProvider.getUserIdFromToken(null);
        assertNull(userId);
    }

    @Test
    @DisplayName("测试空字符串参数 - getUserIdFromToken(\"\")")
    void testGetUserIdFromTokenEmptyString() {
        Long userId = jwtTokenProvider.getUserIdFromToken("");
        assertNull(userId);
    }

    @Test
    @DisplayName("测试null参数 - getUsernameFromToken(null)")
    void testGetUsernameFromTokenNull() {
        String username = jwtTokenProvider.getUsernameFromToken(null);
        assertNull(username);
    }

    @Test
    @DisplayName("测试空字符串参数 - getUsernameFromToken(\"\")")
    void testGetUsernameFromTokenEmptyString() {
        String username = jwtTokenProvider.getUsernameFromToken("");
        assertNull(username);
    }

    @Test
    @DisplayName("测试null参数 - validateToken(null)")
    void testValidateTokenNull() {
        boolean valid = jwtTokenProvider.validateToken(null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("测试空字符串参数 - validateToken(\"\")")
    void testValidateTokenEmptyString() {
        boolean valid = jwtTokenProvider.validateToken("");
        assertFalse(valid);
    }

    @Test
    @DisplayName("测试超长用户名生成Token")
    void testGenerateTokenWithLongUsername() {
        String longUsername = "a".repeat(1000);
        String token = jwtTokenProvider.generateToken(1L, longUsername);

        assertNotNull(token);
        assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(longUsername, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("测试空用户名生成Token")
    void testGenerateTokenWithEmptyUsername() {
        String token = jwtTokenProvider.generateToken(1L, "");

        assertNotNull(token);
        assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("测试包含特殊字符的用户名")
    void testGenerateTokenWithSpecialCharsUsername() {
        String specialUsername = "admin@test!#$%^&*()_+-=[]{}|;':\",./<>?中文测试";
        String token = jwtTokenProvider.generateToken(999L, specialUsername);

        assertNotNull(token);
        assertEquals(999L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(specialUsername, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("测试超大用户ID")
    void testGenerateTokenWithLargeUserId() {
        Long largeId = 999999999999999L;
        String token = jwtTokenProvider.generateToken(largeId, "largeuser");

        assertNotNull(token);
        assertEquals(largeId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("测试Token格式错误 - 只有一个点")
    void testValidateMalformedTokenOneDot() {
        String malformedToken = "header.payload";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    @DisplayName("测试Token格式错误 - 没有点")
    void testValidateMalformedTokenNoDot() {
        String malformedToken = "justrandomstring";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    @DisplayName("测试Token格式错误 - 大量空白字符")
    void testValidateTokenWithWhitespace() {
        assertFalse(jwtTokenProvider.validateToken("   "));
        assertFalse(jwtTokenProvider.validateToken("\n\t\r"));
    }
}
