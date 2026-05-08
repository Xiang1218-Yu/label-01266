package com.rental.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房屋导出DTO
 */
@Data
public class RoomExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("小区名称")
    @ColumnWidth(20)
    private String communityName;

    @ExcelProperty("楼栋名称")
    @ColumnWidth(15)
    private String buildingName;

    @ExcelProperty("房号")
    @ColumnWidth(15)
    private String roomNumber;

    @ExcelProperty("楼层")
    @ColumnWidth(10)
    private Integer floor;

    @ExcelProperty("面积(㎡)")
    @ColumnWidth(12)
    private BigDecimal area;

    @ExcelProperty("租金(元/月)")
    @ColumnWidth(12)
    private BigDecimal rent;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;

    @ExcelProperty("承租人")
    @ColumnWidth(15)
    private String tenantName;

    @ExcelProperty("身份证号")
    @ColumnWidth(20)
    private String idCard;

    @ExcelProperty("联系电话")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;
}
