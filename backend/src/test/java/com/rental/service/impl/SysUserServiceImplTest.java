package com.rental.service.impl;

import com.rental.common.BusinessException;
import com.rental.common.Constants;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.entity.SysUser;
import com.rental.mapper.SysUserMapper;
import com.rental.security.JwtTokenProvider;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private LoginDTO loginDTO;
    private SysUser sysUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("admin");
        sysUser.setPassword("encoded_password");
        sysUser.setRealName("系统管理员");
        sysUser.setStatus(1);
    }

    @Test
    @DisplayName("登录 - 用户不存在抛出异常")
    void login_userNotFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> sysUserService.login(loginDTO));
    }

    @Test
    @DisplayName("登录 - 密码错误抛出异常")
    void login_wrongPassword() {
        when(sysUserMapper.selectOne(any())).thenReturn(sysUser);
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(false);

        assertThrows(BusinessException.class, () -> sysUserService.login(loginDTO));
    }

    @Test
    @DisplayName("登录 - 账号被禁用抛出异常")
    void login_userDisabled() {
        sysUser.setStatus(0);
        when(sysUserMapper.selectOne(any())).thenReturn(sysUser);
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(true);

        assertThrows(BusinessException.class, () -> sysUserService.login(loginDTO));
    }

    @Test
    @DisplayName("登录 - 成功登录")
    void login_success() {
        when(sysUserMapper.selectOne(any())).thenReturn(sysUser);
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "admin")).thenReturn("test-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResultDTO result = sysUserService.login(loginDTO);

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
        assertEquals("系统管理员", result.getRealName());
        verify(valueOperations).set(eq(Constants.CACHE_TOKEN_PREFIX + "1"), eq("test-token"), eq(86400000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void getByUsername() {
        when(sysUserMapper.selectOne(any())).thenReturn(sysUser);

        SysUser result = sysUserService.getByUsername("admin");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("根据用户名查询用户 - 用户不存在")
    void getByUsername_notFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        SysUser result = sysUserService.getByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("退出登录 - 带Bearer前缀的token")
    void logout_withBearerPrefix() {
        when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);

        sysUserService.logout("Bearer test-token");

        verify(redisTemplate).delete(Constants.CACHE_TOKEN_PREFIX + "1");
    }

    @Test
    @DisplayName("退出登录 - 不带Bearer前缀的token")
    void logout_withoutBearerPrefix() {
        when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);

        sysUserService.logout("test-token");

        verify(redisTemplate).delete(Constants.CACHE_TOKEN_PREFIX + "1");
    }

    @Test
    @DisplayName("退出登录 - token为null")
    void logout_nullToken() {
        assertDoesNotThrow(() -> sysUserService.logout(null));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("退出登录 - userId为null时不删除缓存")
    void logout_nullUserId() {
        when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(null);

        sysUserService.logout("test-token");

        verify(redisTemplate, never()).delete(anyString());
    }
}
