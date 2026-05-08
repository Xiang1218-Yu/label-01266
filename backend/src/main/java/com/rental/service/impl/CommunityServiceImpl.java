package com.rental.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rental.common.Constants;
import com.rental.dto.CommunityTreeDTO;
import com.rental.entity.Building;
import com.rental.entity.Community;
import com.rental.mapper.BuildingMapper;
import com.rental.mapper.CommunityMapper;
import com.rental.service.CommunityService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 小区服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl extends ServiceImpl<CommunityMapper, Community> implements CommunityService {

    private final BuildingMapper buildingMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<CommunityTreeDTO> getCommunityTree() {
        // 先从缓存获取
        String cacheKey = Constants.CACHE_COMMUNITY_TREE;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);
        
        if (StrUtil.isNotBlank(cacheData)) {
            try {
                return objectMapper.readValue(cacheData, new TypeReference<List<CommunityTreeDTO>>() {});
            } catch (Exception e) {
                log.warn("解析小区缓存失败", e);
            }
        }

        // 从数据库查询
        List<CommunityTreeDTO> tree = buildTree();

        // 存入缓存
        try {
            String json = objectMapper.writeValueAsString(tree);
            redisTemplate.opsForValue().set(cacheKey, json, Constants.CACHE_EXPIRE_COMMUNITY, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存小区数据失败", e);
        }

        return tree;
    }

    @Override
    public List<CommunityTreeDTO> searchCommunity(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return getCommunityTree();
        }

        // 搜索匹配的小区
        List<Community> communities = list(new LambdaQueryWrapper<Community>()
                .like(Community::getName, keyword)
                .eq(Community::getStatus, 1));

        return communities.stream().map(c -> {
            CommunityTreeDTO dto = new CommunityTreeDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setType("community");
            dto.setNodeKey("community_" + c.getId());
            dto.setChildren(getBuildingsByCommId(c.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void refreshCache() {
        redisTemplate.delete(Constants.CACHE_COMMUNITY_TREE);
        getCommunityTree();
        log.info("小区缓存已刷新");
    }

    private List<CommunityTreeDTO> buildTree() {
        List<Community> communities = list(new LambdaQueryWrapper<Community>()
                .eq(Community::getStatus, 1)
                .orderByAsc(Community::getId));

        return communities.stream().map(c -> {
            CommunityTreeDTO dto = new CommunityTreeDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setType("community");
            dto.setNodeKey("community_" + c.getId());
            dto.setChildren(getBuildingsByCommId(c.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<CommunityTreeDTO> getBuildingsByCommId(Long communityId) {
        List<Building> buildings = buildingMapper.selectList(
                new LambdaQueryWrapper<Building>()
                        .eq(Building::getCommunityId, communityId)
                        .eq(Building::getStatus, 1)
                        .orderByAsc(Building::getId));

        return buildings.stream().map(b -> {
            CommunityTreeDTO dto = new CommunityTreeDTO();
            dto.setId(b.getId());
            dto.setName(b.getName());
            dto.setType("building");
            dto.setNodeKey("building_" + b.getId());
            dto.setParentId(communityId);
            return dto;
        }).collect(Collectors.toList());
    }
}
