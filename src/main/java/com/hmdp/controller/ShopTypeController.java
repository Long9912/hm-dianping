package com.hmdp.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("list")
    public Result queryTypeList() {
        //1，从redis查询缓存
        String key = "cache:showType";
        String cache = stringRedisTemplate.opsForValue().get(key);
        //2，判断是否存在
        if (StrUtil.isNotBlank(cache)){
            //3.存在，直接返回
            List<ShopType> typeList = JSONUtil.toList(cache,ShopType.class);
            return Result.ok(typeList);
        }
        //4，不存在，查询数据库
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        //5.存在，写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList),5, TimeUnit.MINUTES);
        return Result.ok(typeList);
    }
}
