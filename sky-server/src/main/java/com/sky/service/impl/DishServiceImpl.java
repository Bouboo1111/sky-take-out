package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setMealDishMapper;
    @Override
    @Transactional
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO){
        //向菜品表插入数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //向口味表插入数据
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()){
            List<DishFlavor> flavors = dishDTO.getFlavors();
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult page(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        Long total = page.getTotal();
        List<DishVO> records = page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public Dish getById(Long id){
        return dishMapper.getById(id);
    }
    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        ids.forEach(id-> {
                    Dish dish = dishMapper.getById(id);
                    //判断当前菜品是否停售
                    if (dish.getStatus() == StatusConstant.ENABLE) {
                        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
                    }
                });
            //判断当前菜品是否被套餐引用
            List<Long> setmealIds = setMealDishMapper.getSetmealIdsByDishIds(ids);
            if(setmealIds != null && setmealIds.size() > 0){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            //删除菜品数据
            dishMapper.deleteBatch(ids);
            //删除菜品口味数据
            dishFlavorMapper.deleteByDishIds(ids);


    }
    /**
     * 根据菜品id查询菜品的口味数据
     * @param id
     * @return
     */
    @Transactional
    public DishVO getByIdWithFlavor(Long id){
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            throw new AccountNotFoundException(MessageConstant.DISH_NOT_FOUND);
        }
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //组装DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO){
        //修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //删除菜品口味数据
        Long id = dishDTO.getId();
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(id));
        //插入菜品口味数据
        Long dishId = dish.getId();
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()){
            List<DishFlavor> flavors = dishDTO.getFlavors();
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id){
        Dish dish = Dish.builder()
                .status( status).id( id).build();
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品选项
     * @param categoryId
     * return
     */
    public List<Dish> list(Long categoryId){
        List<Dish> list = dishMapper.list(categoryId);
        return list;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    @Transactional
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.listWithFlavor(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


}
