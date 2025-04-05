package com.easychat.websocket.netty;

import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.redis.RedisComponent;
import com.easychat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private RedisComponent redisComponent;

    private static final Logger logger = LoggerFactory.getLogger(HandlerWebSocket.class);

    /**
     * 通道就绪后 调用，一般用户来做初始化
     * @param ctx
     * @throw Excpetion
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新的连接加入....");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有链接断开...");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        logger.info("收到消息{}", textWebSocketFrame.text());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if(token == null){
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if(tokenUserInfoDto == null){
                ctx.channel().close();
                return;
            }
        }
    }

    private String getToken(String url){
        if(StringTools.isEmpty(url)||url.indexOf("?")==-1){
            return null;
        }
        String[] queryParams = url.split("\\?");
        if(queryParams.length!=2){
            return null;
        }
        String[] params = queryParams[1].split("=");
        if(params.length!=2){
            return null;
        }
        return params[1];
    }
}
