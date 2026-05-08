package com.rental.security;

import cn.hutool.core.util.StrUtil;
import com.rental.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        // 没有 token，直接放行（让 Spring Security 处理）
        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 验证 token 有效性
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Token验证失败: {}", token.substring(0, Math.min(20, token.length())) + "...");
                sendUnauthorized(response, "Token已过期或无效");
                return;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 验证 Redis 中的 Token
            String redisKey = Constants.CACHE_TOKEN_PREFIX + userId;
            String redisToken = redisTemplate.opsForValue().get(redisKey);

            log.debug("验证Token - userId: {}, redisKey: {}, tokenMatch: {}", userId, redisKey, token.equals(redisToken));

            if (!token.equals(redisToken)) {
                log.warn("Redis Token不匹配 - userId: {}, 请求token: {}..., redis token: {}", 
                    userId, 
                    token.substring(0, Math.min(20, token.length())),
                    redisToken != null ? redisToken.substring(0, Math.min(20, redisToken.length())) + "..." : "null");
                sendUnauthorized(response, "登录已失效，请重新登录");
                return;
            }

            // 设置认证信息
            LoginUser loginUser = new LoginUser(userId, username);
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT认证失败", e);
            sendUnauthorized(response, "认证失败");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
