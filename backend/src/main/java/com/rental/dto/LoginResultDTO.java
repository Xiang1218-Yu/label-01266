package com.rental.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录结果DTO
 */
@Data
public class LoginResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private Long expireTime;
}
