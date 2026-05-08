package com.rental.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    @DisplayName("success - 无参")
    void success_noArgs() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("success - 带数据")
    void success_withData() {
        Result<String> result = Result.success("test-data");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test-data", result.getData());
    }

    @Test
    @DisplayName("success - 带消息和数据")
    void success_withMessageAndData() {
        Result<String> result = Result.success("登录成功", "token-abc");

        assertEquals(200, result.getCode());
        assertEquals("登录成功", result.getMessage());
        assertEquals("token-abc", result.getData());
    }

    @Test
    @DisplayName("error - 默认500")
    void error_default() {
        Result<Void> result = Result.error("系统错误");

        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("error - 自定义code")
    void error_customCode() {
        Result<Void> result = Result.error(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    @DisplayName("unauthorized")
    void unauthorized() {
        Result<Void> result = Result.unauthorized("未登录");

        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMessage());
    }

    @Test
    @DisplayName("forbidden")
    void forbidden() {
        Result<Void> result = Result.forbidden("无权限");

        assertEquals(403, result.getCode());
        assertEquals("无权限", result.getMessage());
    }

    @Test
    @DisplayName("notFound")
    void notFound() {
        Result<Void> result = Result.notFound("资源不存在");

        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
    }

    @Test
    @DisplayName("badRequest")
    void badRequest() {
        Result<Void> result = Result.badRequest("参数不合法");

        assertEquals(400, result.getCode());
        assertEquals("参数不合法", result.getMessage());
    }
}
