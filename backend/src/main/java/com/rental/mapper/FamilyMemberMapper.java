package com.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭成员Mapper
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
}
