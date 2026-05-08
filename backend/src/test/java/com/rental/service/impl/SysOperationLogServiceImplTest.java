package com.rental.service.impl;

import com.rental.entity.SysOperationLog;
import com.rental.mapper.SysOperationLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SysOperationLogServiceImpl测试")
@ExtendWith(MockitoExtension.class)
class SysOperationLogServiceImplTest {

    @Mock
    private SysOperationLogMapper sysOperationLogMapper;

    @InjectMocks
    private SysOperationLogServiceImpl operationLogService;

    private SysOperationLog testLog;

    @BeforeEach
    void setUp() {
        testLog = new SysOperationLog();
        testLog.setId(1L);
        testLog.setModule("房屋管理");
        testLog.setOperationType("保存");
        testLog.setDescription("测试操作");
        testLog.setUserId(1L);
        testLog.setUsername("admin");
        testLog.setStatus(1);
        testLog.setOperationTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试操作日志实体")
    void testOperationLogEntity() {
        SysOperationLog log = new SysOperationLog();
        log.setId(2L);
        log.setModule("用户管理");
        log.setOperationType("登录");
        log.setDescription("用户登录操作");
        log.setUserId(100L);
        log.setUsername("testuser");
        log.setIpAddress("127.0.0.1");
        log.setRequestUrl("/api/auth/login");
        log.setRequestMethod("POST");
        log.setRequestParams("{\"username\":\"test\"}");
        log.setResponseResult("{\"code\":200}");
        log.setStatus(1);
        log.setErrorMsg(null);
        log.setOperationTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        assertEquals(2L, log.getId());
        assertEquals("用户管理", log.getModule());
        assertEquals("登录", log.getOperationType());
        assertEquals("用户登录操作", log.getDescription());
        assertEquals(100L, log.getUserId());
        assertEquals("testuser", log.getUsername());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals("/api/auth/login", log.getRequestUrl());
        assertEquals("POST", log.getRequestMethod());
        assertEquals("{\"username\":\"test\"}", log.getRequestParams());
        assertEquals("{\"code\":200}", log.getResponseResult());
        assertEquals(1, log.getStatus());
        assertNull(log.getErrorMsg());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), log.getOperationTime());
    }

    @Test
    @DisplayName("测试saveLog方法存在")
    void testSaveLogMethodExists() {
        assertDoesNotThrow(() -> {
            operationLogService.saveLog(testLog);
        });
    }
}
