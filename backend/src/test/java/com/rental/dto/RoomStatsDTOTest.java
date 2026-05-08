package com.rental.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RoomStatsDTOTest {

    @Test
    @DisplayName("入住率计算 - 正常计算")
    void occupancyRate_normal() {
        RoomStatsDTO dto = new RoomStatsDTO();
        dto.setTotalCount(10);
        dto.setRentedCount(6);
        dto.setVacantCount(4);

        BigDecimal rate = dto.getOccupancyRate();

        assertEquals(new BigDecimal("60.00"), rate);
    }

    @Test
    @DisplayName("入住率计算 - 总数为0时返回0")
    void occupancyRate_zeroTotal() {
        RoomStatsDTO dto = new RoomStatsDTO();
        dto.setTotalCount(0);
        dto.setRentedCount(0);
        dto.setVacantCount(0);

        BigDecimal rate = dto.getOccupancyRate();

        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    @DisplayName("入住率计算 - 总数为null时返回0")
    void occupancyRate_nullTotal() {
        RoomStatsDTO dto = new RoomStatsDTO();
        dto.setTotalCount(null);
        dto.setRentedCount(0);

        BigDecimal rate = dto.getOccupancyRate();

        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    @DisplayName("入住率计算 - 100%入住")
    void occupancyRate_full() {
        RoomStatsDTO dto = new RoomStatsDTO();
        dto.setTotalCount(5);
        dto.setRentedCount(5);
        dto.setVacantCount(0);

        BigDecimal rate = dto.getOccupancyRate();

        assertEquals(new BigDecimal("100.00"), rate);
    }
}
