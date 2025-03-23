package redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import java.util.Collection;

@Component("redisUtils")
public class RedisUtils<V> {
    @Resource
    private RedisTemplate<String, V> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);



    public void delete(String... key) {
        if(key == null || key.length == 0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            }
            else {
                redisTemplate.delete((Collection<String>)CollectionUtils.arrayToList(key));
            }
        }

    }
    public  V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public  boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }
        catch (Exception e) {
            logger.error("设置redisKey：{}，value:()失败", key, value);
            return false;
        }

    }
    public  boolean setex(String key, V value, long time) {
        try {
            if(time>0){
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

}
