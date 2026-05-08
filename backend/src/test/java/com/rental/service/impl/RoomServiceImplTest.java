package com.rental.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.BusinessException;
import com.rental.common.Constants;
import com.rental.dto.*;
import com.rental.entity.*;
import com.rental.mapper.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
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
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room testRoom;
    private Tenant testTenant;
    private Building testBuilding;
    private Community testCommunity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roomService, "baseMapper", roomMapper);
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Room.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Building.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Community.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Tenant.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), FamilyMember.class);
    }

    private Room createTestRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setBuildingId(1L);
        room.setRoomNumber("101");
        room.setFloor(1);
        room.setArea(new BigDecimal("65.5"));
        room.setRent(new BigDecimal("800"));
        room.setStatus(Constants.ROOM_STATUS_VACANT);
        return room;
    }

    private Tenant createTestTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setRoomId(1L);
        tenant.setName("张三");
        tenant.setIdCard("110101199001011234");
        tenant.setPhone("13800138001");
        tenant.setStatus(1);
        tenant.setCheckInTime(LocalDateTime.now());
        return tenant;
    }

    @Test
    @DisplayName("获取楼栋统计 - 缓存命中")
    void getBuildingStats_cacheHit() throws Exception {
        testRoom = createTestRoom();
        testTenant = createTestTenant();
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(6);
        stats.setVacantCount(4);

        String json = "{\"totalCount\":10}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "building:1")).thenReturn(json);
        when(objectMapper.readValue(eq(json), eq(RoomStatsDTO.class))).thenReturn(stats);

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertNotNull(result);
        assertEquals(10, result.getTotalCount());
    }

    @Test
    @DisplayName("获取楼栋统计 - 缓存未命中")
    void getBuildingStats_cacheMiss() throws Exception {
        testRoom = createTestRoom();
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(10);
        stats.setRentedCount(6);
        stats.setVacantCount(4);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "building:1")).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(stats);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertNotNull(result);
        assertEquals(10, result.getTotalCount());
    }

    @Test
    @DisplayName("获取楼栋统计 - 数据库返回null时返回空统计")
    void getBuildingStats_nullFromDb() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "building:1")).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRentedCount());
        assertEquals(0, result.getVacantCount());
    }

    @Test
    @DisplayName("获取楼栋统计 - 缓存解析失败时回源数据库")
    void getBuildingStats_cacheParseFail() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "building:1")).thenReturn("invalid-json");
        when(objectMapper.readValue(eq("invalid-json"), eq(RoomStatsDTO.class))).thenThrow(new RuntimeException("parse error"));

        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(5);
        stats.setRentedCount(3);
        stats.setVacantCount(2);
        when(roomMapper.getBuildingStats(1L)).thenReturn(stats);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertNotNull(result);
        assertEquals(5, result.getTotalCount());
    }

    @Test
    @DisplayName("获取小区统计 - 缓存命中")
    void getCommunityStats_cacheHit() throws Exception {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(20);
        stats.setRentedCount(12);
        stats.setVacantCount(8);

        String json = "{\"totalCount\":20}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "community:1")).thenReturn(json);
        when(objectMapper.readValue(eq(json), eq(RoomStatsDTO.class))).thenReturn(stats);

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertNotNull(result);
        assertEquals(20, result.getTotalCount());
    }

    @Test
    @DisplayName("获取小区统计 - 缓存未命中")
    void getCommunityStats_cacheMiss() throws Exception {
        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(20);
        stats.setRentedCount(12);
        stats.setVacantCount(8);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "community:1")).thenReturn(null);
        when(roomMapper.getCommunityStats(1L)).thenReturn(stats);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertNotNull(result);
        assertEquals(20, result.getTotalCount());
    }

    @Test
    @DisplayName("获取房屋详情 - 房屋不存在时抛出异常")
    void getRoomDetail_roomNotFound() {
        when(roomMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> roomService.getRoomDetail(999L));
    }

    @Test
    @DisplayName("获取房屋详情 - 正常获取含承租人")
    void getRoomDetail_withTenant() {
        testRoom = createTestRoom();
        testTenant = createTestTenant();
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());

        RoomDetailDTO result = roomService.getRoomDetail(1L);

        assertNotNull(result);
        assertNotNull(result.getRoom());
        assertNotNull(result.getTenant());
        assertEquals("张三", result.getTenant().getName());
        assertNotNull(result.getFamilyMembers());
        assertNotNull(result.getOperationLogs());
    }

    @Test
    @DisplayName("获取房屋详情 - 无承租人时返回空列表")
    void getRoomDetail_noTenant() {
        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_VACANT);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());

        RoomDetailDTO result = roomService.getRoomDetail(1L);

        assertNotNull(result);
        assertNull(result.getTenant());
        assertTrue(result.getFamilyMembers().isEmpty());
    }

    @Test
    @DisplayName("保存房屋详情 - 房屋不存在抛出异常")
    void saveRoomDetail_roomNotFound() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(999L);
        when(roomMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 已出租状态承租人姓名为空抛出异常")
    void saveRoomDetail_rentedWithoutName() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("");
        testRoom = createTestRoom();
        when(roomMapper.selectById(1L)).thenReturn(testRoom);

        assertThrows(BusinessException.class, () -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 已出租状态身份证为空抛出异常")
    void saveRoomDetail_rentedWithoutIdCard() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("");
        testRoom = createTestRoom();
        when(roomMapper.selectById(1L)).thenReturn(testRoom);

        assertThrows(BusinessException.class, () -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 已出租状态联系方式为空抛出异常")
    void saveRoomDetail_rentedWithoutPhone() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("");
        testRoom = createTestRoom();
        when(roomMapper.selectById(1L)).thenReturn(testRoom);

        assertThrows(BusinessException.class, () -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 已出租状态入住时间为空抛出异常")
    void saveRoomDetail_rentedWithoutCheckInTime() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        dto.setCheckInTime(null);
        testRoom = createTestRoom();
        when(roomMapper.selectById(1L)).thenReturn(testRoom);

        assertThrows(BusinessException.class, () -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 更换承租人")
    void saveRoomDetail_changeTenant() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setChangeTenant(true);
        dto.setTenantName("李四");
        dto.setIdCard("110101199002022345");
        dto.setPhone("13800138002");
        dto.setCheckInTime(LocalDateTime.now());
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant).thenReturn(null);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(tenantMapper.insert(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        verify(tenantMapper).updateById(any(Tenant.class));
        verify(tenantMapper).insert(any(Tenant.class));
    }

    @Test
    @DisplayName("保存房屋详情 - 新增承租人")
    void saveRoomDetail_addTenant() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        dto.setCheckInTime(LocalDateTime.now());
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testBuilding = createTestBuilding();

        Tenant newTenant = new Tenant();
        newTenant.setId(2L);
        newTenant.setName("张三");
        newTenant.setStatus(1);
        newTenant.setRoomId(1L);

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(null).thenReturn(newTenant);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.insert(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        verify(tenantMapper).insert(any(Tenant.class));
    }

    @Test
    @DisplayName("保存房屋详情 - 状态改为空置时清除承租人")
    void saveRoomDetail_setVacant() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_VACANT);
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant).thenReturn(null);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        verify(tenantMapper).updateById(any(Tenant.class));
    }

    @Test
    @DisplayName("保存房屋详情 - 普通修改")
    void saveRoomDetail_update() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        dto.setCheckInTime(LocalDateTime.now());
        dto.setArea(new BigDecimal("70.0"));
        dto.setRent(new BigDecimal("900"));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        verify(roomMapper).updateById(any(Room.class));
    }

    @Test
    @DisplayName("获取楼栋楼层列表")
    void getBuildingFloors() {
        Room r1 = new Room();
        r1.setFloor(1);
        Room r2 = new Room();
        r2.setFloor(2);
        Room r3 = new Room();
        r3.setFloor(3);

        when(roomMapper.selectList(any())).thenReturn(Arrays.asList(r1, r2, r3));

        List<Integer> floors = roomService.getBuildingFloors(1L);

        assertNotNull(floors);
        assertEquals(3, floors.size());
        assertTrue(floors.contains(1));
        assertTrue(floors.contains(2));
        assertTrue(floors.contains(3));
    }

    @Test
    @DisplayName("获取房屋卡片列表")
    void getRoomCards() {
        List<RoomCardDTO> cards = new ArrayList<>();
        RoomCardDTO card = new RoomCardDTO();
        card.setId(1L);
        card.setRoomNumber("101");
        card.setStatus(1);
        cards.add(card);

        when(roomMapper.getRoomCards(1L, 1, 1)).thenReturn(cards);

        List<RoomCardDTO> result = roomService.getRoomCards(1L, 1, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).getRoomNumber());
    }

    private Building createTestBuilding() {
        Building b = new Building();
        b.setId(1L);
        b.setCommunityId(1L);
        b.setName("1栋");
        b.setStatus(1);
        return b;
    }

    @Test
    @DisplayName("保存房屋详情 - 更换承租人时无原承租人")
    void saveRoomDetail_changeTenantNoOldTenant() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_VACANT);
        dto.setChangeTenant(true);
        dto.setTenantName("");
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_VACANT);
        testBuilding = createTestBuilding();

        RoomDetailDTO beforeDetail = new RoomDetailDTO();
        beforeDetail.setRoom(testRoom);
        beforeDetail.setTenant(null);
        beforeDetail.setFamilyMembers(new ArrayList<>());
        beforeDetail.setOperationLogs(new ArrayList<>());

        RoomDetailDTO afterDetail = new RoomDetailDTO();
        afterDetail.setRoom(testRoom);
        afterDetail.setTenant(null);
        afterDetail.setFamilyMembers(new ArrayList<>());
        afterDetail.setOperationLogs(new ArrayList<>());

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("保存房屋详情 - 更换承租人时清空房屋状态")
    void saveRoomDetail_changeTenantClearRoom() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_VACANT);
        dto.setChangeTenant(true);
        dto.setTenantName("");
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        RoomDetailDTO beforeDetail = new RoomDetailDTO();
        beforeDetail.setRoom(testRoom);
        beforeDetail.setTenant(testTenant);
        beforeDetail.setFamilyMembers(new ArrayList<>());
        beforeDetail.setOperationLogs(new ArrayList<>());

        RoomDetailDTO afterDetail = new RoomDetailDTO();
        afterDetail.setRoom(testRoom);
        afterDetail.setTenant(null);
        afterDetail.setFamilyMembers(new ArrayList<>());
        afterDetail.setOperationLogs(new ArrayList<>());

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant).thenReturn(null);
        when(familyMemberMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        assertEquals(Constants.ROOM_STATUS_VACANT, testRoom.getStatus());
    }

    @Test
    @DisplayName("保存房屋详情 - 缓存写入失败时继续执行")
    void saveRoomDetail_cacheWriteFail() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_VACANT);
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        testRoom = createTestRoom();
        testBuilding = createTestBuilding();

        RoomDetailDTO beforeDetail = new RoomDetailDTO();
        beforeDetail.setRoom(testRoom);
        beforeDetail.setTenant(null);
        beforeDetail.setFamilyMembers(new ArrayList<>());
        beforeDetail.setOperationLogs(new ArrayList<>());

        RoomDetailDTO afterDetail = new RoomDetailDTO();
        afterDetail.setRoom(testRoom);
        afterDetail.setTenant(null);
        afterDetail.setFamilyMembers(new ArrayList<>());
        afterDetail.setOperationLogs(new ArrayList<>());

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("导入房源 - 空文件抛出BusinessException")
    void importRooms_emptyFile() {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        new byte[0]);

        assertThrows(Exception.class, () -> roomService.importRooms(file));
    }

    @Test
    @DisplayName("导入房源 - IO异常时抛出BusinessException")
    void importRooms_ioException() throws Exception {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getInputStream()).thenThrow(new java.io.IOException("读取失败"));

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.importRooms(file));
        assertTrue(ex.getMessage().contains("导入失败"));
    }

    @Test
    @DisplayName("导出房源 - 按楼栋ID导出")
    void exportRooms_byBuildingId() throws Exception {
        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_VACANT);
        testBuilding = createTestBuilding();
        testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");

        when(roomMapper.selectList(any())).thenReturn(Arrays.asList(testRoom));
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(communityMapper.selectById(1L)).thenReturn(testCommunity);
        when(tenantMapper.selectOne(any())).thenReturn(null);

        javax.servlet.http.HttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        assertDoesNotThrow(() -> roomService.exportRooms(null, 1L, response));
    }

    @Test
    @DisplayName("导出房源 - 按小区ID导出")
    void exportRooms_byCommunityId() throws Exception {
        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testBuilding = createTestBuilding();
        testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");
        testTenant = createTestTenant();

        when(buildingMapper.selectList(any())).thenReturn(Arrays.asList(testBuilding));
        when(roomMapper.selectList(any())).thenReturn(Arrays.asList(testRoom));
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(communityMapper.selectById(1L)).thenReturn(testCommunity);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant);

        javax.servlet.http.HttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        assertDoesNotThrow(() -> roomService.exportRooms(1L, null, response));
    }

    @Test
    @DisplayName("导出房源 - 小区下无楼栋")
    void exportRooms_noBuildings() throws Exception {
        when(buildingMapper.selectList(any())).thenReturn(new ArrayList<>());

        javax.servlet.http.HttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        assertDoesNotThrow(() -> roomService.exportRooms(999L, null, response));
    }

    @Test
    @DisplayName("导出房源 - 无过滤条件导出所有")
    void exportRooms_allRooms() throws Exception {
        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_VACANT);
        testBuilding = createTestBuilding();
        testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");

        when(roomMapper.selectList(any())).thenReturn(Arrays.asList(testRoom));
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(communityMapper.selectById(1L)).thenReturn(testCommunity);
        when(tenantMapper.selectOne(any())).thenReturn(null);

        javax.servlet.http.HttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        assertDoesNotThrow(() -> roomService.exportRooms(null, null, response));
    }

    @Test
    @DisplayName("导出房源 - IO异常时抛出BusinessException")
    void exportRooms_ioException() throws Exception {
        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_VACANT);
        testBuilding = createTestBuilding();
        testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("阳光花园");

        when(roomMapper.selectList(any())).thenReturn(Arrays.asList(testRoom));
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(communityMapper.selectById(1L)).thenReturn(testCommunity);
        when(tenantMapper.selectOne(any())).thenReturn(null);

        javax.servlet.http.HttpServletResponse response = mock(javax.servlet.http.HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new java.io.IOException("写入失败"));

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.exportRooms(null, 1L, response));
        assertTrue(ex.getMessage().contains("导出失败"));
    }

    @Test
    @DisplayName("保存房屋详情 - 含家庭成员")
    void saveRoomDetail_withFamilyMembers() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        dto.setCheckInTime(LocalDateTime.now());
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        RoomSaveDTO.FamilyMemberDTO memberDTO = new RoomSaveDTO.FamilyMemberDTO();
        memberDTO.setId(1L);
        memberDTO.setName("李四");
        memberDTO.setIdCard("110101199002022345");
        memberDTO.setRelationship("配偶");
        dto.setFamilyMembers(Arrays.asList(memberDTO));

        testRoom = createTestRoom();
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        FamilyMember existingMember = new FamilyMember();
        existingMember.setId(1L);
        existingMember.setTenantId(1L);
        existingMember.setName("王五");
        existingMember.setStatus(1);

        Tenant newTenant = new Tenant();
        newTenant.setId(1L);
        newTenant.setName("张三");
        newTenant.setStatus(1);
        newTenant.setRoomId(1L);

        RoomDetailDTO beforeDetail = new RoomDetailDTO();
        beforeDetail.setRoom(testRoom);
        beforeDetail.setTenant(testTenant);
        beforeDetail.setFamilyMembers(new ArrayList<>());
        beforeDetail.setOperationLogs(new ArrayList<>());

        RoomDetailDTO afterDetail = new RoomDetailDTO();
        afterDetail.setRoom(testRoom);
        afterDetail.setTenant(newTenant);
        afterDetail.setFamilyMembers(new ArrayList<>());
        afterDetail.setOperationLogs(new ArrayList<>());

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant).thenReturn(newTenant);
        when(familyMemberMapper.selectList(any())).thenReturn(Arrays.asList(existingMember));
        when(familyMemberMapper.selectById(1L)).thenReturn(existingMember);
        when(familyMemberMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
    }

    @Test
    @DisplayName("导入房源 - 有效Excel文件")
    void importRooms_validExcel() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(baos, RoomImportDTO.class).sheet("房源数据").doWrite(createImportData());
        byte[] excelBytes = baos.toByteArray();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "import.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        Community existingCommunity = new Community();
        existingCommunity.setId(1L);
        existingCommunity.setName("阳光花园");
        existingCommunity.setStatus(1);

        Building existingBuilding = new Building();
        existingBuilding.setId(1L);
        existingBuilding.setCommunityId(1L);
        existingBuilding.setName("1栋");
        existingBuilding.setStatus(1);

        when(communityMapper.selectOne(any())).thenReturn(existingCommunity);
        when(buildingMapper.selectOne(any())).thenReturn(existingBuilding);
        when(roomMapper.selectOne(any())).thenReturn(null);
        when(communityMapper.insert(any())).thenReturn(1);
        when(buildingMapper.insert(any())).thenReturn(1);
        when(roomMapper.insert(any())).thenReturn(1);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(tenantMapper.insert(any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roomService.importRooms(file));
        verify(redisTemplate).delete(Constants.CACHE_COMMUNITY_TREE);
    }

    @Test
    @DisplayName("导入房源 - 已存在房屋时更新")
    void importRooms_existingRoom() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(baos, RoomImportDTO.class).sheet("房源数据").doWrite(createImportData());
        byte[] excelBytes = baos.toByteArray();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "import.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        Community existingCommunity = new Community();
        existingCommunity.setId(1L);
        existingCommunity.setName("阳光花园");
        existingCommunity.setStatus(1);

        Building existingBuilding = new Building();
        existingBuilding.setId(1L);
        existingBuilding.setCommunityId(1L);
        existingBuilding.setName("1栋");
        existingBuilding.setStatus(1);

        Room existingRoom = createTestRoom();

        when(communityMapper.selectOne(any())).thenReturn(existingCommunity);
        when(buildingMapper.selectOne(any())).thenReturn(existingBuilding);
        when(roomMapper.selectOne(any())).thenReturn(existingRoom);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(tenantMapper.insert(any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roomService.importRooms(file));
        verify(roomMapper).updateById(any(Room.class));
    }

    @Test
    @DisplayName("导入房源 - 已有承租人时更新")
    void importRooms_existingTenant() throws Exception {
        List<RoomImportDTO> importData = new ArrayList<>();
        RoomImportDTO dto = new RoomImportDTO();
        dto.setCommunityName("阳光花园");
        dto.setBuildingName("1栋");
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));
        dto.setStatusText("已出租");
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        importData.add(dto);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(baos, RoomImportDTO.class).sheet("房源数据").doWrite(importData);
        byte[] excelBytes = baos.toByteArray();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "import.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        Community existingCommunity = new Community();
        existingCommunity.setId(1L);
        existingCommunity.setName("阳光花园");
        existingCommunity.setStatus(1);

        Building existingBuilding = new Building();
        existingBuilding.setId(1L);
        existingBuilding.setCommunityId(1L);
        existingBuilding.setName("1栋");
        existingBuilding.setStatus(1);

        Room existingRoom = createTestRoom();
        existingRoom.setStatus(1);

        Tenant existingTenant = createTestTenant();

        when(communityMapper.selectOne(any())).thenReturn(existingCommunity);
        when(buildingMapper.selectOne(any())).thenReturn(existingBuilding);
        when(roomMapper.selectOne(any())).thenReturn(existingRoom);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(tenantMapper.selectOne(any())).thenReturn(existingTenant);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roomService.importRooms(file));
        verify(tenantMapper).updateById(any(Tenant.class));
    }

    @Test
    @DisplayName("导入房源 - 空白承租人身份证和电话使用默认值")
    void importRooms_blankIdCardAndPhone() throws Exception {
        List<RoomImportDTO> importData = new ArrayList<>();
        RoomImportDTO dto = new RoomImportDTO();
        dto.setCommunityName("阳光花园");
        dto.setBuildingName("1栋");
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));
        dto.setStatusText("已出租");
        dto.setTenantName("张三");
        dto.setIdCard("");
        dto.setPhone("");
        importData.add(dto);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(baos, RoomImportDTO.class).sheet("房源数据").doWrite(importData);
        byte[] excelBytes = baos.toByteArray();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "import.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        Community existingCommunity = new Community();
        existingCommunity.setId(1L);
        existingCommunity.setName("阳光花园");
        existingCommunity.setStatus(1);

        Building existingBuilding = new Building();
        existingBuilding.setId(1L);
        existingBuilding.setCommunityId(1L);
        existingBuilding.setName("1栋");
        existingBuilding.setStatus(1);

        when(communityMapper.selectOne(any())).thenReturn(existingCommunity);
        when(buildingMapper.selectOne(any())).thenReturn(existingBuilding);
        when(roomMapper.selectOne(any())).thenReturn(null);
        when(roomMapper.insert(any())).thenReturn(1);
        when(tenantMapper.selectOne(any())).thenReturn(null);
        when(tenantMapper.insert(any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roomService.importRooms(file));
    }

    @Test
    @DisplayName("导入房源 - 新建小区和楼栋")
    void importRooms_newCommunityAndBuilding() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(baos, RoomImportDTO.class).sheet("房源数据").doWrite(createImportData());
        byte[] excelBytes = baos.toByteArray();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "import.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        when(communityMapper.selectOne(any())).thenReturn(null);
        when(communityMapper.insert(any())).thenReturn(1);
        when(buildingMapper.selectOne(any())).thenReturn(null);
        when(buildingMapper.insert(any())).thenReturn(1);
        when(roomMapper.selectOne(any())).thenReturn(null);
        when(roomMapper.insert(any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roomService.importRooms(file));
        verify(communityMapper).insert(any(Community.class));
        verify(buildingMapper).insert(any(Building.class));
    }

    @Test
    @DisplayName("获取小区统计 - 缓存未命中且数据库返回null")
    void getCommunityStats_nullFromDb() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "community:1")).thenReturn(null);
        when(roomMapper.getCommunityStats(1L)).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRentedCount());
        assertEquals(0, result.getVacantCount());
    }

    @Test
    @DisplayName("获取小区统计 - 缓存解析失败")
    void getCommunityStats_cacheParseFail() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "community:1")).thenReturn("bad-json");
        when(objectMapper.readValue(eq("bad-json"), eq(RoomStatsDTO.class))).thenThrow(new RuntimeException("parse error"));

        RoomStatsDTO stats = new RoomStatsDTO();
        stats.setTotalCount(5);
        stats.setRentedCount(3);
        stats.setVacantCount(2);
        when(roomMapper.getCommunityStats(1L)).thenReturn(stats);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RoomStatsDTO result = roomService.getCommunityStats(1L);

        assertNotNull(result);
        assertEquals(5, result.getTotalCount());
    }

    @Test
    @DisplayName("保存房屋详情 - 家庭成员新增和删除")
    void saveRoomDetail_familyMembersAddAndDelete() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(Constants.ROOM_STATUS_RENTED);
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138001");
        dto.setCheckInTime(LocalDateTime.now());
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));

        RoomSaveDTO.FamilyMemberDTO newMemberDTO = new RoomSaveDTO.FamilyMemberDTO();
        newMemberDTO.setId(null);
        newMemberDTO.setName("李四");
        newMemberDTO.setIdCard("110101199002022345");
        newMemberDTO.setRelationship("配偶");
        dto.setFamilyMembers(Arrays.asList(newMemberDTO));

        testRoom = createTestRoom();
        testRoom.setStatus(Constants.ROOM_STATUS_RENTED);
        testTenant = createTestTenant();
        testBuilding = createTestBuilding();

        FamilyMember oldMember = new FamilyMember();
        oldMember.setId(2L);
        oldMember.setTenantId(1L);
        oldMember.setName("王五");
        oldMember.setStatus(1);

        Tenant newTenant = new Tenant();
        newTenant.setId(1L);
        newTenant.setName("张三");
        newTenant.setStatus(1);
        newTenant.setRoomId(1L);

        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(tenantMapper.selectOne(any())).thenReturn(testTenant).thenReturn(newTenant);
        when(familyMemberMapper.selectList(any())).thenReturn(Arrays.asList(oldMember));
        when(familyMemberMapper.selectById(2L)).thenReturn(oldMember);
        when(familyMemberMapper.updateById(any())).thenReturn(1);
        when(familyMemberMapper.insert(any())).thenReturn(1);
        when(roomOperationLogMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(buildingMapper.selectById(1L)).thenReturn(testBuilding);
        when(tenantMapper.updateById(any())).thenReturn(1);
        when(roomMapper.updateById(any())).thenReturn(1);
        when(roomOperationLogMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> roomService.saveRoomDetail(dto, 1L, "admin"));
        verify(familyMemberMapper).insert(any(FamilyMember.class));
        verify(familyMemberMapper).updateById(any(FamilyMember.class));
    }

    @Test
    @DisplayName("获取楼栋统计 - 缓存写入失败")
    void getBuildingStats_cacheWriteFail() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.CACHE_ROOM_STATS + "building:1")).thenReturn(null);
        when(roomMapper.getBuildingStats(1L)).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("write error"));

        RoomStatsDTO result = roomService.getBuildingStats(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
    }

    private List<RoomImportDTO> createImportData() {
        List<RoomImportDTO> list = new ArrayList<>();
        RoomImportDTO dto = new RoomImportDTO();
        dto.setCommunityName("阳光花园");
        dto.setBuildingName("1栋");
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setArea(new BigDecimal("65.5"));
        dto.setRent(new BigDecimal("800"));
        dto.setStatusText("空置");
        dto.setRemark("测试");
        list.add(dto);
        return list;
    }
}
