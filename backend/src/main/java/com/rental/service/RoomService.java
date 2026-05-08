package com.rental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rental.dto.*;
import com.rental.entity.Room;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 房屋服务接口
 */
public interface RoomService extends IService<Room> {

    /**
     * 获取楼栋统计信息
     */
    RoomStatsDTO getBuildingStats(Long buildingId);

    /**
     * 获取小区统计信息
     */
    RoomStatsDTO getCommunityStats(Long communityId);

    /**
     * 获取房屋卡片列表
     */
    List<RoomCardDTO> getRoomCards(Long buildingId, Integer floor, Integer status);

    /**
     * 获取房屋详情
     */
    RoomDetailDTO getRoomDetail(Long roomId);

    /**
     * 保存房屋信息(包含承租人和家庭成员)
     */
    void saveRoomDetail(RoomSaveDTO dto, Long operatorId, String operatorName);

    /**
     * 导入房源
     */
    void importRooms(MultipartFile file);

    /**
     * 导出房源
     */
    void exportRooms(Long communityId, Long buildingId, HttpServletResponse response);

    /**
     * 获取楼栋楼层列表
     */
    List<Integer> getBuildingFloors(Long buildingId);
}
