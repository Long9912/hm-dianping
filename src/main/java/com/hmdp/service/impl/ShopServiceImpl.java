package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        //1，从redis查询商铺缓存
        String key = "cache:show" + id;
        String cache = stringRedisTemplate.opsForValue().get(key);
        //2，判断是否存在
        if (StrUtil.isNotBlank(cache)){
            //3.存在，直接返回
            Shop shop = JSONUtil.toBean(cache,Shop.class);
            return Result.ok(shop);
        }
        //判断缓存是否为空值
        if ("".equals(cache)){
            return Result.fail("店铺不存在");
        }
        //4，不存在，根据id查询数据库
        Shop shop = getById(id);
        //5.不存在，返回错误
        if (shop == null){
            //空值写入Redis
            stringRedisTemplate.opsForValue().set(key,"",3, TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
        //6.存在，写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),30, TimeUnit.MINUTES);
        //7.返回
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null){
            return Result.fail("店铺不能为空");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        String key = "cache:show" + id;
        stringRedisTemplate.delete(key);
        return Result.ok();
    }
}
