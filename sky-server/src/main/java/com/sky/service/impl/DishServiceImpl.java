package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service

public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Transactional
    @Override
    public void addDishAndFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //添加菜品口味
        Long id = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(dishDTO.getFlavors() != null && dishDTO.getFlavors().size() > 0){
            flavors.forEach(df -> df.setDishId(id));
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Dish> page= dishMapper.selectPage(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteDishById(List<Long> ids) {
        //起售中的菜品不能被删除
        for(Long id:ids){
            Dish dish = dishMapper.selectById(id);
            if(dish != null && StatusConstant.ENABLE.equals(dish.getStatus())){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //套餐里的商品不能删除
        Integer count = setmealDishMapper.countByDishIds(ids);
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Transactional
    @Override
    public void updateDishAndFlavor(DishDTO dishDTO) {
        //导入新数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);

        //删除旧口味
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));

        //更新菜品
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(df -> df.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
