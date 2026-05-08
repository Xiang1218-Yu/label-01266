package com.rental.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rental.entity.SysOperationLog;
import com.rental.mapper.SysOperationLogMapper;
import com.rental.service.SysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志服务实现
 */
@Slf4j
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> 
        implements SysOperationLogService {

    @Async
    @Override
    public void saveLog(SysOperationLog operationLog) {
        try {
            save(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
