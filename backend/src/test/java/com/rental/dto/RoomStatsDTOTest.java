package com.rental.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoomStatsDTO测试")
class RoomStatsDTOTest {

    @Test
    @DisplayName("测试入住率计算 - 正常情况")
    void testGetOccupancyRateNormal() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(100);
        stats.setRentedCount(70);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("70.00"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 总户数为0")
    void testGetOccupancyRateTotalZero() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(0);
        stats.setRentedCount(0);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("测试入住率计算 - 总户数为null")
    void testGetOccupancyRateTotalNull() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(null);
        stats.setRentedCount(50);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("测试入住率计算 - 全部出租")
    void testGetOccupancyRateAllRented() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(10);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 全部空置")
    void testGetOccupancyRateAllVacant() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(50);
        stats.setRentedCount(0);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 四舍五入")
    void testGetOccupancyRateRounding() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(3);
        stats.setRentedCount(1);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    @DisplayName("测试setter和getter方法")
    void testSettersAndGetters() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(200);
        stats.setRentedCount(150);
        stats.setVacantCount(50);

        assertEquals(200, stats.getTotalCount());
        assertEquals(150, stats.getRentedCount());
        assertEquals(50, stats.getVacantCount());
    }

    @Test
    @DisplayName("测试入住率计算 - 极大数值")
    void testGetOccupancyRateLargeNumbers() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(1000000);
        stats.setRentedCount(999999);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 极小比率精确计算")
    void testGetOccupancyRateMinimalRate() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10000);
        stats.setRentedCount(1);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("0.01"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 出租数大于总数（边界异常）")
    void testGetOccupancyRateRentedGreaterThanTotal() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(15);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    @DisplayName("测试入住率计算 - 出租数为null时的异常行为")
    void testGetOccupancyRateRentedNullThrowsNPE() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(null);

        assertThrows(NullPointerException.class, stats::getOccupancyRate);
    }

    @Test
    @DisplayName("测试可序列化")
    void testSerializable() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(100);
        stats.setRentedCount(70);
        stats.setVacantCount(30);

        assertTrue(java.io.Serializable.class.isInstance(stats));
    }

    @Test
    @DisplayName("测试全部字段为null")
    void testAllFieldsNull() {
        RoomStatsDTO stats = new RoomStatsDTO();

        assertNull(stats.getTotalCount());
        assertNull(stats.getRentedCount());
        assertNull(stats.getVacantCount());
        assertEquals(BigDecimal.ZERO, stats.getOccupancyRate());
    }

    @Test
    @DisplayName("测试大数四舍五入进位")
    void testGetOccupancyRateRoundingUp() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(20000);
        stats.setRentedCount(13333);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("66.67"), result);
    }

    @Test
    @DisplayName("测试大数四舍五入舍去")
    void testGetOccupancyRateRoundingDown() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(1000);
        stats.setRentedCount(123);

        BigDecimal result = stats.getOccupancyRate();

        assertEquals(new BigDecimal("12.30"), result);
    }
}
