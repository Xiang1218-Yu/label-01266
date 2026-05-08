package com.rental.controller;

import com.rental.common.Result;
import com.rental.dto.CommunityTreeDTO;
import com.rental.service.CommunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CommunityController测试")
@ExtendWith(MockitoExtension.class)
class CommunityControllerTest {

    @Mock
    private CommunityService communityService;

    @InjectMocks
    private CommunityController communityController;

    private List<CommunityTreeDTO> testTree;

    @BeforeEach
    void setUp() {
        testTree = new ArrayList<>();
        CommunityTreeDTO community1 = new CommunityTreeDTO();
        community1.setId(1L);
        community1.setName("阳光小区");
        community1.setType("community");
        community1.setNodeKey("community_1");
        testTree.add(community1);

        CommunityTreeDTO building1 = new CommunityTreeDTO();
        building1.setId(101L);
        building1.setName("1号楼");
        building1.setType("building");
        building1.setNodeKey("building_101");
        building1.setParentId(1L);
        community1.setChildren(List.of(building1));
    }

    @Test
    @DisplayName("测试获取小区树结构")
    void testGetCommunityTree() {
        when(communityService.getCommunityTree()).thenReturn(testTree);

        Result<List<CommunityTreeDTO>> result = communityController.getCommunityTree();

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(1, result.getData().size());
        assertEquals("阳光小区", result.getData().get(0).getName());
    }

    @Test
    @DisplayName("测试搜索小区 - 有关键词")
    void testSearchCommunityWithKeyword() {
        String keyword = "阳光";
        when(communityService.searchCommunity(keyword)).thenReturn(testTree);

        Result<List<CommunityTreeDTO>> result = communityController.searchCommunity(keyword);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(communityService).searchCommunity(keyword);
    }

    @Test
    @DisplayName("测试搜索小区 - 无关键词")
    void testSearchCommunityWithoutKeyword() {
        when(communityService.searchCommunity(null)).thenReturn(testTree);

        Result<List<CommunityTreeDTO>> result = communityController.searchCommunity(null);

        assertEquals(200, result.getCode());
        verify(communityService).searchCommunity(null);
    }

    @Test
    @DisplayName("测试刷新缓存")
    void testRefreshCache() {
        Result<Void> result = communityController.refreshCache();

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        verify(communityService).refreshCache();
    }

    @Test
    @DisplayName("测试获取小区树 - 空数据")
    void testGetCommunityTreeEmpty() {
        when(communityService.getCommunityTree()).thenReturn(new ArrayList<>());

        Result<List<CommunityTreeDTO>> result = communityController.getCommunityTree();

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }
}
