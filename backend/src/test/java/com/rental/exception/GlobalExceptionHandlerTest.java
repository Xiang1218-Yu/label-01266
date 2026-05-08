package com.rental.exception;

import com.rental.common.BusinessException;
import com.rental.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理业务异常")
    void handleBusinessException() {
        BusinessException ex = new BusinessException(400, "参数不合法");

        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals(400, result.getCode());
        assertEquals("参数不合法", result.getMessage());
    }

    @Test
    @DisplayName("处理业务异常 - 默认code")
    void handleBusinessException_defaultCode() {
        BusinessException ex = new BusinessException("操作失败");

        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
    }

    @Test
    @DisplayName("处理参数绑定异常")
    void handleBindException() {
        BindException ex = new BindException(new Object(), "object");
        ex.addError(new FieldError("object", "field", "格式错误"));

        Result<Void> result = handler.handleBindException(ex);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("格式错误"));
    }

    @Test
    @DisplayName("处理认证异常")
    void handleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("未登录") {};

        Result<Void> result = handler.handleAuthenticationException(ex);

        assertEquals(401, result.getCode());
        assertEquals("认证失败，请重新登录", result.getMessage());
    }

    @Test
    @DisplayName("处理MethodArgumentNotValidException")
    void handleValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Arrays.asList(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        Result<Void> result = handler.handleValidException(ex);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("处理授权异常")
    void handleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("无权限");

        Result<Void> result = handler.handleAccessDeniedException(ex);

        assertEquals(403, result.getCode());
        assertEquals("权限不足", result.getMessage());
    }

    @Test
    @DisplayName("处理约束违反异常")
    void handleConstraintViolationException() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("不能为空");
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        Result<Void> result = handler.handleConstraintViolationException(ex);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("处理其他异常")
    void handleException() {
        Exception ex = new RuntimeException("unknown error");

        Result<Void> result = handler.handleException(ex);

        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }
}
