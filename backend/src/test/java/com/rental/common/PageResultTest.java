package com.rental.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    @DisplayName("分页结果构造 - 正常")
    void constructor_normal() {
        List<String> records = Arrays.asList("a", "b", "c");

        PageResult<String> result = new PageResult<>(records, 30, 10, 1);

        assertEquals(3, result.getRecords().size());
        assertEquals(30, result.getTotal());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getCurrent());
        assertEquals(3, result.getPages());
    }

    @Test
    @DisplayName("分页结果构造 - 空列表")
    void constructor_empty() {
        PageResult<String> result = new PageResult<>();

        assertNull(result.getRecords());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("分页结果 - 页数计算向上取整")
    void pageCalculation_ceil() {
        List<String> records = Arrays.asList("a");

        PageResult<String> result = new PageResult<>(records, 11, 10, 1);

        assertEquals(2, result.getPages());
    }
}
