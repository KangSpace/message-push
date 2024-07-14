package org.kangspace.messagepush.consumer.core.redis;

import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Redis相关操作Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@Service
public class RedisService extends org.kangspace.messagepush.core.redis.RedisService {

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
        T cache = super.get(key, clazz);
        if (cache == null) {
            synchronized (this) {
                cache = super.get(key, clazz);
                if (cache == null) {
                    cache = fetchData.get();
                    if (cache != null) {
                        //重新缓存
                        super.setEX(key, cache, time);
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
        if (super.setNX(key, flag, ttl)) {
            try {
                callback.run();
            } finally {
                String value = super.get(key);
                if (value != null && flag.equals(value)) {
                    super.del(key);
                }
            }
            return true;
        }
        return false;
    }
}
