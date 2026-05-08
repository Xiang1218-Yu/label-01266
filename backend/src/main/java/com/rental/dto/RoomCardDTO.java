package com.rental.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 房屋卡片DTO
 */
@Data
public class RoomCardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String roomNumber;

    private Integer floor;

    private Integer status;

    private String tenantName;
}
