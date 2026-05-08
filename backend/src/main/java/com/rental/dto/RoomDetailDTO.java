package com.rental.dto;

import com.rental.entity.FamilyMember;
import com.rental.entity.Room;
import com.rental.entity.RoomOperationLog;
import com.rental.entity.Tenant;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 房屋详情DTO
 */
@Data
public class RoomDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 房屋信息 */
    private Room room;

    /** 承租人信息 */
    private Tenant tenant;

    /** 家庭成员列表 */
    private List<FamilyMember> familyMembers;

    /** 操作记录列表 */
    private List<RoomOperationLog> operationLogs;
}
