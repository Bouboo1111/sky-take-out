package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void saveWithDish(SetmealDTO setmealDTO){
        //保存套餐的基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setMealMapper.insert(setmeal);
        //保存菜品套餐的关联关系
        Long setMealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setMealId);
            });
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setMealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids){
        //判断套餐是否在售
        ids.forEach(id-> {
            Setmeal setmeal = setMealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                throw new RuntimeException(MessageConstant.DISH_ON_SALE);
            }
        });
        //删除套餐数据
        setMealMapper.deleteBatch(ids);
        //删除套餐和菜品的关联数据
        setMealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id){
        //根据id查询套餐基本信息
        Setmeal setmeal = setMealMapper.getById(id);
        //根据id查询套餐菜品信息
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setMealDishMapper.getBySetmealId(id));
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    public void updateWithDish(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //修改套餐基本信息
        setMealMapper.update(setmeal);
        //删除套餐和菜品的关联数据
        Long setmealId = setmeal.getId();
        setMealDishMapper.deleteBySetmealIds(Arrays.asList(setmealId));
        //保存新的套餐和菜品的关联数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id){
        Setmeal setmeal = Setmeal.builder()
                .status(status).id(id)
                .build();
        setMealMapper.update(setmeal);
    }
}
