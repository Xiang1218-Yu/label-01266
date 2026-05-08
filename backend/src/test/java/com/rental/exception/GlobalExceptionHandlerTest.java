package com.rental.exception;

import com.rental.common.BusinessException;
import com.rental.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @Test
    @DisplayName("测试处理参数校验异常-MethodArgumentNotValidException")
    void testHandleValidException() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field", "参数校验错误"));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        Result<Void> result = exceptionHandler.handleValidException(exception);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("参数校验错误"));
    }

    @Test
    @DisplayName("测试空字段错误列表")
    void testEmptyFieldErrors() {
        List<FieldError> fieldErrors = new ArrayList<>();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        BindException exception = new BindException(bindingResult);

        Result<Void> result = exceptionHandler.handleBindException(exception);

        assertEquals(400, result.getCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("测试空约束违反集合")
    void testEmptyConstraintViolations() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Result<Void> result = exceptionHandler.handleConstraintViolationException(exception);

        assertEquals(400, result.getCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("测试业务异常 - 不同错误码")
    void testBusinessExceptionDifferentCodes() {
        BusinessException notFound = new BusinessException(404, "资源不存在");
        BusinessException conflict = new BusinessException(409, "资源冲突");
        BusinessException serverError = new BusinessException(503, "服务不可用");

        Result<Void> result1 = exceptionHandler.handleBusinessException(notFound);
        Result<Void> result2 = exceptionHandler.handleBusinessException(conflict);
        Result<Void> result3 = exceptionHandler.handleBusinessException(serverError);

        assertEquals(404, result1.getCode());
        assertEquals(409, result2.getCode());
        assertEquals(503, result3.getCode());
    }

    @Test
    @DisplayName("测试异常消息为null")
    void testExceptionWithNullMessage() {
        Exception exception = new RuntimeException();

        Result<Void> result = exceptionHandler.handleException(exception);

        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("测试多个约束违反合并")
    void testMultipleConstraintViolations() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("用户名不能为空");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("密码长度不能小于6位");

        Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Result<Void> result = exceptionHandler.handleConstraintViolationException(exception);

        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("用户名不能为空"));
        assertTrue(result.getMessage().contains("密码长度不能小于6位"));
    }

    @Test
    @DisplayName("测试空认证异常消息")
    void testAuthenticationExceptionWithNullMessage() {
        org.springframework.security.core.AuthenticationException exception =
            new org.springframework.security.core.AuthenticationException(null) {};

        Result<Void> result = exceptionHandler.handleAuthenticationException(exception);

        assertEquals(401, result.getCode());
        assertEquals("认证失败，请重新登录", result.getMessage());
    }

    @Test
    @DisplayName("测试空授权异常消息")
    void testAccessDeniedExceptionWithNullMessage() {
        org.springframework.security.access.AccessDeniedException exception =
            new org.springframework.security.access.AccessDeniedException(null);

        Result<Void> result = exceptionHandler.handleAccessDeniedException(exception);

        assertEquals(403, result.getCode());
        assertEquals("权限不足", result.getMessage());
    }
}
