package com.rental.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SecurityUtils测试")
class SecurityUtilsTest {

    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        if (originalContext != null) {
            SecurityContextHolder.setContext(originalContext);
        } else {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("测试获取当前用户 - 已登录")
    void testGetCurrentUserAuthenticated() {
        LoginUser loginUser = new LoginUser(1L, "admin");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("测试获取当前用户 - 未登录")
    void testGetCurrentUserNotAuthenticated() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNull(result);
    }

    @Test
    @DisplayName("测试获取当前用户 - principal不是LoginUser")
    void testGetCurrentUserWrongPrincipalType() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("string-principal");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        LoginUser result = SecurityUtils.getCurrentUser();

        assertNull(result);
    }

    @Test
    @DisplayName("测试获取当前用户ID - 已登录")
    void testGetCurrentUserIdAuthenticated() {
        LoginUser loginUser = new LoginUser(100L, "testuser");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        Long userId = SecurityUtils.getCurrentUserId();

        assertEquals(100L, userId);
    }

    @Test
    @DisplayName("测试获取当前用户ID - 未登录")
    void testGetCurrentUserIdNotAuthenticated() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        Long userId = SecurityUtils.getCurrentUserId();

        assertNull(userId);
    }

    @Test
    @DisplayName("测试获取当前用户名 - 已登录")
    void testGetCurrentUsernameAuthenticated() {
        LoginUser loginUser = new LoginUser(1L, "administrator");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        String username = SecurityUtils.getCurrentUsername();

        assertEquals("administrator", username);
    }

    @Test
    @DisplayName("测试获取当前用户名 - 未登录")
    void testGetCurrentUsernameNotAuthenticated() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        String username = SecurityUtils.getCurrentUsername();

        assertNull(username);
    }

    @Test
    @DisplayName("测试LoginUser实体")
    void testLoginUserEntity() {
        LoginUser user = new LoginUser();
        user.setUserId(1L);
        user.setUsername("admin");

        assertEquals(1L, user.getUserId());
        assertEquals("admin", user.getUsername());
    }

    @Test
    @DisplayName("测试LoginUser全参构造器")
    void testLoginUserAllArgsConstructor() {
        LoginUser user = new LoginUser(2L, "testuser");

        assertEquals(2L, user.getUserId());
        assertEquals("testuser", user.getUsername());
    }
}
