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
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @Operation(summary = "获取店铺营业状态")
    public Result<Integer> getStatus(){
        log.info("获取店铺营业状态为：{}", (Integer)redisTemplate.opsForValue().get("SHOP_STATUS")==1?"营业":"关闭");
        return  Result.success((Integer) redisTemplate.opsForValue().get("SHOP_STATUS"));
    }
}
