package com.rental.controller;

import com.rental.dto.*;
import com.rental.entity.Room;
import com.rental.security.LoginUser;
import com.rental.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController).build();

        LoginUser loginUser = new LoginUser(1L, "admin");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取楼栋统计")
    void getBuildingStats() throws Exception {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(6);
        stats.setVacantCount(4);

        when(roomService.getBuildingStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/room/stats/building/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(10));
    }

    @Test
    @DisplayName("获取小区统计")
    void getCommunityStats() throws Exception {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(20);
        stats.setRentedCount(12);
        stats.setVacantCount(8);

        when(roomService.getCommunityStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/room/stats/community/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(20));
    }

    @Test
    @DisplayName("获取房屋卡片列表")
    void getRoomCards() throws Exception {
        RoomCardDTO card = new RoomCardDTO();
        card.setId(1L);
        card.setRoomNumber("101");
        card.setStatus(1);
        card.setTenantName("张三");

        when(roomService.getRoomCards(1L, null, null)).thenReturn(Arrays.asList(card));

        mockMvc.perform(get("/room/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].roomNumber").value("101"));
    }

    @Test
    @DisplayName("获取楼层列表")
    void getBuildingFloors() throws Exception {
        when(roomService.getBuildingFloors(1L)).thenReturn(Arrays.asList(1, 2, 3));

        mockMvc.perform(get("/room/floors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value(1));
    }

    @Test
    @DisplayName("获取房屋详情")
    void getRoomDetail() throws Exception {
        RoomDetailDTO detail = new RoomDetailDTO();
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        detail.setRoom(room);

        when(roomService.getRoomDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/room/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.room.roomNumber").value("101"));
    }

    @Test
    @DisplayName("保存房屋详情")
    void saveRoomDetail() throws Exception {
        doNothing().when(roomService).saveRoomDetail(any(), eq(1L), eq("admin"));

        String json = "{\"roomId\":1,\"status\":0,\"area\":65.5,\"rent\":800}";
        mockMvc.perform(post("/room/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roomService).saveRoomDetail(any(), eq(1L), eq("admin"));
    }

    @Test
    @DisplayName("导入房源")
    void importRooms() throws Exception {
        doNothing().when(roomService).importRooms(any());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "content".getBytes());

        mockMvc.perform(multipart("/room/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roomService).importRooms(any());
    }

    @Test
    @DisplayName("导出房源")
    void exportRooms() throws Exception {
        doNothing().when(roomService).exportRooms(any(), any(), any());

        mockMvc.perform(get("/room/export"))
                .andExpect(status().isOk());

        verify(roomService).exportRooms(isNull(), isNull(), any());
    }

    @Test
    @DisplayName("导出房源 - 带communityId和buildingId")
    void exportRooms_withParams() throws Exception {
        doNothing().when(roomService).exportRooms(eq(1L), eq(2L), any());

        mockMvc.perform(get("/room/export")
                        .param("communityId", "1")
                        .param("buildingId", "2"))
                .andExpect(status().isOk());

        verify(roomService).exportRooms(eq(1L), eq(2L), any());
    }
}
