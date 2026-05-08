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

    @Test
    @DisplayName("测试success(null) - 空数据")
    void testSuccessWithNullData() {
        Result<String> result = Result.success((String) null);

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试success(String message, null) - 消息非空但数据为空")
    void testSuccessWithMessageAndNullData() {
        Result<Integer> result = Result.success("测试消息", null);

        assertEquals(200, result.getCode());
        assertEquals("测试消息", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试success(String message, T data) - 空消息")
    void testSuccessWithEmptyMessage() {
        Result<String> result = Result.success("", "data");

        assertEquals(200, result.getCode());
        assertEquals("", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("测试error(null) - 空错误消息")
    void testErrorWithNullMessage() {
        Result<Void> result = Result.error(null);

        assertEquals(500, result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试error(0, null) - 零错误码和空消息")
    void testErrorWithZeroCodeAndNullMessage() {
        Result<Void> result = Result.error(0, null);

        assertEquals(0, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("测试error(-1, message) - 负数错误码")
    void testErrorWithNegativeCode() {
        Result<Void> result = Result.error(-1, "未知错误");

        assertEquals(-1, result.getCode());
        assertEquals("未知错误", result.getMessage());
    }

    @Test
    @DisplayName("测试unauthorized(null)")
    void testUnauthorizedWithNullMessage() {
        Result<Void> result = Result.unauthorized(null);

        assertEquals(401, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("测试forbidden(null)")
    void testForbiddenWithNullMessage() {
        Result<Void> result = Result.forbidden(null);

        assertEquals(403, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("测试notFound(null)")
    void testNotFoundWithNullMessage() {
        Result<Void> result = Result.notFound(null);

        assertEquals(404, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("测试badRequest(null)")
    void testBadRequestWithNullMessage() {
        Result<Void> result = Result.badRequest(null);

        assertEquals(400, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("测试泛型 - 复杂对象类型")
    void testGenericTypeWithComplexObject() {
        java.util.List<String> listData = java.util.Arrays.asList("a", "b", "c");
        Result<java.util.List<String>> result = Result.success(listData);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
        assertEquals("a", result.getData().get(0));
    }

    @Test
    @DisplayName("测试泛型 - 包装类型Integer")
    void testGenericTypeWithInteger() {
        Result<Integer> result = Result.success(42);

        assertEquals(200, result.getCode());
        assertEquals(Integer.valueOf(42), result.getData());
    }

    @Test
    @DisplayName("测试泛型 - Long类型")
    void testGenericTypeWithLong() {
        Result<Long> result = Result.success(999999999999999L);

        assertEquals(200, result.getCode());
        assertEquals(Long.valueOf(999999999999999L), result.getData());
    }

    @Test
    @DisplayName("测试可序列化")
    void testSerializable() {
        Result<String> result = Result.success("test");

        assertTrue(java.io.Serializable.class.isInstance(result));
    }

    @Test
    @DisplayName("测试timestamp自动生成")
    void testTimestampAutoGenerated() throws InterruptedException {
        Result<String> result1 = Result.success("test1");
        Thread.sleep(1);
        Result<String> result2 = Result.success("test2");

        assertTrue(result2.getTimestamp() >= result1.getTimestamp());
    }

    @Test
    @DisplayName("测试error空字符串")
    void testErrorWithEmptyMessage() {
        Result<Void> result = Result.error("");

        assertEquals(500, result.getCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("测试error大数字错误码")
    void testErrorWithLargeCode() {
        Result<Void> result = Result.error(99999, "极端错误");

        assertEquals(99999, result.getCode());
        assertEquals("极端错误", result.getMessage());
    }
}
