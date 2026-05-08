package com.rental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rental.entity.SysOperationLog;

/**
 * 系统操作日志服务接口
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 记录操作日志
     */
    void saveLog(SysOperationLog log);
}
