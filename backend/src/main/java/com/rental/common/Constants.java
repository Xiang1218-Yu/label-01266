package com.rental.common;

/**
 * 系统常量
 */
public class Constants {

    private Constants() {}

    // 房屋状态
    public static final int ROOM_STATUS_VACANT = 0;      // 空置
    public static final int ROOM_STATUS_RENTED = 1;      // 已出租

    // 婚姻状况
    public static final int MARITAL_UNMARRIED = 0;       // 未婚
    public static final int MARITAL_MARRIED = 1;         // 已婚
    public static final int MARITAL_DIVORCED = 2;        // 离异
    public static final int MARITAL_WIDOWED = 3;         // 丧偶

    // 收入状况
    public static final int INCOME_LOW = 0;              // 低收入
    public static final int INCOME_MEDIUM = 1;           // 中等收入
    public static final int INCOME_HIGH = 2;             // 高收入

    // 拆迁/轮候类型
    public static final int RELOCATION_NONE = 0;         // 无
    public static final int RELOCATION_DEMOLITION = 1;   // 拆迁
    public static final int RELOCATION_WAITING = 2;      // 轮候

    // 出售/置换
    public static final int SALE_EXCHANGE_NONE = 0;      // 无
    public static final int SALE_EXCHANGE_SALE = 1;      // 出售
    public static final int SALE_EXCHANGE_EXCHANGE = 2;  // 置换

    // 操作类型
    public static final String OP_TYPE_ADD = "新增";
    public static final String OP_TYPE_UPDATE = "修改";
    public static final String OP_TYPE_CHANGE_TENANT = "更换承租人";
    public static final String OP_TYPE_DELETE = "删除";

    // Redis缓存Key前缀
    public static final String CACHE_COMMUNITY_TREE = "rental:community:tree";
    public static final String CACHE_ROOM_STATS = "rental:room:stats:";
    public static final String CACHE_TOKEN_PREFIX = "rental:token:";

    // 缓存过期时间(秒)
    public static final long CACHE_EXPIRE_COMMUNITY = 3600;
    public static final long CACHE_EXPIRE_STATS = 300;
}
