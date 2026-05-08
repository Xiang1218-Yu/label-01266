package com.rental.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.annotation.OperationLog;
import com.rental.entity.SysOperationLog;
import com.rental.security.LoginUser;
import com.rental.service.SysOperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class OperationLogAspectTest {

    @Mock
    private SysOperationLogService operationLogService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private OperationLogAspect operationLogAspect;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser(1L, "admin");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setRequestURI("/room/save");
        request.setMethod("POST");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("操作日志切面 - 成功执行")
    void around_success() throws Throwable {
        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenReturn("result");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        Object result = operationLogAspect.around(joinPoint, operationLog);

        assertEquals("result", result);
        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).saveLog(captor.capture());
        assertEquals(1, captor.getValue().getStatus());
        assertEquals("127.0.0.1", captor.getValue().getIpAddress());
        assertEquals("/room/save", captor.getValue().getRequestUrl());
        assertEquals("POST", captor.getValue().getRequestMethod());
    }

    @Test
    @DisplayName("操作日志切面 - 成功执行并有请求参数和响应结果")
    void around_successWithParamsAndResult() throws Throwable {
        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenReturn("successResult");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"param1", "param2"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");

        Object result = operationLogAspect.around(joinPoint, operationLog);

        assertEquals("successResult", result);
        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).saveLog(captor.capture());
        assertNotNull(captor.getValue().getRequestParams());
        assertNotNull(captor.getValue().getResponseResult());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("操作日志切面 - 执行异常")
    void around_exception() throws Throwable {
        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenThrow(new RuntimeException("业务异常"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());

        assertThrows(RuntimeException.class, () -> operationLogAspect.around(joinPoint, operationLog));

        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).saveLog(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        assertTrue(captor.getValue().getErrorMsg().contains("业务异常"));
    }

    @Test
    @DisplayName("操作日志切面 - 无请求上下文")
    void around_noRequestContext() throws Throwable {
        RequestContextHolder.resetRequestAttributes();

        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenReturn("result");
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());

        Object result = operationLogAspect.around(joinPoint, operationLog);

        assertEquals("result", result);
        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).saveLog(captor.capture());
        assertNull(captor.getValue().getIpAddress());
        assertNull(captor.getValue().getRequestUrl());
    }

    @Test
    @DisplayName("操作日志切面 - 序列化请求参数失败")
    void around_serializeParamsFail() throws Throwable {
        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenReturn("result");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("序列化失败"))
                .thenReturn("{}");

        Object result = operationLogAspect.around(joinPoint, operationLog);

        assertEquals("result", result);
        verify(operationLogService).saveLog(any(SysOperationLog.class));
    }

    @Test
    @DisplayName("操作日志切面 - 序列化响应结果失败")
    void around_serializeResultFail() throws Throwable {
        OperationLog operationLog = createTestOperationLog();
        when(joinPoint.proceed()).thenReturn("result");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getMockMethod());
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}")
                .thenThrow(new RuntimeException("序列化失败"));

        Object result = operationLogAspect.around(joinPoint, operationLog);

        assertEquals("result", result);
        verify(operationLogService).saveLog(any(SysOperationLog.class));
    }

    private OperationLog createTestOperationLog() {
        return new OperationLog() {
            @Override
            public String module() {
                return "房屋管理";
            }

            @Override
            public String type() {
                return "保存";
            }

            @Override
            public String description() {
                return "保存房屋详情";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return OperationLog.class;
            }
        };
    }

    private Method getMockMethod() throws NoSuchMethodException {
        return this.getClass().getDeclaredMethod("around_success");
    }
}
