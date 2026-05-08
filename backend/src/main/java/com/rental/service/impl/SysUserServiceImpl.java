package com.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rental.common.BusinessException;
import com.rental.common.Constants;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.entity.SysUser;
import com.rental.mapper.SysUserMapper;
import com.rental.security.JwtTokenProvider;
import com.rental.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginResultDTO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());

        // 查询用户
        SysUser user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expireTime = System.currentTimeMillis() + jwtTokenProvider.getExpiration();

        // 存入Redis
        String redisKey = Constants.CACHE_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, jwtTokenProvider.getExpiration(), TimeUnit.MILLISECONDS);

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setExpireTime(expireTime);

        log.info("用户登录成功: {}", loginDTO.getUsername());
        return result;
    }

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null) {
                String redisKey = Constants.CACHE_TOKEN_PREFIX + userId;
                redisTemplate.delete(redisKey);
                log.info("用户退出登录: userId={}", userId);
            }
        }
    }
}
