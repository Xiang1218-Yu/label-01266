package com.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.entity.RoomOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房屋操作记录Mapper
 */
@Mapper
public interface RoomOperationLogMapper extends BaseMapper<RoomOperationLog> {
}
