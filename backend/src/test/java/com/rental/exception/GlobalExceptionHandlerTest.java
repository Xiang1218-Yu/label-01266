package com.rental.exception;

import com.rental.common.BusinessException;
import com.rental.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler全局异常处理测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("测试处理业务异常")
    void testHandleBusinessException() {
        String message = "业务异常测试消息";
        BusinessException exception = new BusinessException(400, message);

        Result<Void> result = exceptionHandler.handleBusinessException(exception);

        assertEquals(400, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试处理参数绑定异常")
    void testHandleBindException() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field", "绑定错误"));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        BindException exception = new BindException(bindingResult);

        Result<Void> result = exceptionHandler.handleBindException(exception);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("绑定错误"));
    }

    @Test
    @DisplayName("测试处理约束违反异常")
    void testHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("约束验证失败");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Result<Void> result = exceptionHandler.handleConstraintViolationException(exception);

        assertEquals(400, result.getCode());
        assertEquals("约束验证失败", result.getMessage());
    }

    @Test
    @DisplayName("测试处理认证异常")
    void testHandleAuthenticationException() {
        org.springframework.security.core.AuthenticationException exception = 
            new org.springframework.security.core.AuthenticationException("认证失败") {};

        Result<Void> result = exceptionHandler.handleAuthenticationException(exception);

        assertEquals(401, result.getCode());
        assertEquals("认证失败，请重新登录", result.getMessage());
    }

    @Test
    @DisplayName("测试处理授权异常")
    void testHandleAccessDeniedException() {
        org.springframework.security.access.AccessDeniedException exception = 
            new org.springframework.security.access.AccessDeniedException("权限不足");

        Result<Void> result = exceptionHandler.handleAccessDeniedException(exception);

        assertEquals(403, result.getCode());
        assertEquals("权限不足", result.getMessage());
    }

    @Test
    @DisplayName("测试处理其他异常")
    void testHandleException() {
        Exception exception = new RuntimeException("内部错误");

        Result<Void> result = exceptionHandler.handleException(exception);

        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("测试业务异常默认错误码")
    void testBusinessExceptionDefaultCode() {
        BusinessException exception = new BusinessException("默认错误码测试");

        Result<Void> result = exceptionHandler.handleBusinessException(exception);

        assertEquals(500, result.getCode());
        assertEquals("默认错误码测试", result.getMessage());
    }

    @Test
    @DisplayName("测试多个字段错误合并")
    void testMultipleFieldErrors() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "name", "姓名不能为空"));
        fieldErrors.add(new FieldError("object", "age", "年龄必须大于0"));
        fieldErrors.add(new FieldError("object", "email", "邮箱格式错误"));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        BindException exception = new BindException(bindingResult);

        Result<Void> result = exceptionHandler.handleBindException(exception);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("姓名不能为空"));
        assertTrue(result.getMessage().contains("年龄必须大于0"));
        assertTrue(result.getMessage().contains("邮箱格式错误"));
    }
}
