package com.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.entity.Room;
import com.rental.dto.RoomCardDTO;
import com.rental.dto.RoomStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 房屋Mapper
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 获取楼栋房屋统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as rentedCount, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as vacantCount " +
            "FROM room WHERE building_id = #{buildingId}")
    RoomStatsDTO getBuildingStats(@Param("buildingId") Long buildingId);

    /**
     * 获取小区房屋统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END) as rentedCount, " +
            "SUM(CASE WHEN r.status = 0 THEN 1 ELSE 0 END) as vacantCount " +
            "FROM room r " +
            "INNER JOIN building b ON r.building_id = b.id " +
            "WHERE b.community_id = #{communityId}")
    RoomStatsDTO getCommunityStats(@Param("communityId") Long communityId);

    /**
     * 获取房屋卡片列表
     */
    @Select("<script>" +
            "SELECT r.id, r.room_number as roomNumber, r.floor, r.status, t.name as tenantName " +
            "FROM room r " +
            "LEFT JOIN tenant t ON t.room_id = r.id AND t.status = 1 " +
            "WHERE r.building_id = #{buildingId} " +
            "<if test='floor != null'> AND r.floor = #{floor} </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "ORDER BY r.floor, r.room_number" +
            "</script>")
    List<RoomCardDTO> getRoomCards(@Param("buildingId") Long buildingId,
                                   @Param("floor") Integer floor,
                                   @Param("status") Integer status);
}
