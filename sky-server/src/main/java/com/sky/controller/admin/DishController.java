package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 插入菜品和口味
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        Long categoryId = dishDTO.getCategoryId();
        deleteCache("dish_"+categoryId);
        log.info("删除缓存{}",categoryId);
        return Result.success();

    }
    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除{}",ids);
        dishService.deleteBatch(ids);
        deleteCache("dish_*");
        log.info("删除缓存");
        return Result.success();
    }

    /**
     * 根据id查询菜品关联口味数据
     */
    @GetMapping("/{id}")
    public Result<DishVO> getByIdWithFlavor(@PathVariable Long id){
        DishVO dishvo = dishService.getByIdWithFlavor(id);
        return Result.success(dishvo);
    }
    /**
     * 菜品修改
     * @Param id
     * @return
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        dishService.updateWithFlavor(dishDTO);
        deleteCache("dish_*");
        log.info("删除缓存");
        return Result.success();
    }

    /**
     * 商品起售停售
     * @Param dish
     * return
     */
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);
        deleteCache("dish_*");
        log.info("删除缓存");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @Param categoryId
     *  return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void deleteCache(String key){
        Set<String> keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);
    }


}
