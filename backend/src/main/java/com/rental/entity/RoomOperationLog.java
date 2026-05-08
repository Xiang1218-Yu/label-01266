package com.rental.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房屋操作记录实体
 */
@Data
@TableName("room_operation_log")
public class RoomOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Long tenantId;

    private Long operatorId;

    private String operatorName;

    private String operationType;

    private String description;

    private String beforeData;

    private String afterData;

    private LocalDateTime operationTime;
}
