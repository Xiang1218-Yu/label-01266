package com.rental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.entity.SysUser;

/**
 * 系统用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     */
    LoginResultDTO login(LoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 退出登录
     */
    void logout(String token);
}
