package com.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 承租人Mapper
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
