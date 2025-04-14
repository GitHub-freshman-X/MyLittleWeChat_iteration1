package com.easychat.websocket.netty;

import com.easychat.entity.config.Appconfig;
import com.easychat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NeeyWebSocketStarter implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NeeyWebSocketStarter.class);
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource
    private Appconfig appconfig;

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception{
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要的处理器
                            //队Http协议的支持，使用Http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码，httpRequest/httpContent/lastHttpContent到fullHttprequest
                            //保证接受的HTTP请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //心跳
                            //ReaderIdleTime,读超时事件 即测试段一定事件内未接收到被测试段的消息
                            //writeIdTime 写超时事件
                            //allIdtime 所有类型的时间
                            pipeline.addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将Http协议升级为ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,64*1024,true,true,1000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            Integer wsPort=appconfig.getWsPort();
            String wsPortStr=System.getProperty("ws.port");
            if(!StringTools.isEmpty(wsPortStr)){
                wsPort = Integer.parseInt(wsPortStr);
            }
            ChannelFuture channelFuture = serverBootstrap.bind(appconfig.getWsPort()).sync();
            logger.info("netty服务启动成功，port:{}",appconfig.getWsPort());
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error("启动netty失败",e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
