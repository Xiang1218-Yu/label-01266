package com.rental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rental.dto.CommunityTreeDTO;
import com.rental.entity.Community;
import java.util.List;

/**
 * 小区服务接口
 */
public interface CommunityService extends IService<Community> {

    /**
     * 获取小区树形结构
     */
    List<CommunityTreeDTO> getCommunityTree();

    /**
     * 搜索小区
     */
    List<CommunityTreeDTO> searchCommunity(String keyword);

    /**
     * 刷新小区缓存
     */
    void refreshCache();
}
