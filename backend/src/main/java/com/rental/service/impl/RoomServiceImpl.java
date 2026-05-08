package com.rental.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.BusinessException;
import com.rental.common.Constants;
import com.rental.dto.*;
import com.rental.entity.*;
import com.rental.mapper.*;
import com.rental.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 房屋服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final TenantMapper tenantMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final RoomOperationLogMapper roomOperationLogMapper;
    private final CommunityMapper communityMapper;
    private final BuildingMapper buildingMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public RoomStatsDTO getBuildingStats(Long buildingId) {
        String cacheKey = Constants.CACHE_ROOM_STATS + "building:" + buildingId;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);

        if (StrUtil.isNotBlank(cacheData)) {
            try {
                return objectMapper.readValue(cacheData, RoomStatsDTO.class);
            } catch (Exception e) {
                log.warn("解析统计缓存失败", e);
            }
        }

        RoomStatsDTO stats = baseMapper.getBuildingStats(buildingId);
        if (stats == null) {
            stats = new RoomStatsDTO();
            stats.setTotalCount(0);
            stats.setRentedCount(0);
            stats.setVacantCount(0);
        }

        try {
            String json = objectMapper.writeValueAsString(stats);
            redisTemplate.opsForValue().set(cacheKey, json, Constants.CACHE_EXPIRE_STATS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存统计数据失败", e);
        }

        return stats;
    }

    @Override
    public RoomStatsDTO getCommunityStats(Long communityId) {
        String cacheKey = Constants.CACHE_ROOM_STATS + "community:" + communityId;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);

        if (StrUtil.isNotBlank(cacheData)) {
            try {
                return objectMapper.readValue(cacheData, RoomStatsDTO.class);
            } catch (Exception e) {
                log.warn("解析统计缓存失败", e);
            }
        }

        RoomStatsDTO stats = baseMapper.getCommunityStats(communityId);
        if (stats == null) {
            stats = new RoomStatsDTO();
            stats.setTotalCount(0);
            stats.setRentedCount(0);
            stats.setVacantCount(0);
        }

        try {
            String json = objectMapper.writeValueAsString(stats);
            redisTemplate.opsForValue().set(cacheKey, json, Constants.CACHE_EXPIRE_STATS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存统计数据失败", e);
        }

        return stats;
    }

    @Override
    public List<RoomCardDTO> getRoomCards(Long buildingId, Integer floor, Integer status) {
        return baseMapper.getRoomCards(buildingId, floor, status);
    }

    @Override
    public RoomDetailDTO getRoomDetail(Long roomId) {
        Room room = getById(roomId);
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        RoomDetailDTO detail = new RoomDetailDTO();
        detail.setRoom(room);

        // 查询承租人
        Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getRoomId, roomId)
                .eq(Tenant::getStatus, 1));
        detail.setTenant(tenant);

        // 查询家庭成员
        if (tenant != null) {
            List<FamilyMember> members = familyMemberMapper.selectList(
                    new LambdaQueryWrapper<FamilyMember>()
                            .eq(FamilyMember::getTenantId, tenant.getId())
                            .eq(FamilyMember::getStatus, 1));
            detail.setFamilyMembers(members);
        } else {
            detail.setFamilyMembers(new ArrayList<>());
        }

        // 查询操作记录
        List<RoomOperationLog> logs = roomOperationLogMapper.selectList(
                new LambdaQueryWrapper<RoomOperationLog>()
                        .eq(RoomOperationLog::getRoomId, roomId)
                        .orderByDesc(RoomOperationLog::getOperationTime));
        detail.setOperationLogs(logs);

        return detail;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoomDetail(RoomSaveDTO dto, Long operatorId, String operatorName) {
        Room room = getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 已出租状态下，承租人信息必填校验
        if (dto.getStatus() != null && dto.getStatus() == Constants.ROOM_STATUS_RENTED) {
            if (StrUtil.isBlank(dto.getTenantName())) {
                throw new BusinessException("已出租状态下，承租人姓名不能为空");
            }
            if (StrUtil.isBlank(dto.getIdCard())) {
                throw new BusinessException("已出租状态下，承租人身份证号不能为空");
            }
            if (StrUtil.isBlank(dto.getPhone())) {
                throw new BusinessException("已出租状态下，承租人联系方式不能为空");
            }
            if (dto.getCheckInTime() == null) {
                throw new BusinessException("已出租状态下，入住时间不能为空");
            }
        }

        // 记录修改前数据
        RoomDetailDTO beforeDetail = getRoomDetail(dto.getRoomId());
        String beforeData = toJson(beforeDetail);

        // 判断操作类型
        String operationType;
        String description;

        if (Boolean.TRUE.equals(dto.getChangeTenant())) {
            // 更换承租人
            operationType = Constants.OP_TYPE_CHANGE_TENANT;
            description = buildChangeTenantDesc(beforeDetail.getTenant());
            
            // 将原承租人状态置为无效
            if (beforeDetail.getTenant() != null) {
                Tenant oldTenant = beforeDetail.getTenant();
                oldTenant.setStatus(0);
                oldTenant.setRoomId(null);
                tenantMapper.updateById(oldTenant);
            }
            
            // 创建新承租人
            if (StrUtil.isNotBlank(dto.getTenantName())) {
                saveTenant(dto, room.getId(), true);
            } else {
                // 清空房屋状态为空置
                room.setStatus(Constants.ROOM_STATUS_VACANT);
            }
        } else if (beforeDetail.getTenant() == null && StrUtil.isNotBlank(dto.getTenantName())) {
            // 新增承租人
            operationType = Constants.OP_TYPE_ADD;
            description = "新增承租人: " + dto.getTenantName();
            saveTenant(dto, room.getId(), true);
            room.setStatus(Constants.ROOM_STATUS_RENTED);
        } else if (dto.getStatus() != null && dto.getStatus() == Constants.ROOM_STATUS_VACANT && beforeDetail.getTenant() != null) {
            // 从已出租改为空置，清除承租人
            operationType = Constants.OP_TYPE_UPDATE;
            description = "房屋状态改为空置，清除承租人: " + beforeDetail.getTenant().getName();
            Tenant oldTenant = beforeDetail.getTenant();
            oldTenant.setStatus(0);
            oldTenant.setRoomId(null);
            tenantMapper.updateById(oldTenant);
        } else {
            // 修改
            operationType = Constants.OP_TYPE_UPDATE;
            description = buildUpdateDesc(beforeDetail, dto);
            if (beforeDetail.getTenant() != null && dto.getStatus() == Constants.ROOM_STATUS_RENTED) {
                saveTenant(dto, room.getId(), false);
            }
        }

        // 更新房屋信息
        room.setArea(dto.getArea());
        room.setRent(dto.getRent());
        room.setRemark(dto.getRoomRemark());
        if (dto.getStatus() != null) {
            room.setStatus(dto.getStatus());
        }
        updateById(room);

        // 记录修改后数据
        RoomDetailDTO afterDetail = getRoomDetail(dto.getRoomId());
        String afterData = toJson(afterDetail);

        // 保存操作记录
        RoomOperationLog opLog = new RoomOperationLog();
        opLog.setRoomId(dto.getRoomId());
        opLog.setTenantId(afterDetail.getTenant() != null ? afterDetail.getTenant().getId() : null);
        opLog.setOperatorId(operatorId);
        opLog.setOperatorName(operatorName);
        opLog.setOperationType(operationType);
        opLog.setDescription(description);
        opLog.setBeforeData(beforeData);
        opLog.setAfterData(afterData);
        opLog.setOperationTime(LocalDateTime.now());
        roomOperationLogMapper.insert(opLog);

        // 清除统计缓存
        clearStatsCache(room.getBuildingId());

        log.info("保存房屋详情成功: roomId={}, operationType={}", dto.getRoomId(), operationType);
    }

    private void saveTenant(RoomSaveDTO dto, Long roomId, boolean isNew) {
        Tenant tenant;
        if (isNew) {
            tenant = new Tenant();
            tenant.setRoomId(roomId);
            tenant.setStatus(1);
        } else {
            tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                    .eq(Tenant::getRoomId, roomId)
                    .eq(Tenant::getStatus, 1));
            if (tenant == null) {
                tenant = new Tenant();
                tenant.setRoomId(roomId);
                tenant.setStatus(1);
                isNew = true;
            }
        }

        tenant.setName(dto.getTenantName());
        tenant.setIdCard(dto.getIdCard());
        tenant.setMaritalStatus(dto.getMaritalStatus());
        tenant.setFamilySize(dto.getFamilySize());
        tenant.setPhone(dto.getPhone());
        tenant.setIncomeStatus(dto.getIncomeStatus());
        tenant.setCommunityBelong(dto.getCommunityBelong());
        tenant.setCheckInTime(dto.getCheckInTime());
        tenant.setIsDisabled(dto.getIsDisabled());
        tenant.setRelocationType(dto.getRelocationType());
        tenant.setSaleExchange(dto.getSaleExchange());
        tenant.setRemark(dto.getTenantRemark());

        if (isNew) {
            tenantMapper.insert(tenant);
        } else {
            tenantMapper.updateById(tenant);
        }

        // 保存家庭成员
        saveFamilyMembers(tenant.getId(), dto.getFamilyMembers());
    }

    private void saveFamilyMembers(Long tenantId, List<RoomSaveDTO.FamilyMemberDTO> members) {
        // 获取现有成员ID
        List<FamilyMember> existingMembers = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getTenantId, tenantId)
                        .eq(FamilyMember::getStatus, 1));
        Set<Long> existingIds = existingMembers.stream()
                .map(FamilyMember::getId)
                .collect(Collectors.toSet());

        Set<Long> newIds = new HashSet<>();
        if (CollUtil.isNotEmpty(members)) {
            for (RoomSaveDTO.FamilyMemberDTO memberDTO : members) {
                FamilyMember member;
                if (memberDTO.getId() != null && existingIds.contains(memberDTO.getId())) {
                    member = familyMemberMapper.selectById(memberDTO.getId());
                    newIds.add(memberDTO.getId());
                } else {
                    member = new FamilyMember();
                    member.setTenantId(tenantId);
                    member.setStatus(1);
                }
                member.setName(memberDTO.getName());
                member.setIdCard(memberDTO.getIdCard());
                member.setRelationship(memberDTO.getRelationship());

                if (member.getId() == null) {
                    familyMemberMapper.insert(member);
                } else {
                    familyMemberMapper.updateById(member);
                }
            }
        }

        // 删除不在新列表中的成员
        for (Long existingId : existingIds) {
            if (!newIds.contains(existingId)) {
                FamilyMember member = familyMemberMapper.selectById(existingId);
                member.setStatus(0);
                familyMemberMapper.updateById(member);
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRooms(MultipartFile file) {
        try {
            List<RoomImportDTO> dataList = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), RoomImportDTO.class, new ReadListener<RoomImportDTO>() {
                @Override
                public void invoke(RoomImportDTO data, AnalysisContext context) {
                    dataList.add(data);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();

            for (RoomImportDTO dto : dataList) {
                // 查找或创建小区
                Community community = communityMapper.selectOne(
                        new LambdaQueryWrapper<Community>().eq(Community::getName, dto.getCommunityName()));
                if (community == null) {
                    community = new Community();
                    community.setName(dto.getCommunityName());
                    community.setStatus(1);
                    communityMapper.insert(community);
                }

                // 查找或创建楼栋
                Building building = buildingMapper.selectOne(
                        new LambdaQueryWrapper<Building>()
                                .eq(Building::getCommunityId, community.getId())
                                .eq(Building::getName, dto.getBuildingName()));
                if (building == null) {
                    building = new Building();
                    building.setCommunityId(community.getId());
                    building.setName(dto.getBuildingName());
                    building.setStatus(1);
                    buildingMapper.insert(building);
                }

                // 查找或创建房屋
                Room room = getOne(new LambdaQueryWrapper<Room>()
                        .eq(Room::getBuildingId, building.getId())
                        .eq(Room::getRoomNumber, dto.getRoomNumber()));
                if (room == null) {
                    room = new Room();
                    room.setBuildingId(building.getId());
                    room.setRoomNumber(dto.getRoomNumber());
                }
                room.setFloor(dto.getFloor());
                room.setArea(dto.getArea());
                room.setRent(dto.getRent());
                room.setStatus("已出租".equals(dto.getStatusText()) ? 1 : 0);
                room.setRemark(dto.getRemark());

                if (room.getId() == null) {
                    save(room);
                } else {
                    updateById(room);
                }

                // 处理承租人信息
                if (StrUtil.isNotBlank(dto.getTenantName()) && room.getStatus() == 1) {
                    Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                            .eq(Tenant::getRoomId, room.getId())
                            .eq(Tenant::getStatus, 1));
                    if (tenant == null) {
                        tenant = new Tenant();
                        tenant.setRoomId(room.getId());
                        tenant.setStatus(1);
                    }
                    tenant.setName(dto.getTenantName());
                    tenant.setIdCard(StrUtil.isNotBlank(dto.getIdCard()) ? dto.getIdCard() : "000000000000000000");
                    tenant.setPhone(StrUtil.isNotBlank(dto.getPhone()) ? dto.getPhone() : "00000000000");
                    if (tenant.getCheckInTime() == null) {
                        tenant.setCheckInTime(LocalDateTime.now());
                    }
                    if (tenant.getId() == null) {
                        tenantMapper.insert(tenant);
                    } else {
                        tenantMapper.updateById(tenant);
                    }
                }
            }

            log.info("导入房源成功, 共{}条", dataList.size());
            
            // 清除小区树缓存
            redisTemplate.delete(Constants.CACHE_COMMUNITY_TREE);
        } catch (IOException e) {
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }

    @Override
    public void exportRooms(Long communityId, Long buildingId, HttpServletResponse response) {
        try {
            List<RoomExportDTO> exportList = new ArrayList<>();

            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            if (buildingId != null) {
                wrapper.eq(Room::getBuildingId, buildingId);
            } else if (communityId != null) {
                List<Building> buildings = buildingMapper.selectList(
                        new LambdaQueryWrapper<Building>().eq(Building::getCommunityId, communityId));
                List<Long> buildingIds = buildings.stream().map(Building::getId).collect(Collectors.toList());
                if (CollUtil.isEmpty(buildingIds)) {
                    buildingIds.add(-1L);
                }
                wrapper.in(Room::getBuildingId, buildingIds);
            }

            List<Room> rooms = list(wrapper);
            for (Room room : rooms) {
                RoomExportDTO dto = new RoomExportDTO();
                
                Building building = buildingMapper.selectById(room.getBuildingId());
                Community community = communityMapper.selectById(building.getCommunityId());
                
                dto.setCommunityName(community.getName());
                dto.setBuildingName(building.getName());
                dto.setRoomNumber(room.getRoomNumber());
                dto.setFloor(room.getFloor());
                dto.setArea(room.getArea());
                dto.setRent(room.getRent());
                dto.setStatusText(room.getStatus() == 1 ? "已出租" : "空置");
                dto.setRemark(room.getRemark());

                Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getRoomId, room.getId())
                        .eq(Tenant::getStatus, 1));
                if (tenant != null) {
                    dto.setTenantName(tenant.getName());
                    dto.setIdCard(tenant.getIdCard());
                    dto.setPhone(tenant.getPhone());
                }

                exportList.add(dto);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("房源数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), RoomExportDTO.class)
                    .sheet("房源数据")
                    .doWrite(exportList);

            log.info("导出房源成功, 共{}条", exportList.size());
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public List<Integer> getBuildingFloors(Long buildingId) {
        List<Room> rooms = list(new LambdaQueryWrapper<Room>()
                .eq(Room::getBuildingId, buildingId)
                .select(Room::getFloor)
                .groupBy(Room::getFloor)
                .orderByAsc(Room::getFloor));
        return rooms.stream().map(Room::getFloor).distinct().collect(Collectors.toList());
    }

    private void clearStatsCache(Long buildingId) {
        redisTemplate.delete(Constants.CACHE_ROOM_STATS + "building:" + buildingId);
        Building building = buildingMapper.selectById(buildingId);
        if (building != null) {
            redisTemplate.delete(Constants.CACHE_ROOM_STATS + "community:" + building.getCommunityId());
        }
    }

    private String buildChangeTenantDesc(Tenant oldTenant) {
        if (oldTenant == null) {
            return "更换承租人(原无承租人)";
        }
        return String.format("更换承租人, 原承租人: %s, 身份证: %s, 联系方式: %s",
                oldTenant.getName(), oldTenant.getIdCard(), oldTenant.getPhone());
    }

    private String buildUpdateDesc(RoomDetailDTO before, RoomSaveDTO after) {
        StringBuilder sb = new StringBuilder("修改信息: ");
        List<String> changes = new ArrayList<>();

        if (before.getRoom() != null) {
            if (!Objects.equals(before.getRoom().getArea(), after.getArea())) {
                changes.add("面积");
            }
            if (!Objects.equals(before.getRoom().getRent(), after.getRent())) {
                changes.add("租金");
            }
        }
        if (before.getTenant() != null) {
            if (!Objects.equals(before.getTenant().getName(), after.getTenantName())) {
                changes.add("承租人姓名");
            }
            if (!Objects.equals(before.getTenant().getPhone(), after.getPhone())) {
                changes.add("联系方式");
            }
        }

        if (changes.isEmpty()) {
            return "修改房屋/承租人信息";
        }
        sb.append(String.join(", ", changes));
        return sb.toString();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
