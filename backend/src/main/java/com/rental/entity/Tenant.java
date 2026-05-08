package com.rental.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 承租人实体
 */
@Data
@TableName("tenant")
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private String name;

    private String idCard;

    /** 婚姻状况: 0-未婚, 1-已婚, 2-离异, 3-丧偶 */
    private Integer maritalStatus;

    private Integer familySize;

    private String phone;

    /** 收入状况: 0-低收入, 1-中等收入, 2-高收入 */
    private Integer incomeStatus;

    private String communityBelong;

    private LocalDateTime checkInTime;

    /** 是否残疾: 0-否, 1-是 */
    private Integer isDisabled;

    /** 拆迁/轮候: 0-无, 1-拆迁, 2-轮候 */
    private Integer relocationType;

    /** 出售/置换: 0-无, 1-出售, 2-置换 */
    private Integer saleExchange;

    private String remark;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
