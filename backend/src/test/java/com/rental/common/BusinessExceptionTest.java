package com.rental.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessException业务异常测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("测试BusinessException(String message)构造函数")
    void testConstructorWithMessage() {
        String message = "业务异常消息";
        BusinessException exception = new BusinessException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(500, exception.getCode());
    }

    @Test
    @DisplayName("测试BusinessException(int code, String message)构造函数")
    void testConstructorWithCodeAndMessage() {
        int code = 400;
        String message = "参数错误";
        BusinessException exception = new BusinessException(code, message);

        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    @DisplayName("测试BusinessException(String message, Throwable cause)构造函数")
    void testConstructorWithMessageAndCause() {
        String message = "包装异常消息";
        Throwable cause = new RuntimeException("原始异常");
        BusinessException exception = new BusinessException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(500, exception.getCode());
    }

    @Test
    @DisplayName("测试自定义错误码")
    void testCustomErrorCode() {
        int customCode = 404;
        String message = "资源不存在";
        BusinessException exception = new BusinessException(customCode, message);

        assertEquals(customCode, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("测试getCode方法")
    void testGetCode() {
        BusinessException exception = new BusinessException(401, "未授权");
        assertEquals(401, exception.getCode());
    }

    @Test
    @DisplayName("测试默认错误码为500")
    void testDefaultErrorCode() {
        BusinessException exception1 = new BusinessException("测试");
        assertEquals(500, exception1.getCode());

        BusinessException exception2 = new BusinessException("测试", new RuntimeException());
        assertEquals(500, exception2.getCode());
    }

    @Test
    @DisplayName("测试异常消息传递")
    void testExceptionMessage() {
        String message = "这是一个业务异常";
        BusinessException exception = new BusinessException(message);
        assertEquals(message, exception.getMessage());
    }
}
