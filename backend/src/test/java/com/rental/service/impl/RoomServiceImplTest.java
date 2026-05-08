package com.rental.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.Constants;
import com.rental.dto.RoomStatsDTO;
import com.rental.entity.Building;
import com.rental.entity.Room;
import com.rental.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RoomServiceImpl测试")
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private FamilyMemberMapper familyMemberMapper;

    @Mock
    private RoomOperationLogMapper roomOperationLogMapper;

    @Mock
    private CommunityMapper communityMapper;

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("测试RoomStatsDTO实体")
    void testRoomStatsDTO() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(100);
        stats.setRentedCount(70);
        stats.setVacantCount(30);

        assertEquals(100, stats.getTotalCount());
        assertEquals(70, stats.getRentedCount());
        assertEquals(30, stats.getVacantCount());
        assertEquals(new BigDecimal("70.00"), stats.getOccupancyRate());
    }

    @Test
    @DisplayName("测试RoomStatsDTO入住率计算 - 总数为0")
    void testRoomStatsDTOOccupancyRateZeroTotal() {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(0);
        stats.setRentedCount(0);
        stats.setVacantCount(0);

        assertEquals(BigDecimal.ZERO, stats.getOccupancyRate());
    }

    @Test
    @DisplayName("测试Room实体")
    void testRoomEntity() {
        Room room = new Room();
        room.setId(1L);
        room.setBuildingId(101L);
        room.setRoomNumber("101");
        room.setFloor(1);
        room.setArea(new BigDecimal("50.5"));
        room.setRent(new BigDecimal("1500.0"));
        room.setStatus(1);
        room.setRemark("测试房屋");

        assertEquals(1L, room.getId());
        assertEquals(101L, room.getBuildingId());
        assertEquals("101", room.getRoomNumber());
        assertEquals(1, room.getFloor());
        assertEquals(new BigDecimal("50.5"), room.getArea());
        assertEquals(new BigDecimal("1500.0"), room.getRent());
        assertEquals(1, room.getStatus());
        assertEquals("测试房屋", room.getRemark());
    }

    @Test
    @DisplayName("测试Building实体")
    void testBuildingEntity() {
        Building building = new Building();
        building.setId(101L);
        building.setCommunityId(1L);
        building.setName("1号楼");
        building.setFloors(6);
        building.setStatus(1);

        assertEquals(101L, building.getId());
        assertEquals(1L, building.getCommunityId());
        assertEquals("1号楼", building.getName());
        assertEquals(6, building.getFloors());
        assertEquals(1, building.getStatus());
    }
}
