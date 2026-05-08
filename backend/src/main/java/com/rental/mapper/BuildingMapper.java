package com.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.entity.Building;
import org.apache.ibatis.annotations.Mapper;

/**
 * 楼栋Mapper
 */
@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
}
