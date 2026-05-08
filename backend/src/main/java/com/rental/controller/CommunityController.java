package com.rental.controller;

import com.rental.common.Result;
import com.rental.dto.CommunityTreeDTO;
import com.rental.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小区控制器
 */
@Tag(name = "小区管理")
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "获取小区树形结构")
    @GetMapping("/tree")
    public Result<List<CommunityTreeDTO>> getCommunityTree() {
        List<CommunityTreeDTO> tree = communityService.getCommunityTree();
        return Result.success(tree);
    }

    @Operation(summary = "搜索小区")
    @GetMapping("/search")
    public Result<List<CommunityTreeDTO>> searchCommunity(@RequestParam(required = false) String keyword) {
        List<CommunityTreeDTO> result = communityService.searchCommunity(keyword);
        return Result.success(result);
    }

    @Operation(summary = "刷新小区缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        communityService.refreshCache();
        return Result.success();
    }
}
