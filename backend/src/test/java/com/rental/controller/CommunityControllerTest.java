package com.rental.controller;

import com.rental.dto.CommunityTreeDTO;
import com.rental.service.CommunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommunityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommunityService communityService;

    @InjectMocks
    private CommunityController communityController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(communityController).build();
    }

    @Test
    @DisplayName("获取小区树")
    void getCommunityTree() throws Exception {
        List<CommunityTreeDTO> tree = new ArrayList<>();
        CommunityTreeDTO dto = new CommunityTreeDTO();
        dto.setId(1L);
        dto.setName("阳光花园");
        dto.setType("community");
        tree.add(dto);

        when(communityService.getCommunityTree()).thenReturn(tree);

        mockMvc.perform(get("/community/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("阳光花园"));
    }

    @Test
    @DisplayName("搜索小区")
    void searchCommunity() throws Exception {
        List<CommunityTreeDTO> tree = new ArrayList<>();
        CommunityTreeDTO dto = new CommunityTreeDTO();
        dto.setId(1L);
        dto.setName("阳光花园");
        tree.add(dto);

        when(communityService.searchCommunity("阳光")).thenReturn(tree);

        mockMvc.perform(get("/community/search").param("keyword", "阳光"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("阳光花园"));
    }

    @Test
    @DisplayName("刷新缓存")
    void refreshCache() throws Exception {
        mockMvc.perform(post("/community/refresh-cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
