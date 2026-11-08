package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Tag(name = "菜品接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @Operation(summary = "新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        dishService.addDishAndFlavor(dishDTO);
        String key="dish_"+dishDTO.getCategoryId();
        clearCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询菜品")
    public Result<PageResult> pageDish(DishPageQueryDTO pageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜品（含口味）")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @DeleteMapping
    @Operation(summary = "根据id批量删除菜品")
    public Result deleteDishById(@RequestParam List<Long> ids) {
        dishService.deleteDishById(ids);
        clearCache("dish_*");

        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        dishService.updateDishAndFlavor(dishDTO);
        clearCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        clearCache("dish_*");
        return Result.success();
    }

    //清理所有dish_的缓存数据
    private void clearCache(String pattern) {

        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
