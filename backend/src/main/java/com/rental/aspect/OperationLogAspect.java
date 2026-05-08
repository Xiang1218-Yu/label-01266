package com.rental.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.annotation.OperationLog;
import com.rental.entity.SysOperationLog;
import com.rental.security.SecurityUtils;
import com.rental.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setModule(operationLog.module());
        logEntity.setOperationType(operationLog.type());
        logEntity.setDescription(operationLog.description());
        logEntity.setOperationTime(LocalDateTime.now());

        // 获取当前用户
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        logEntity.setUserId(userId);
        logEntity.setUsername(username);

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logEntity.setIpAddress(ServletUtil.getClientIP(request));
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
        }

        // 获取请求参数
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                logEntity.setRequestParams(StrUtil.sub(params, 0, 2000));
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
        }

        Object result = null;
        try {
            result = point.proceed();
            logEntity.setStatus(1);
            
            // 记录响应结果
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                logEntity.setResponseResult(StrUtil.sub(resultStr, 0, 2000));
            } catch (Exception e) {
                log.warn("序列化响应结果失败", e);
            }
        } catch (Throwable e) {
            logEntity.setStatus(0);
            logEntity.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("操作日志 - 模块: {}, 类型: {}, 耗时: {}ms", 
                    operationLog.module(), operationLog.type(), costTime);
            operationLogService.saveLog(logEntity);
        }

        return result;
    }
}
