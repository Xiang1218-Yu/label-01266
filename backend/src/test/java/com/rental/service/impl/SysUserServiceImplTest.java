package com.rental.service.impl;

import com.rental.common.BusinessException;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.entity.SysUser;
import com.rental.mapper.SysUserMapper;
import com.rental.security.JwtTokenProvider;
import com.rental.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SysUserServiceImpl测试")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser testUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("encoded-password");
        testUser.setRealName("管理员");
        testUser.setStatus(1);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");
    }

    @Test
    @DisplayName("测试退出登录 - 带Bearer前缀")
    void testLogoutWithBearer() {
        String tokenWithBearer = "Bearer mock-jwt-token";
        String token = "mock-jwt-token";

        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(1L);

        sysUserService.logout(tokenWithBearer);

        verify(redisTemplate).delete("rental:token:1");
    }

    @Test
    @DisplayName("测试退出登录 - 不带Bearer前缀")
    void testLogoutWithoutBearer() {
        String token = "mock-jwt-token";

        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(1L);

        sysUserService.logout(token);

        verify(redisTemplate).delete("rental:token:1");
    }

    @Test
    @DisplayName("测试退出登录 - null token")
    void testLogoutWithNullToken() {
        sysUserService.logout(null);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试退出登录 - 无效token返回null userId")
    void testLogoutWithInvalidToken() {
        String token = "invalid-token";

        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(null);

        sysUserService.logout(token);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试LoginDTO实体")
    void testLoginDTO() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("testpass");

        assertEquals("testuser", dto.getUsername());
        assertEquals("testpass", dto.getPassword());
    }

    @Test
    @DisplayName("测试LoginResultDTO实体")
    void testLoginResultDTO() {
        LoginResultDTO dto = new LoginResultDTO();
        dto.setToken("token123");
        dto.setUserId(100L);
        dto.setUsername("admin");
        dto.setRealName("管理员");
        dto.setExpireTime(1234567890L);

        assertEquals("token123", dto.getToken());
        assertEquals(100L, dto.getUserId());
        assertEquals("admin", dto.getUsername());
        assertEquals("管理员", dto.getRealName());
        assertEquals(1234567890L, dto.getExpireTime());
    }

    @Test
    @DisplayName("测试SysUser实体")
    void testSysUserEntity() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("pass");
        user.setRealName("测试用户");
        user.setStatus(1);

        assertEquals(1L, user.getId());
        assertEquals("test", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertEquals("测试用户", user.getRealName());
        assertEquals(1, user.getStatus());
    }
}
