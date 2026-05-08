package com.rental.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String operationType;

    private String module;

    private String description;

    private String ipAddress;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseResult;

    private Integer status;

    private String errorMsg;

    private LocalDateTime operationTime;
}
