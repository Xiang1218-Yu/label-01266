package com.rental.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房屋导入DTO
 */
@Data
public class RoomImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("小区名称")
    private String communityName;

    @ExcelProperty("楼栋名称")
    private String buildingName;

    @ExcelProperty("房号")
    private String roomNumber;

    @ExcelProperty("楼层")
    private Integer floor;

    @ExcelProperty("面积(㎡)")
    private BigDecimal area;

    @ExcelProperty("租金(元/月)")
    private BigDecimal rent;

    @ExcelProperty("状态")
    private String statusText;

    @ExcelProperty("承租人")
    private String tenantName;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("备注")
    private String remark;
}
