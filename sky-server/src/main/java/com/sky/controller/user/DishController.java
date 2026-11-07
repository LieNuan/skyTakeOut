package com.sky.controller.user;

import com.alibaba.fastjson2.JSON;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Tag(name = "C端-菜品浏览接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis的key：dish_分类id
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品数据（value是JSON字符串，空列表[]也算有效缓存）
        List<DishVO> list = queryCache(key);
        //如果存在直接返回
        if (list != null) {
            log.info("菜品缓存命中：{}", key);
            return Result.success(list);
        }

        //如果不存在，查询数据库，再放到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        //查询起售中的菜品
        list = dishService.listWithFlavor(dish);

        //缓存30分钟，避免无效分类id产生的key长期堆积
        redisTemplate.opsForValue().set(key, JSON.toJSONString(list), 30, TimeUnit.MINUTES);
        return Result.success(list);
    }

    /**
     * 读取缓存并转成 List<DishVO>
     * 缓存内容不是合法JSON时（例如旧版本序列化格式）当作未命中处理
     */
    private List<DishVO> queryCache(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return JSON.parseArray(json, DishVO.class);
        } catch (Exception e) {
            log.warn("菜品缓存格式非法，已删除并重新查询数据库：{}", key);
            redisTemplate.delete(key);
            return null;
        }
    }
}
