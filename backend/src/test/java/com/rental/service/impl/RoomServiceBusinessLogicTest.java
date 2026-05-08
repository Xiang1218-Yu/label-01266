package com.rental.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.dto.RoomDetailDTO;
import com.rental.dto.RoomSaveDTO;
import com.rental.entity.Tenant;
import com.rental.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoomServiceImpl核心业务逻辑测试")
class RoomServiceBusinessLogicTest {

    private RoomServiceImpl roomService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        roomService = new RoomServiceImpl(null, null, null, null, null, null, null);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("测试buildChangeTenantDesc - 原承租人为null")
    void testBuildChangeTenantDescWithNullTenant() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod("buildChangeTenantDesc", Tenant.class);
        method.setAccessible(true);

        String result = (String) method.invoke(roomService, (Tenant) null);

        assertEquals("更换承租人(原无承租人)", result);
    }

    @Test
    @DisplayName("测试buildChangeTenantDesc - 有原承租人")
    void testBuildChangeTenantDescWithTenant() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod("buildChangeTenantDesc", Tenant.class);
        method.setAccessible(true);

        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setIdCard("110101199001011234");
        tenant.setPhone("13800138000");

        String result = (String) method.invoke(roomService, tenant);

        assertTrue(result.contains("张三"));
        assertTrue(result.contains("110101199001011234"));
        assertTrue(result.contains("13800138000"));
        assertTrue(result.contains("更换承租人"));
    }

    @Test
    @DisplayName("测试buildChangeTenantDesc - 承租人信息不完整")
    void testBuildChangeTenantDescWithPartialInfo() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod("buildChangeTenantDesc", Tenant.class);
        method.setAccessible(true);

        Tenant tenant = new Tenant();
        tenant.setName("李四");
        tenant.setIdCard(null);
        tenant.setPhone(null);

        String result = (String) method.invoke(roomService, tenant);

        assertTrue(result.contains("李四"));
        assertTrue(result.contains("更换承租人"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 无任何变更")
    void testBuildUpdateDescNoChanges() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Room room = new Room();
        room.setArea(new java.math.BigDecimal("50.0"));
        room.setRent(new java.math.BigDecimal("1500.0"));
        before.setRoom(room);

        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setPhone("13800138000");
        before.setTenant(tenant);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("50.0"));
        after.setRent(new java.math.BigDecimal("1500.0"));
        after.setTenantName("张三");
        after.setPhone("13800138000");

        String result = (String) method.invoke(roomService, before, after);

        assertEquals("修改房屋/承租人信息", result);
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 面积变更")
    void testBuildUpdateDescAreaChange() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Room room = new Room();
        room.setArea(new java.math.BigDecimal("50.0"));
        room.setRent(new java.math.BigDecimal("1500.0"));
        before.setRoom(room);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("60.0"));
        after.setRent(new java.math.BigDecimal("1500.0"));

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("面积"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 租金变更")
    void testBuildUpdateDescRentChange() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Room room = new Room();
        room.setArea(new java.math.BigDecimal("50.0"));
        room.setRent(new java.math.BigDecimal("1500.0"));
        before.setRoom(room);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("50.0"));
        after.setRent(new java.math.BigDecimal("1800.0"));

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("租金"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 承租人姓名变更")
    void testBuildUpdateDescTenantNameChange() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setPhone("13800138000");
        before.setTenant(tenant);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setTenantName("李四");
        after.setPhone("13800138000");

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("承租人姓名"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 联系方式变更")
    void testBuildUpdateDescPhoneChange() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setPhone("13800138000");
        before.setTenant(tenant);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setTenantName("张三");
        after.setPhone("13900139000");

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("联系方式"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - 多个变更合并")
    void testBuildUpdateDescMultipleChanges() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Room room = new Room();
        room.setArea(new java.math.BigDecimal("50.0"));
        room.setRent(new java.math.BigDecimal("1500.0"));
        before.setRoom(room);

        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setPhone("13800138000");
        before.setTenant(tenant);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("60.0"));
        after.setRent(new java.math.BigDecimal("1800.0"));
        after.setTenantName("李四");
        after.setPhone("13900139000");

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("面积"));
        assertTrue(result.contains("租金"));
        assertTrue(result.contains("承租人姓名"));
        assertTrue(result.contains("联系方式"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - Room为null")
    void testBuildUpdateDescRoomNull() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        before.setRoom(null);

        Tenant tenant = new Tenant();
        tenant.setName("张三");
        tenant.setPhone("13800138000");
        before.setTenant(tenant);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("60.0"));
        after.setTenantName("李四");

        String result = (String) method.invoke(roomService, before, after);

        assertFalse(result.contains("面积"));
        assertTrue(result.contains("承租人姓名"));
    }

    @Test
    @DisplayName("测试buildUpdateDesc - Tenant为null")
    void testBuildUpdateDescTenantNull() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod(
                "buildUpdateDesc", RoomDetailDTO.class, RoomSaveDTO.class);
        method.setAccessible(true);

        RoomDetailDTO before = new RoomDetailDTO();
        Room room = new Room();
        room.setArea(new java.math.BigDecimal("50.0"));
        room.setRent(new java.math.BigDecimal("1500.0"));
        before.setRoom(room);
        before.setTenant(null);

        RoomSaveDTO after = new RoomSaveDTO();
        after.setArea(new java.math.BigDecimal("60.0"));
        after.setTenantName("李四");

        String result = (String) method.invoke(roomService, before, after);

        assertTrue(result.contains("面积"));
        assertFalse(result.contains("承租人姓名"));
    }

    @Test
    @DisplayName("测试toJson - 正常对象")
    void testToJsonNormalObject() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod("toJson", Object.class);
        method.setAccessible(true);

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");

        String result = (String) method.invoke(roomService, room);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    @DisplayName("测试toJson - null对象")
    void testToJsonNullObject() throws Exception {
        Method method = RoomServiceImpl.class.getDeclaredMethod("toJson", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(roomService, (Object) null);

        assertEquals("{}", result);
    }

    @Test
    @DisplayName("测试RoomSaveDTO实体")
    void testRoomSaveDTO() {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setRoomId(1L);
        dto.setStatus(1);
        dto.setArea(new java.math.BigDecimal("50.0"));
        dto.setRent(new java.math.BigDecimal("1500.0"));
        dto.setRoomRemark("测试备注");
        dto.setTenantName("张三");
        dto.setIdCard("110101199001011234");
        dto.setPhone("13800138000");
        dto.setCheckInTime(java.time.LocalDateTime.now());
        dto.setChangeTenant(false);

        assertEquals(1L, dto.getRoomId());
        assertEquals(Integer.valueOf(1), dto.getStatus());
        assertEquals(new java.math.BigDecimal("50.0"), dto.getArea());
        assertEquals(new java.math.BigDecimal("1500.0"), dto.getRent());
        assertEquals("测试备注", dto.getRoomRemark());
        assertEquals("张三", dto.getTenantName());
        assertEquals("110101199001011234", dto.getIdCard());
        assertEquals("13800138000", dto.getPhone());
        assertNotNull(dto.getCheckInTime());
        assertEquals(Boolean.FALSE, dto.getChangeTenant());
    }

    @Test
    @DisplayName("测试RoomSaveDTO空值")
    void testRoomSaveDTONullValues() {
        RoomSaveDTO dto = new RoomSaveDTO();

        assertNull(dto.getRoomId());
        assertNull(dto.getStatus());
        assertNull(dto.getArea());
        assertNull(dto.getRent());
        assertNull(dto.getRoomRemark());
        assertNull(dto.getTenantName());
        assertNull(dto.getIdCard());
        assertNull(dto.getPhone());
        assertNull(dto.getCheckInTime());
        assertNull(dto.getChangeTenant());
    }

    @Test
    @DisplayName("测试RoomSaveDTO.FamilyMemberDTO")
    void testRoomSaveDTOFamilyMemberDTO() {
        RoomSaveDTO.FamilyMemberDTO member = new RoomSaveDTO.FamilyMemberDTO();
        member.setId(1L);
        member.setName("家庭成员1");
        member.setIdCard("110101199001011235");
        member.setRelationship("配偶");

        assertEquals(1L, member.getId());
        assertEquals("家庭成员1", member.getName());
        assertEquals("110101199001011235", member.getIdCard());
        assertEquals("配偶", member.getRelationship());
    }

    @Test
    @DisplayName("测试RoomSaveDTO.FamilyMemberDTO空值")
    void testRoomSaveDTOFamilyMemberDTONullValues() {
        RoomSaveDTO.FamilyMemberDTO member = new RoomSaveDTO.FamilyMemberDTO();

        assertNull(member.getId());
        assertNull(member.getName());
        assertNull(member.getIdCard());
        assertNull(member.getRelationship());
    }

    @Test
    @DisplayName("测试RoomDetailDTO实体")
    void testRoomDetailDTO() {
        RoomDetailDTO detail = new RoomDetailDTO();

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        detail.setRoom(room);

        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("张三");
        detail.setTenant(tenant);

        detail.setFamilyMembers(Collections.emptyList());
        detail.setOperationLogs(Collections.emptyList());

        assertNotNull(detail.getRoom());
        assertNotNull(detail.getTenant());
        assertNotNull(detail.getFamilyMembers());
        assertNotNull(detail.getOperationLogs());
        assertEquals(0, detail.getFamilyMembers().size());
        assertEquals(0, detail.getOperationLogs().size());
    }

    @Test
    @DisplayName("测试RoomDetailDTO空值")
    void testRoomDetailDTONullValues() {
        RoomDetailDTO detail = new RoomDetailDTO();

        assertNull(detail.getRoom());
        assertNull(detail.getTenant());
        assertNull(detail.getFamilyMembers());
        assertNull(detail.getOperationLogs());
    }
}
