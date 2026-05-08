package com.rental.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.dto.CommunityTreeDTO;
import com.rental.entity.Building;
import com.rental.entity.Community;
import com.rental.mapper.BuildingMapper;
import com.rental.mapper.CommunityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CommunityServiceImpl测试")
@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private CommunityMapper communityMapper;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("测试Community实体")
    void testCommunityEntity() {
        Community community = new Community();
        community.setId(1L);
        community.setName("阳光小区");
        community.setAddress("测试地址");
        community.setStatus(1);

        assertEquals(1L, community.getId());
        assertEquals("阳光小区", community.getName());
        assertEquals("测试地址", community.getAddress());
        assertEquals(1, community.getStatus());
    }

    @Test
    @DisplayName("测试Building实体")
    void testBuildingEntity() {
        Building building = new Building();
        building.setId(101L);
        building.setName("1号楼");
        building.setCommunityId(1L);
        building.setFloors(6);
        building.setStatus(1);

        assertEquals(101L, building.getId());
        assertEquals("1号楼", building.getName());
        assertEquals(1L, building.getCommunityId());
        assertEquals(6, building.getFloors());
        assertEquals(1, building.getStatus());
    }

    @Test
    @DisplayName("测试CommunityTreeDTO实体")
    void testCommunityTreeDTO() {
        CommunityTreeDTO dto = new CommunityTreeDTO();
        dto.setId(1L);
        dto.setName("测试小区");
        dto.setType("community");
        dto.setNodeKey("community_1");
        dto.setParentId(null);
        dto.setChildren(new ArrayList<>());

        assertEquals(1L, dto.getId());
        assertEquals("测试小区", dto.getName());
        assertEquals("community", dto.getType());
        assertEquals("community_1", dto.getNodeKey());
        assertNull(dto.getParentId());
        assertTrue(dto.getChildren().isEmpty());
    }
}
