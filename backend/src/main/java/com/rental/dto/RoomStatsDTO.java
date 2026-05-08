package com.rental.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 房屋统计DTO
 */
@Data
public class RoomStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总户数 */
    private Integer totalCount;

    /** 已出租数 */
    private Integer rentedCount;

    /** 空置数 */
    private Integer vacantCount;

    /** 入住率 */
    private BigDecimal occupancyRate;

    public BigDecimal getOccupancyRate() {
        if (totalCount == null || totalCount == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rentedCount)
                .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
