package com.easychat.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import java.util.Collection;

@Component("redisUtils")
public class RedisUtils<V> {
    @Resource
    private RedisTemplate<String, V> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);


    public void delete(String... key) {
        if(key != null && key.length > 0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            }
            else {
                redisTemplate.delete((Collection<String>)CollectionUtils.arrayToList(key));
            }
        }

    }
    public  V get(String key) {
        logger.info("获取get key:{}", key);
        logger.info("获取get value:{}", redisTemplate.opsForValue().get(key));
        if(key==null){
            return null;
        }else{
            return redisTemplate.opsForValue().get(key);
        }
    }

    public  boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }
        catch (Exception e) {
            logger.error("设置redisKey：{}，value:{}失败", key, value);
            return false;
        }

    }
    public  boolean setex(String key, V value, long time) {
        try {
            if(time>0){
                logger.info("setex执行了，key : {}, value : {}, time : {}", key, value, time);
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }
            else {
                set(key, value);
            }
            return  true;
        }
        catch (Exception e) {
            logger.error("设置redisKey：{}，value:()失败",key,value);
            return false;
        }
    }

    public  boolean expire(String key, long time) {
        try {
            if(time>0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return  true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<V> getQueueList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public boolean lpush(String key, V value, long time ) {
        try{
            redisTemplate.opsForList().leftPush(key, value);
            if(time>0){
                expire(key,time);
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long remove(String key, V value) {
        try{
            Long remove=redisTemplate.opsForList().remove(key,1,value);
            return remove;
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public boolean lpushAll(String key, List<V> values,long time) {
        try{
            redisTemplate.opsForList().leftPushAll(key,values);
            if(time>0){
                expire(key,time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
