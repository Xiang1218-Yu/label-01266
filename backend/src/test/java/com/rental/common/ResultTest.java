package com.rental.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result统一响应结果测试")
class ResultTest {

    @Test
    @DisplayName("测试success()无参数方法")
    void testSuccessWithoutData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试success(T data)带数据方法")
    void testSuccessWithData() {
        String testData = "test data";
        Result<String> result = Result.success(testData);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    @DisplayName("测试success(String message, T data)带消息和数据方法")
    void testSuccessWithMessageAndData() {
        String message = "自定义成功消息";
        String data = "test data";
        Result<String> result = Result.success(message, data);
        assertEquals(200, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("测试error(String message)方法")
    void testErrorWithMessage() {
        String message = "错误消息";
        Result<Void> result = Result.error(message);
        assertEquals(500, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试error(int code, String message)方法")
    void testErrorWithCodeAndMessage() {
        int code = 400;
        String message = "错误消息";
        Result<Void> result = Result.error(code, message);
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试unauthorized方法")
    void testUnauthorized() {
        String message = "未授权";
        Result<Void> result = Result.unauthorized(message);
        assertEquals(401, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试forbidden方法")
    void testForbidden() {
        String message = "禁止访问";
        Result<Void> result = Result.forbidden(message);
        assertEquals(403, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试notFound方法")
    void testNotFound() {
        String message = "未找到";
        Result<Void> result = Result.notFound(message);
        assertEquals(404, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试badRequest方法")
    void testBadRequest() {
        String message = "请求错误";
        Result<Void> result = Result.badRequest(message);
        assertEquals(400, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        Result<String> result = new Result<>();
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试setter方法")
    void testSetters() {
        Result<String> result = new Result<>();
        result.setCode(201);
        result.setMessage("Created");
        result.setData("test");

        assertEquals(201, result.getCode());
        assertEquals("Created", result.getMessage());
        assertEquals("test", result.getData());
    }
}
