package org.kangspace.messagepush.core.redis;


import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.core.util.StrUtil;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis相关操作Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
public class RedisService {

    @Getter
    private RedisTemplate<String, String> redisTemplate;

    /**
     * <pre>
     * 获取Redis缓存对象;
     * 若fetchData数据为空时,返回null,反之若缓存不存在时,重新进行缓存。
     * </pre>
     *
     * @param key       redis key
     * @param clazz     redis 缓存的对象类型
     * @param time      缓存超时时间
     * @param fetchData 重新获取对象的方法
     * @param <T>       目标对象
     * @return T or null, 当 fetchData 为空时,返回null
     */
    public <T> T getAndCache(String key, Class<T> clazz, long time, Supplier<T> fetchData) {
        T cache = this.get(key, clazz);
        if (cache == null) {
            synchronized (this) {
                cache = this.get(key, clazz);
                if (cache == null) {
                    cache = fetchData.get();
                    if (cache != null) {
                        //重新缓存
                        this.setEX(key, cache, time);
                    }
                }
            }
        }
        return cache;
    }

    /**
     * 分布式锁
     *
     * @param key      redis key
     * @param ttl      超时时间
     * @param callback 获取到锁后的执行函数
     * @return boolean true:已获取到锁,false:未获取到锁
     */
    public <T> boolean lock(String key, long ttl, Runnable callback) {
        String flag = System.currentTimeMillis() + "";
        if (this.setNX(key, flag, ttl)) {
            try {
                callback.run();
            } finally {
                String value = this.get(key);
                if (value != null && flag.equals(value)) {
                    this.del(key);
                }
            }
            return true;
        }
        return false;
    }

    public <T> Map<String, T> hGetAll(String key, Class<T> type) {
        if (!StrUtil.isEmpty(key) && type != null) {
            Map<Object, Object> map = this.getRedisTemplate().opsForHash().entries(key);
            Map<String, T> result = new HashMap();
            Iterator var5 = map.entrySet().iterator();

            while (var5.hasNext()) {
                Map.Entry<Object, Object> obj = (Map.Entry) var5.next();
                result.put((String) obj.getKey(), JsonUtil.toObject((String) obj.getValue(), type));
            }

            return result;
        } else {
            return null;
        }
    }

    public <T> Boolean setNX(String key, T value) {
        return !StrUtil.isEmpty(key) && value != null ? this.getRedisTemplate().opsForValue().setIfAbsent(key, JsonUtil.toJson(value)) : false;
    }

    public <T> Boolean setNX(String key, T value, long time) {
        return !StrUtil.isEmpty(key) && value != null ? this.getRedisTemplate().opsForValue().setIfAbsent(key, JsonUtil.toJson(value), time, TimeUnit.SECONDS) : false;
    }

    public <T> void setEX(String key, T value, long time) {
        if (!StrUtil.isEmpty(key) && value != null) {
            this.getRedisTemplate().opsForValue().set(key, JsonUtil.toJson(value), time, TimeUnit.SECONDS);
        }
    }

    public String get(String key) {
        return StrUtil.isEmpty(key) ? null : (String) this.getRedisTemplate().opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return !StrUtil.isEmpty(key) && type != null ? JsonUtil.toObject((String) this.getRedisTemplate().opsForValue().get(key), type) : null;
    }

    public Boolean del(String key) {
        return StrUtil.isEmpty(key) ? false : this.getRedisTemplate().delete(key);
    }

    public <T> void hMSet(String key, Map<String, T> map) {
        if (!StrUtil.isEmpty(key) && !MapUtil.isEmpty(map)) {
            Map<String, String> saveMap = new HashMap();
            Iterator var4 = map.entrySet().iterator();

            while (var4.hasNext()) {
                Map.Entry<String, T> obj = (Map.Entry) var4.next();
                saveMap.put(obj.getKey(), JsonUtil.toJson(obj.getValue()));
            }

            this.getRedisTemplate().opsForHash().putAll(key, saveMap);
        }
    }

}
