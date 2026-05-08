package com.rental.controller;

import com.rental.annotation.OperationLog;
import com.rental.common.Result;
import com.rental.dto.*;
import com.rental.security.SecurityUtils;
import com.rental.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 房屋控制器
 */
@Tag(name = "房屋管理")
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "获取楼栋统计信息")
    @GetMapping("/stats/building/{buildingId}")
    public Result<RoomStatsDTO> getBuildingStats(@PathVariable Long buildingId) {
        RoomStatsDTO stats = roomService.getBuildingStats(buildingId);
        return Result.success(stats);
    }

    @Operation(summary = "获取小区统计信息")
    @GetMapping("/stats/community/{communityId}")
    public Result<RoomStatsDTO> getCommunityStats(@PathVariable Long communityId) {
        RoomStatsDTO stats = roomService.getCommunityStats(communityId);
        return Result.success(stats);
    }

    @Operation(summary = "获取房屋卡片列表")
    @GetMapping("/cards/{buildingId}")
    public Result<List<RoomCardDTO>> getRoomCards(
            @PathVariable Long buildingId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer status) {
        List<RoomCardDTO> cards = roomService.getRoomCards(buildingId, floor, status);
        return Result.success(cards);
    }

    @Operation(summary = "获取楼栋楼层列表")
    @GetMapping("/floors/{buildingId}")
    public Result<List<Integer>> getBuildingFloors(@PathVariable Long buildingId) {
        List<Integer> floors = roomService.getBuildingFloors(buildingId);
        return Result.success(floors);
    }

    @Operation(summary = "获取房屋详情")
    @GetMapping("/detail/{roomId}")
    public Result<RoomDetailDTO> getRoomDetail(@PathVariable Long roomId) {
        RoomDetailDTO detail = roomService.getRoomDetail(roomId);
        return Result.success(detail);
    }

    @Operation(summary = "保存房屋详情")
    @PostMapping("/save")
    @OperationLog(module = "房屋管理", type = "保存", description = "保存房屋详情")
    public Result<Void> saveRoomDetail(@Valid @RequestBody RoomSaveDTO dto) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        String operatorName = SecurityUtils.getCurrentUsername();
        roomService.saveRoomDetail(dto, operatorId, operatorName);
        return Result.success("保存成功", null);
    }

    @Operation(summary = "导入房源")
    @PostMapping("/import")
    @OperationLog(module = "房屋管理", type = "导入", description = "导入房源数据")
    public Result<Void> importRooms(@RequestParam("file") MultipartFile file) {
        roomService.importRooms(file);
        return Result.success("导入成功", null);
    }

    @Operation(summary = "导出房源")
    @GetMapping("/export")
    public void exportRooms(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) Long buildingId,
            HttpServletResponse response) {
        roomService.exportRooms(communityId, buildingId, response);
    }
}
