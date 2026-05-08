package com.rental.controller;

import com.rental.common.Result;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthController测试")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private AuthController authController;

    private LoginDTO loginDTO;
    private LoginResultDTO loginResultDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");

        loginResultDTO = new LoginResultDTO();
        loginResultDTO.setToken("mock-jwt-token");
        loginResultDTO.setUserId(1L);
        loginResultDTO.setUsername("admin");
        loginResultDTO.setRealName("管理员");
        loginResultDTO.setExpireTime(System.currentTimeMillis() + 86400000);
    }

    @Test
    @DisplayName("测试登录接口")
    void testLogin() {
        when(sysUserService.login(loginDTO)).thenReturn(loginResultDTO);

        Result<LoginResultDTO> result = authController.login(loginDTO);

        assertEquals(200, result.getCode());
        assertEquals("登录成功", result.getMessage());
        assertEquals(loginResultDTO, result.getData());
        assertEquals("mock-jwt-token", result.getData().getToken());
        assertEquals("admin", result.getData().getUsername());
    }

    @Test
    @DisplayName("测试退出登录接口")
    void testLogout() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "Bearer mock-jwt-token";
        when(request.getHeader("Authorization")).thenReturn(token);

        Result<Void> result = authController.logout(request);

        assertEquals(200, result.getCode());
        assertEquals("退出成功", result.getMessage());
        assertNull(result.getData());
        verify(sysUserService).logout(token);
    }

    @Test
    @DisplayName("测试退出登录 - 无token")
    void testLogoutNoToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        Result<Void> result = authController.logout(request);

        assertEquals(200, result.getCode());
        verify(sysUserService).logout(null);
    }
}
