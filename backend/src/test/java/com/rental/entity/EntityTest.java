package com.rental.entity;

import com.rental.common.Constants;
import com.rental.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("实体和DTO测试")
class EntityTest {

    @Test
    @DisplayName("测试 RoomImportDTO 实体")
    void testRoomImportDTO() {
        RoomImportDTO dto = new RoomImportDTO();
        dto.setCommunityName("测试小区");
        dto.setBuildingName("1号楼");
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setArea(new BigDecimal("50.0"));
        dto.setRent(new BigDecimal("1500.0"));
        dto.setStatusText("已出租");
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138000");
        dto.setRemark("测试备注");

        assertEquals("测试小区", dto.getCommunityName());
        assertEquals("1号楼", dto.getBuildingName());
        assertEquals("101", dto.getRoomNumber());
        assertEquals(Integer.valueOf(1), dto.getFloor());
        assertEquals(new BigDecimal("50.0"), dto.getArea());
        assertEquals(new BigDecimal("1500.0"), dto.getRent());
        assertEquals("已出租", dto.getStatusText());
        assertEquals("张三", dto.getTenantName());
        assertEquals("110101199001011234", dto.getIdCard());
        assertEquals("13800138000", dto.getPhone());
        assertEquals("测试备注", dto.getRemark());
    }

