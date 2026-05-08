package com.rental.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HealthController测试")
class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }

    @Test
    @DisplayName("测试健康检查接口")
    void testHealth() {
        long before = System.currentTimeMillis();
        
        Map<String, Object> result = healthController.health();
        
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp"));
        assertEquals("UP", result.get("status"));
        
        Long timestamp = (Long) result.get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp >= before);
    }
}
