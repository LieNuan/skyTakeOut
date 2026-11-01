package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Tag(name = "菜品接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @PostMapping
    @Operation(summary = "新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        dishService.addDishAndFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询菜品")
    public Result<PageResult> pageDish(DishPageQueryDTO pageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @Operation(summary = "根据id批量删除菜品")
    public Result deleteDishById(@RequestParam List<Long> ids) {
        dishService.deleteDishById(ids);
        return Result.success();
    }
}
