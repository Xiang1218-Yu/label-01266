package com.rental.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.Constants;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityMapper communityMapper;

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Mock
    private com.rental.mapper.CommunityMapper baseMapperMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(communityService, "baseMapper", communityMapper);
    }

    @Test
    @DisplayName("获取小区树 - 缓存命中")
    void getCommunityTree_cacheHit() throws Exception {
        String json = "[{\"id\":1,\"name\":\"阳光花园\"}]";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_COMMUNITY_TREE)).thenReturn(json);

        List<CommunityTreeDTO> cachedTree = new ArrayList<>();
        CommunityTreeDTO dto = new CommunityTreeDTO();
        dto.setId(1L);
        dto.setName("阳光花园");
        cachedTree.add(dto);

        when(objectMapper.readValue(eq(json), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(cachedTree);

        List<CommunityTreeDTO> result = communityService.getCommunityTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("阳光花园", result.get(0).getName());
    }

    @Test
    @DisplayName("获取小区树 - 缓存未命中")
    void getCommunityTree_cacheMiss() throws Exception {
        Community testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");
        testCommunity.setStatus(1);

        Building testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setCommunityId(1L);
        testBuilding.setName("1栋");
        testBuilding.setStatus(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_COMMUNITY_TREE)).thenReturn(null);
        when(communityMapper.selectList(any())).thenReturn(Arrays.asList(testCommunity));
        when(buildingMapper.selectList(any())).thenReturn(Arrays.asList(testBuilding));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{}]");

        List<CommunityTreeDTO> result = communityService.getCommunityTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("阳光花园", result.get(0).getName());
        assertEquals("community", result.get(0).getType());
        assertEquals("community_1", result.get(0).getNodeKey());
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("building", result.get(0).getChildren().get(0).getType());
        assertEquals("building_1", result.get(0).getChildren().get(0).getNodeKey());
    }

    @Test
    @DisplayName("搜索小区 - 关键词为空时返回完整树")
    void searchCommunity_blankKeyword() throws Exception {
        Community testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");
        testCommunity.setStatus(1);

        Building testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setCommunityId(1L);
        testBuilding.setName("1栋");
        testBuilding.setStatus(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_COMMUNITY_TREE)).thenReturn(null);
        when(communityMapper.selectList(any())).thenReturn(Arrays.asList(testCommunity));
        when(buildingMapper.selectList(any())).thenReturn(Arrays.asList(testBuilding));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{}]");

        List<CommunityTreeDTO> result = communityService.searchCommunity("");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("搜索小区 - 按关键词搜索")
    void searchCommunity_withKeyword() {
        Community testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");
        testCommunity.setStatus(1);

        Building testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setCommunityId(1L);
        testBuilding.setName("1栋");
        testBuilding.setStatus(1);

        when(communityMapper.selectList(any())).thenReturn(Arrays.asList(testCommunity));
        when(buildingMapper.selectList(any())).thenReturn(Arrays.asList(testBuilding));

        List<CommunityTreeDTO> result = communityService.searchCommunity("阳光");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("阳光花园", result.get(0).getName());
    }

    @Test
    @DisplayName("搜索小区 - 无匹配结果")
    void searchCommunity_noMatch() {
        when(communityMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<CommunityTreeDTO> result = communityService.searchCommunity("不存在的");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("刷新缓存")
    void refreshCache() throws Exception {
        Community testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");
        testCommunity.setStatus(1);

        Building testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setCommunityId(1L);
        testBuilding.setName("1栋");
        testBuilding.setStatus(1);

        when(communityMapper.selectList(any())).thenReturn(Arrays.asList(testCommunity));
        when(buildingMapper.selectList(any())).thenReturn(Arrays.asList(testBuilding));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{}]");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        communityService.refreshCache();

        verify(redisTemplate).delete(Constants.CACHE_COMMUNITY_TREE);
    }
}