    @Test
    @DisplayName("测试 RoomImportDTO 空值")
    void testRoomImportDTONullValues() {
        RoomImportDTO dto = new RoomImportDTO();

        assertNull(dto.getCommunityName());
        assertNull(dto.getBuildingName());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getFloor());
        assertNull(dto.getArea());
        assertNull(dto.getRent());
        assertNull(dto.getStatusText());
        assertNull(dto.getTenantName());
        assertNull(dto.getIdCard());
        assertNull(dto.getPhone());
        assertNull(dto.getRemark());
    }

    @Test
    @DisplayName("测试 RoomExportDTO 实体")
    void testRoomExportDTO() {
        RoomExportDTO dto = new RoomExportDTO();
        dto.setCommunityName("测试小区");
        dto.setBuildingName("1号楼");
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setArea(new BigDecimal("50.0"));
        dto.setRent(new BigDecimal("1500.0"));
        dto.setStatusText("已出租");
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138000");
        dto.setRemark("测试备注");

        assertEquals("测试小区", dto.getCommunityName());
        assertEquals("1号楼", dto.getBuildingName());
        assertEquals("101", dto.getRoomNumber());
        assertEquals(Integer.valueOf(1), dto.getFloor());
        assertEquals(new BigDecimal("50.0"), dto.getArea());
        assertEquals(new BigDecimal("1500.0"), dto.getRent());
        assertEquals("已出租", dto.getStatusText());
        assertEquals("张三", dto.getTenantName());
        assertEquals("110101199001011234", dto.getIdCard());
        assertEquals("13800138000", dto.getPhone());
        assertEquals("测试备注", dto.getRemark());
    }

    @Test
    @DisplayName("测试 RoomExportDTO 空值")
    void testRoomExportDTONullValues() {
        RoomExportDTO dto = new RoomExportDTO();

        assertNull(dto.getCommunityName());
        assertNull(dto.getBuildingName());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getFloor());
        assertNull(dto.getArea());
        assertNull(dto.getRent());
        assertNull(dto.getStatusText());
        assertNull(dto.getTenantName());
        assertNull(dto.getIdCard());
        assertNull(dto.getPhone());
        assertNull(dto.getRemark());
    }

    @Test
    @DisplayName("测试 RoomCardDTO 实体")
    void testRoomCardDTO() {
        RoomCardDTO dto = new RoomCardDTO();
        dto.setId(1L);
        dto.setRoomNumber("101");
        dto.setFloor(1);
        dto.setStatus(1);
        dto.setTenantName("张三");

        assertEquals(1L, dto.getId());
        assertEquals("101", dto.getRoomNumber());
        assertEquals(Integer.valueOf(1), dto.getFloor());
        assertEquals(Integer.valueOf(1), dto.getStatus());
        assertEquals("张三", dto.getTenantName());
    }

    @Test
    @DisplayName("测试 RoomCardDTO 空值")
    void testRoomCardDTONullValues() {
        RoomCardDTO dto = new RoomCardDTO();

        assertNull(dto.getId());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getFloor());
        assertNull(dto.getStatus());
        assertNull(dto.getTenantName());
    }

    @Test
    @DisplayName("测试 Constants 常量值")
    void testConstants() {
        assertEquals("rental:room:stats:", Constants.CACHE_ROOM_STATS);
        assertEquals("rental:community:tree", Constants.CACHE_COMMUNITY_TREE);
        assertEquals("rental:token:", Constants.CACHE_TOKEN_PREFIX);
        assertEquals(3600L, Constants.CACHE_EXPIRE_COMMUNITY);
        assertEquals(300L, Constants.CACHE_EXPIRE_STATS);
        assertEquals(0, Constants.ROOM_STATUS_VACANT);
        assertEquals(1, Constants.ROOM_STATUS_RENTED);
        assertEquals("新增", Constants.OP_TYPE_ADD);
        assertEquals("修改", Constants.OP_TYPE_UPDATE);
        assertEquals("更换承租人", Constants.OP_TYPE_CHANGE_TENANT);
        assertEquals("删除", Constants.OP_TYPE_DELETE);
        assertEquals(0, Constants.MARITAL_UNMARRIED);
        assertEquals(1, Constants.MARITAL_MARRIED);
        assertEquals(2, Constants.MARITAL_DIVORCED);
        assertEquals(3, Constants.MARITAL_WIDOWED);
        assertEquals(0, Constants.INCOME_LOW);
        assertEquals(1, Constants.INCOME_MEDIUM);
        assertEquals(2, Constants.INCOME_HIGH);
        assertEquals(0, Constants.RELOCATION_NONE);
        assertEquals(1, Constants.RELOCATION_DEMOLITION);
        assertEquals(2, Constants.RELOCATION_WAITING);
        assertEquals(0, Constants.SALE_EXCHANGE_NONE);
        assertEquals(1, Constants.SALE_EXCHANGE_SALE);
        assertEquals(2, Constants.SALE_EXCHANGE_EXCHANGE);
    }

    @Test
    @DisplayName("测试 Room 实体边界值")
    void testRoomBoundaryValues() {
        Room room = new Room();
        room.setId(1L);
        room.setBuildingId(1L);
        room.setRoomNumber("101");
        room.setFloor(1);
        room.setArea(new BigDecimal("9999.99"));
        room.setRent(new BigDecimal("99999.99"));
        room.setStatus(1);
        room.setRemark("测试备注");
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());

        assertEquals(1L, room.getId());
        assertEquals(1L, room.getBuildingId());
        assertEquals("101", room.getRoomNumber());
        assertEquals(Integer.valueOf(1), room.getFloor());
        assertEquals(new BigDecimal("9999.99"), room.getArea());
        assertEquals(new BigDecimal("99999.99"), room.getRent());
        assertEquals(Integer.valueOf(1), room.getStatus());
        assertEquals("测试备注", room.getRemark());
        assertNotNull(room.getCreateTime());
        assertNotNull(room.getUpdateTime());
    }

    @Test
    @DisplayName("测试 Room 空值")
    void testRoomNullValues() {
        Room room = new Room();

        assertNull(room.getId());
        assertNull(room.getBuildingId());
        assertNull(room.getRoomNumber());
        assertNull(room.getFloor());
        assertNull(room.getArea());
        assertNull(room.getRent());
        assertNull(room.getStatus());
        assertNull(room.getRemark());
    }

    @Test
    @DisplayName("测试 Tenant 实体边界值")
    void testTenantBoundaryValues() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setRoomId(1L);
        tenant.setName("张三");
        tenant.setIdCard("110101199001011234");
        tenant.setPhone("13800138000");
        tenant.setMaritalStatus(1);
        tenant.setFamilySize(3);
        tenant.setIncomeStatus(1);
        tenant.setCommunityBelong("测试社区");
        tenant.setCheckInTime(LocalDateTime.now());
        tenant.setIsDisabled(0);
        tenant.setRelocationType(1);
        tenant.setSaleExchange(0);
        tenant.setStatus(1);
        tenant.setRemark("测试备注");
        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(LocalDateTime.now());

        assertEquals(1L, tenant.getId());
        assertEquals(1L, tenant.getRoomId());
        assertEquals("张三", tenant.getName());
        assertEquals("110101199001011234", tenant.getIdCard());
        assertEquals("13800138000", tenant.getPhone());
        assertEquals(Integer.valueOf(1), tenant.getMaritalStatus());
        assertEquals(Integer.valueOf(3), tenant.getFamilySize());
        assertEquals(Integer.valueOf(1), tenant.getIncomeStatus());
        assertEquals("测试社区", tenant.getCommunityBelong());
        assertNotNull(tenant.getCheckInTime());
        assertEquals(Integer.valueOf(0), tenant.getIsDisabled());
        assertEquals(Integer.valueOf(1), tenant.getRelocationType());
        assertEquals(Integer.valueOf(0), tenant.getSaleExchange());
        assertEquals(Integer.valueOf(1), tenant.getStatus());
        assertEquals("测试备注", tenant.getRemark());
        assertNotNull(tenant.getCreateTime());
        assertNotNull(tenant.getUpdateTime());
    }

    @Test
    @DisplayName("测试 Tenant 空值")
    void testTenantNullValues() {
        Tenant tenant = new Tenant();

        assertNull(tenant.getId());
        assertNull(tenant.getRoomId());
        assertNull(tenant.getName());
        assertNull(tenant.getIdCard());
        assertNull(tenant.getPhone());
        assertNull(tenant.getMaritalStatus());
        assertNull(tenant.getFamilySize());
        assertNull(tenant.getIncomeStatus());
        assertNull(tenant.getCommunityBelong());
        assertNull(tenant.getCheckInTime());
        assertNull(tenant.getIsDisabled());
        assertNull(tenant.getRelocationType());
        assertNull(tenant.getSaleExchange());
        assertNull(tenant.getStatus());
        assertNull(tenant.getRemark());
    }

    @Test
    @DisplayName("测试 FamilyMember 实体")
    void testFamilyMember() {
        FamilyMember member = new FamilyMember();
        member.setId(1L);
        member.setTenantId(1L);
        member.setName("李四");
        member.setIdCard("110101199001011235");
        member.setRelationship("配偶");
        member.setStatus(1);
        member.setCreateTime(LocalDateTime.now());

        assertEquals(1L, member.getId());
        assertEquals(1L, member.getTenantId());
        assertEquals("李四", member.getName());
        assertEquals("110101199001011235", member.getIdCard());
        assertEquals("配偶", member.getRelationship());
        assertEquals(Integer.valueOf(1), member.getStatus());
        assertNotNull(member.getCreateTime());
    }

    @Test
    @DisplayName("测试 FamilyMember 空值")
    void testFamilyMemberNullValues() {
        FamilyMember member = new FamilyMember();

        assertNull(member.getId());
        assertNull(member.getTenantId());
        assertNull(member.getName());
        assertNull(member.getIdCard());
        assertNull(member.getRelationship());
        assertNull(member.getStatus());
    }

    @Test
    @DisplayName("测试 Community 实体")
    void testCommunity() {
        Community community = new Community();
        community.setId(1L);
        community.setName("测试小区");
        community.setAddress("测试地址");
        community.setDescription("测试描述");
        community.setStatus(1);
        community.setCreateTime(LocalDateTime.now());
        community.setUpdateTime(LocalDateTime.now());

        assertEquals(1L, community.getId());
        assertEquals("测试小区", community.getName());
        assertEquals("测试地址", community.getAddress());
        assertEquals("测试描述", community.getDescription());
        assertEquals(Integer.valueOf(1), community.getStatus());
        assertNotNull(community.getCreateTime());
        assertNotNull(community.getUpdateTime());
    }

    @Test
    @DisplayName("测试 Community 空值")
    void testCommunityNullValues() {
        Community community = new Community();

        assertNull(community.getId());
        assertNull(community.getName());
        assertNull(community.getAddress());
        assertNull(community.getDescription());
        assertNull(community.getStatus());
    }

    @Test
    @DisplayName("测试 Building 实体")
    void testBuilding() {
        Building building = new Building();
        building.setId(1L);
        building.setCommunityId(1L);
        building.setName("1号楼");
        building.setFloors(6);
        building.setUnitsPerFloor(2);
        building.setDescription("测试描述");
        building.setStatus(1);
        building.setCreateTime(LocalDateTime.now());
        building.setUpdateTime(LocalDateTime.now());

        assertEquals(1L, building.getId());
        assertEquals(1L, building.getCommunityId());
        assertEquals("1号楼", building.getName());
        assertEquals(Integer.valueOf(6), building.getFloors());
        assertEquals(Integer.valueOf(2), building.getUnitsPerFloor());
        assertEquals("测试描述", building.getDescription());
        assertEquals(Integer.valueOf(1), building.getStatus());
        assertNotNull(building.getCreateTime());
        assertNotNull(building.getUpdateTime());
    }

    @Test
    @DisplayName("测试 Building 空值")
    void testBuildingNullValues() {
        Building building = new Building();

        assertNull(building.getId());
        assertNull(building.getCommunityId());
        assertNull(building.getName());
        assertNull(building.getFloors());
        assertNull(building.getUnitsPerFloor());
        assertNull(building.getDescription());
        assertNull(building.getStatus());
    }

    @Test
    @DisplayName("测试 RoomOperationLog 实体")
    void testRoomOperationLog() {
        RoomOperationLog log = new RoomOperationLog();
        log.setId(1L);
        log.setRoomId(1L);
        log.setOperatorId(1L);
        log.setOperatorName("管理员");
        log.setOperationType("新增");
        log.setDescription("新增承租人");
        log.setBeforeData("{}");
        log.setAfterData("{}");
        log.setOperationTime(LocalDateTime.now());

        assertEquals(1L, log.getId());
        assertEquals(1L, log.getRoomId());
        assertEquals(1L, log.getOperatorId());
        assertEquals("管理员", log.getOperatorName());
        assertEquals("新增", log.getOperationType());
        assertEquals("新增承租人", log.getDescription());
        assertEquals("{}", log.getBeforeData());
        assertEquals("{}", log.getAfterData());
        assertNotNull(log.getOperationTime());
    }

    @Test
    @DisplayName("测试 RoomOperationLog 空值")
    void testRoomOperationLogNullValues() {
        RoomOperationLog log = new RoomOperationLog();

        assertNull(log.getId());
        assertNull(log.getRoomId());
        assertNull(log.getOperatorId());
        assertNull(log.getOperatorName());
        assertNull(log.getOperationType());
        assertNull(log.getDescription());
        assertNull(log.getBeforeData());
        assertNull(log.getAfterData());
        assertNull(log.getOperationTime());
    }
}
