package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Tag(name = "店铺管理")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/status")
    @Operation(summary = "获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer status = getStatusFromRedis();
        log.info("获取店铺营业状态为：{}", status == 1 ? "营业" : "关闭");
        return Result.success(status);
    }

    /**
     * 从redis读取营业状态，key不存在或值非法时按"打烊(0)"处理
     */
    private Integer getStatusFromRedis() {
        String value = redisTemplate.opsForValue().get("SHOP_STATUS");
        if (value == null) {
            return 0;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("营业状态缓存值非法，按打烊处理：{}", value);
            return 0;
        }
    }
}
