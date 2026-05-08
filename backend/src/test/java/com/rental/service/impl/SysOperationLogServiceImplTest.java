package com.rental.service.impl;

import com.rental.entity.SysOperationLog;
import com.rental.mapper.SysOperationLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysOperationLogServiceImplTest {

    @Mock
    private SysOperationLogMapper sysOperationLogMapper;

    @InjectMocks
    private SysOperationLogServiceImpl sysOperationLogService;

    @Test
    @DisplayName("保存操作日志 - 成功")
    void saveLog_success() {
        SysOperationLog log = new SysOperationLog();
        log.setModule("房屋管理");
        log.setOperationType("保存");
        log.setDescription("保存房屋详情");

        when(sysOperationLogMapper.insert(any())).thenReturn(1);

        sysOperationLogService.saveLog(log);

        verify(sysOperationLogMapper).insert(log);
    }

    @Test
    @DisplayName("保存操作日志 - 异常时不抛出")
    void saveLog_exception() {
        SysOperationLog log = new SysOperationLog();
        log.setModule("房屋管理");

        when(sysOperationLogMapper.insert(any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> sysOperationLogService.saveLog(log));
    }
}
