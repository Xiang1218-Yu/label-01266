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

    @Test
    @DisplayName("测试SysUser状态边界值")
    void testSysUserStatusBoundaryValues() {
        SysUser userEnabled = new SysUser();
        userEnabled.setStatus(1);
        assertEquals(1, userEnabled.getStatus());

        SysUser userDisabled = new SysUser();
        userDisabled.setStatus(0);
        assertEquals(0, userDisabled.getStatus());

        SysUser userNegativeStatus = new SysUser();
        userNegativeStatus.setStatus(-1);
        assertEquals(-1, userNegativeStatus.getStatus());
    }

    @Test
    @DisplayName("测试SysUser超长用户名")
    void testSysUserLongUsername() {
        SysUser user = new SysUser();
        String longUsername = "a".repeat(200);
        user.setUsername(longUsername);
        user.setPassword("password");
        user.setRealName("测试用户");
        user.setStatus(1);

        assertEquals(longUsername, user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("测试用户", user.getRealName());
        assertEquals(1, user.getStatus());
    }

    @Test
    @DisplayName("测试SysUser空字符串")
    void testSysUserEmptyStrings() {
        SysUser user = new SysUser();
        user.setUsername("");
        user.setPassword("");
        user.setRealName("");

        assertEquals("", user.getUsername());
        assertEquals("", user.getPassword());
        assertEquals("", user.getRealName());
    }

    @Test
    @DisplayName("测试SysUser null值")
    void testSysUserNullValues() {
        SysUser user = new SysUser();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getRealName());
        assertNull(user.getStatus());
    }

    @Test
    @DisplayName("测试SysUser包含特殊字符的用户名")
    void testSysUserSpecialCharacters() {
        SysUser user = new SysUser();
        user.setUsername("admin@test!#$%^&*()");
        user.setRealName("测试用户-管理员");

        assertEquals("admin@test!#$%^&*()", user.getUsername());
        assertEquals("测试用户-管理员", user.getRealName());
    }

    @Test
    @DisplayName("测试LoginDTO空字符串")
    void testLoginDTOEmptyStrings() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");
        dto.setPassword("");

        assertEquals("", dto.getUsername());
        assertEquals("", dto.getPassword());
    }

    @Test
    @DisplayName("测试LoginDTOnull值")
    void testLoginDTONullValues() {
        LoginDTO dto = new LoginDTO();

        assertNull(dto.getUsername());
        assertNull(dto.getPassword());
    }

    @Test
    @DisplayName("测试LoginResultDTO空字符串")
    void testLoginResultDTOEmptyStrings() {
        LoginResultDTO dto = new LoginResultDTO();
        dto.setToken("");
        dto.setUsername("");
        dto.setRealName("");

        assertEquals("", dto.getToken());
        assertEquals("", dto.getUsername());
        assertEquals("", dto.getRealName());
    }

    @Test
    @DisplayName("测试LoginResultDTOnull值")
    void testLoginResultDTONullValues() {
        LoginResultDTO dto = new LoginResultDTO();

        assertNull(dto.getToken());
        assertNull(dto.getUserId());
        assertNull(dto.getUsername());
        assertNull(dto.getRealName());
        assertNull(dto.getExpireTime());
    }
}
