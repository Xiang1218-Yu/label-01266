package com.rental.controller;

import com.rental.common.Result;
import com.rental.dto.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RoomController测试")
@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private RoomStatsDTO testStats;
    private List<RoomCardDTO> testCards;

    @BeforeEach
    void setUp() {
        testStats = new RoomStatsDTO();
        testStats.setTotalCount(100);
        testStats.setRentedCount(70);
        testStats.setVacantCount(30);

        testCards = new ArrayList<>();
        RoomCardDTO card = new RoomCardDTO();
        card.setId(1L);
        card.setRoomNumber("101");
        card.setFloor(1);
        card.setStatus(1);
        testCards.add(card);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        LoginUser loginUser = new LoginUser(1L, "admin");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("测试获取楼栋统计")
    void testGetBuildingStats() {
        Long buildingId = 101L;
        when(roomService.getBuildingStats(buildingId)).thenReturn(testStats);

        Result<RoomStatsDTO> result = roomController.getBuildingStats(buildingId);

        assertEquals(200, result.getCode());
        assertEquals(100, result.getData().getTotalCount());
        verify(roomService).getBuildingStats(buildingId);
    }

    @Test
    @DisplayName("测试获取小区统计")
    void testGetCommunityStats() {
        Long communityId = 1L;
        when(roomService.getCommunityStats(communityId)).thenReturn(testStats);

        Result<RoomStatsDTO> result = roomController.getCommunityStats(communityId);

        assertEquals(200, result.getCode());
        assertEquals(70, result.getData().getRentedCount());
        verify(roomService).getCommunityStats(communityId);
    }

    @Test
    @DisplayName("测试获取房屋卡片列表")
    void testGetRoomCards() {
        Long buildingId = 101L;
        Integer floor = 1;
        Integer status = 1;

        when(roomService.getRoomCards(buildingId, floor, status)).thenReturn(testCards);

        Result<List<RoomCardDTO>> result = roomController.getRoomCards(buildingId, floor, status);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("101", result.getData().get(0).getRoomNumber());
    }

    @Test
    @DisplayName("测试获取楼栋楼层列表")
    void testGetBuildingFloors() {
        Long buildingId = 101L;
        List<Integer> floors = List.of(1, 2, 3);
        when(roomService.getBuildingFloors(buildingId)).thenReturn(floors);

        Result<List<Integer>> result = roomController.getBuildingFloors(buildingId);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
    }

    @Test
    @DisplayName("测试获取房屋详情")
    void testGetRoomDetail() {
        Long roomId = 1L;
        RoomDetailDTO detail = new RoomDetailDTO();
        when(roomService.getRoomDetail(roomId)).thenReturn(detail);

        Result<RoomDetailDTO> result = roomController.getRoomDetail(roomId);

        assertEquals(200, result.getCode());
        assertEquals(detail, result.getData());
    }

    @Test
    @DisplayName("测试保存房屋详情")
    void testSaveRoomDetail() {
        setupSecurityContext();
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);

        Result<Void> result = roomController.saveRoomDetail(dto);

        assertEquals(200, result.getCode());
        assertEquals("保存成功", result.getMessage());
        verify(roomService).saveRoomDetail(eq(dto), anyLong(), anyString());
    }

    @Test
    @DisplayName("测试导入房源")
    void testImportRooms() {
        MultipartFile file = mock(MultipartFile.class);

        Result<Void> result = roomController.importRooms(file);

        assertEquals(200, result.getCode());
        assertEquals("导入成功", result.getMessage());
        verify(roomService).importRooms(file);
    }

    @Test
    @DisplayName("测试导出房源")
    void testExportRooms() {
        Long communityId = 1L;
        Long buildingId = null;
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertDoesNotThrow(() -> {
            roomController.exportRooms(communityId, buildingId, response);
        });

        verify(roomService).exportRooms(communityId, buildingId, response);
    }
}
