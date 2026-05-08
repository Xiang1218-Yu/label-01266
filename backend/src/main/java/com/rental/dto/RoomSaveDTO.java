package com.rental.dto;

import com.rental.entity.FamilyMember;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房屋保存DTO
 */
@Data
public class RoomSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 房屋信息 ==========
    @NotNull(message = "房屋ID不能为空")
    private Long roomId;

    @DecimalMin(value = "0", message = "面积不能为负数")
    private BigDecimal area;

    @DecimalMin(value = "0", message = "租金不能为负数")
    private BigDecimal rent;

    private Integer status;

    private String roomRemark;

    // ========== 承租人信息 ==========
    private Long tenantId;

    @Size(max = 50, message = "姓名长度不能超过50")
    private String tenantName;

    @Pattern(regexp = "^$|^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "身份证号格式不正确")
    private String idCard;

    private Integer maritalStatus;

    @Min(value = 1, message = "家庭人口至少为1")
    private Integer familySize;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Integer incomeStatus;

    private String communityBelong;

    private LocalDateTime checkInTime;

    private Integer isDisabled;

    private Integer relocationType;

    private Integer saleExchange;

    private String tenantRemark;

    // ========== 家庭成员信息 ==========
    @Valid
    private List<FamilyMemberDTO> familyMembers;

    // ========== 操作类型 ==========
    /** 是否更换承租人 */
    private Boolean changeTenant;

    @Data
    public static class FamilyMemberDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;

        @NotBlank(message = "家庭成员姓名不能为空")
        @Size(max = 50, message = "姓名长度不能超过50")
        private String name;

        @NotBlank(message = "家庭成员身份证号不能为空")
        @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
                message = "身份证号格式不正确")
        private String idCard;

        @Size(max = 50, message = "关系长度不能超过50")
        private String relationship;
    }
}
