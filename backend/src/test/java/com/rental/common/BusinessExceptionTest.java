package com.rental.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    @DisplayName("默认构造 - code为500")
    void constructor_messageOnly() {
        BusinessException ex = new BusinessException("业务错误");

        assertEquals("业务错误", ex.getMessage());
        assertEquals(500, ex.getCode());
    }

    @Test
    @DisplayName("自定义code构造")
    void constructor_codeAndMessage() {
        BusinessException ex = new BusinessException(400, "参数错误");

        assertEquals("参数错误", ex.getMessage());
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("带原因构造")
    void constructor_messageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException("业务错误", cause);

        assertEquals("业务错误", ex.getMessage());
        assertEquals(500, ex.getCode());
        assertEquals(cause, ex.getCause());
    }
}
