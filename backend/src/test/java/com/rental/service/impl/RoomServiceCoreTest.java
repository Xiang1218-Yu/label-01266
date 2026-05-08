package com.rental.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.Constants;
import com.rental.dto.RoomStatsDTO;
import com.rental.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomServiceImpl核心业务方法测试")
class RoomServiceCoreTest {

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
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoomServiceImpl roomService;

    @Mock
    private RoomMapper roomMapper;

    private RoomStatsDTO testStats;

    @BeforeEach
    void setUp() throws Exception {
        testStats = new RoomStatsDTO();
        testStats.setTotalCount(100);
        testStats.setRentedCount(70);
        testStats.setVacantCount(30);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(roomService, roomMapper);
    }

    @Test
    @DisplayName("getBuildingStats - 缓存命中")
    void testGetBuildingStatsCacheHit() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";
        String cacheJson = objectMapper.writeValueAsString(testStats);

        when(valueOperations.get(cacheKey)).thenReturn(cacheJson);

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(100, result.getTotalCount());
        assertEquals(70, result.getRentedCount());
        assertEquals(30, result.getVacantCount());

        verify(roomMapper, never()).getBuildingStats(anyLong());
    }

    @Test
    @DisplayName("getBuildingStats - 缓存未命中，从数据库获取")
    void testGetBuildingStatsCacheMiss() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(testStats);
        when(objectMapper.writeValueAsString(testStats)).thenReturn("{}");

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(100, result.getTotalCount());

        verify(roomMapper).getBuildingStats(1L);
        verify(valueOperations).set(eq(cacheKey), anyString(), eq(Constants.CACHE_EXPIRE_STATS), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("getBuildingStats - 数据库返回null，创建默认统计")
    void testGetBuildingStatsNullFromDB() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(null);

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRentedCount());
        assertEquals(0, result.getVacantCount());
    }

    @Test
    @DisplayName("getBuildingStats - 缓存解析异常，从数据库获取")
    void testGetBuildingStatsCacheParseError() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";

        when(valueOperations.get(cacheKey)).thenReturn("invalid json");
        when(roomMapper.getBuildingStats(1L)).thenReturn(testStats);

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(100, result.getTotalCount());
        verify(roomMapper).getBuildingStats(1L);
    }

    @Test
    @DisplayName("getCommunityStats - 缓存命中")
    void testGetCommunityStatsCacheHit() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:1";
        String cacheJson = objectMapper.writeValueAsString(testStats);

        when(valueOperations.get(cacheKey)).thenReturn(cacheJson);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertEquals(100, result.getTotalCount());
        verify(roomMapper, never()).getCommunityStats(anyLong());
    }

    @Test
    @DisplayName("getCommunityStats - 缓存未命中")
    void testGetCommunityStatsCacheMiss() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:1";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(roomMapper.getCommunityStats(1L)).thenReturn(testStats);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertEquals(100, result.getTotalCount());
        verify(roomMapper).getCommunityStats(1L);
    }

    @Test
    @DisplayName("getCommunityStats - 数据库返回null")
    void testGetCommunityStatsNullFromDB() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:1";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(roomMapper.getCommunityStats(1L)).thenReturn(null);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRentedCount());
        assertEquals(0, result.getVacantCount());
    }

    @Test
    @DisplayName("getBuildingStats - 缓存写入异常")
    void testGetBuildingStatsCacheWriteError() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(testStats);
        when(objectMapper.writeValueAsString(testStats)).thenThrow(new JsonProcessingException("write error") {});

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(100, result.getTotalCount());
    }

    @Test
    @DisplayName("getCommunityStats - 缓存解析异常")
    void testGetCommunityStatsCacheParseError() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:1";

        when(valueOperations.get(cacheKey)).thenReturn("{invalid json}");
        when(roomMapper.getCommunityStats(1L)).thenReturn(testStats);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertEquals(100, result.getTotalCount());
    }

    @Test
    @DisplayName("getBuildingStats - 空字符串缓存")
    void testGetBuildingStatsEmptyCache() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:1";

        when(valueOperations.get(cacheKey)).thenReturn("");
        when(roomMapper.getBuildingStats(1L)).thenReturn(testStats);

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertEquals(100, result.getTotalCount());
        verify(roomMapper).getBuildingStats(1L);
    }

    @Test
    @DisplayName("getCommunityStats - 空白字符缓存")
    void testGetCommunityStatsBlankCache() throws Exception {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:1";

        when(valueOperations.get(cacheKey)).thenReturn("   ");
        when(roomMapper.getCommunityStats(1L)).thenReturn(testStats);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertEquals(100, result.getTotalCount());
        verify(roomMapper).getCommunityStats(1L);
    }
}
