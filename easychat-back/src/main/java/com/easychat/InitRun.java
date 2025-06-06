package com.easychat;

import com.easychat.redis.RedisUtils;
import com.easychat.websocket.netty.NeeyWebSocketStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;
    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private NeeyWebSocketStarter neeyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) {
        try{
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread(neeyWebSocketStarter).start();
            logger.info("服务启动成功，可以开始了");
        } catch (SQLException e) {
            logger.error("数据库配置错误，请检查");
        }catch(RedisConnectionFailureException e){
            logger.error("redis配置错误，请检查redis配置");
        } catch (Exception e) {
            logger.error("服务器启动失败");
        }
    }
}
