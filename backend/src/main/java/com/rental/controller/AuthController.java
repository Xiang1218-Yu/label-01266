package com.rental.controller;

import com.rental.annotation.OperationLog;
import com.rental.common.Result;
import com.rental.dto.LoginDTO;
import com.rental.dto.LoginResultDTO;
import com.rental.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResultDTO result = sysUserService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "认证管理", type = "退出", description = "用户退出登录")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        sysUserService.logout(token);
        return Result.success("退出成功", null);
    }
}
