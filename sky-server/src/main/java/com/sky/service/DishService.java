package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);
    PageResult page(DishPageQueryDTO dishPageQueryDTO);
    Dish getById(Long id);
    void deleteBatch(List<Long> ids);
}
