package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    void addDishAndFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO pageQueryDTO);

    void deleteDishById(List<Long> ids);

    void updateDishAndFlavor(DishDTO dishDTO);
}
