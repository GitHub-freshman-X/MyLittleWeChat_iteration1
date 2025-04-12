package com.easychat.websocket;

import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.WsInitData;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.UserContacTypeEnum;
import com.easychat.entity.enums.UserContactApplyStatusEnum;
import com.easychat.entity.po.*;
import com.easychat.entity.query.ChatMessageQuery;
import com.easychat.entity.query.ChatSessionUserQuery;
import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.mappers.ChatMessageMapper;
import com.easychat.mappers.UserContactApplyMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.service.ChatSessionUserService;
import com.easychat.utils.JsonUtils;
import com.easychat.utils.StringTools;
import com.easychat.entity.constants.Constants;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Channel上下文工具类
 * <p>
 * 用于管理WebSocket连接的上下文信息
 */

@Component
public class ChannelContextUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtils.class);

    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery>userInfoMapper;

    @Resource
    private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply,UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private RedisComponent redisComponent;

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        logger.info("channelId:{}", channelId);
        AttributeKey<String> attributeKey = null;
        // 检查是否已经存在对应的AttributeKey
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }

        // 将用户ID设置到Channel的属性中
        channel.attr(attributeKey).set(userId);

        List<String> contactIdList=redisComponent.getUserContactList(userId);
        for(String groupId:contactIdList){
            if(groupId.startsWith(UserContacTypeEnum.GROUP.GROUP.getPrefix())){
                add2Group(groupId,channel);
            }
        }
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveHeartBeat(userId);

        // 更新用户最后连接时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo, userId);

        // 给用户发送消息
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        Long lastOfflineTime = sourceLastOffTime;
        if (sourceLastOffTime!=null && System.currentTimeMillis() - Constants.MILLISECOND_3DAYS_AGO > sourceLastOffTime) {
            lastOfflineTime = Constants.MILLISECOND_3DAYS_AGO;
        }

        // 查询会话信息，查询用户所有的会话信息，保证换了设备会话同步
        ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
        sessionUserQuery.setUserId(userId);
        sessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.findListByParam(sessionUserQuery);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);

        /*
         * 2、查询聊天信息
         */
        //查询所有联系人
        List<String> groupIdList = contactIdList.stream().filter(item->item.startsWith(UserContacTypeEnum.GROUP.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setContactIdList(groupIdList);
        messageQuery.setLastReceiveTime(lastOfflineTime);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(messageQuery);
        wsInitData.setChatMessageList(chatMessageList);
        /*
         *3、查询好友申请
         */
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setReceiveUserId(userId);
        applyQuery.setLastApplyTimestamp(lastOfflineTime);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        Integer applyCount = userContactApplyMapper.selectCount(applyQuery);
        wsInitData.setApplyCount(applyCount);

        //发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);

        sendMsg(messageSendDto,userId);
    }

    // 添加频道到指定的群组
    private void add2Group(String groupId, Channel channel) {
        // 从映射中获取对应的群组
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        // 如果群组不存在，则创建一个新的群组并添加到映射中
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }

        // 如果频道不为空，则添加到群组中
        if (channel != null) {
            group.add(channel);
        }
    }

    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if(StringTools.isEmpty(userId)){
            USER_CONTEXT_MAP.remove(userId);

        }
        redisComponent.removeUserHeartBeat(userId);
        //更新用户最后离线时间
        UserInfo userInfo=new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo,userId);
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        UserContacTypeEnum userContacTypeEnum = UserContacTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (userContacTypeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
        }
    }

    //发送给用户
    private void send2User(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if(StringTools.isEmpty(contactId)){
            return;
        }
        sendMsg(messageSendDto,contactId);
        //强制下线
        if(MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())){
            closeConext(contactId);
        }
    }

    public void closeConext(String userId) {
        if(StringTools.isEmpty(userId)){
            return;
        }
        redisComponent.cleanUserContact(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if(channel!=null){
            return;
        }
        channel.close();
    }

    //发送给群组
    private void send2Group(MessageSendDto messageSendDto) {
         if(StringTools.isEmpty(messageSendDto.getContactId())){
             return;
         }
         ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
         if(channelGroup==null){
             return;
         }
         channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }

    //发送消息
    public void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        Channel userChannel = USER_CONTEXT_MAP.get(receiveId);
        if (userChannel == null) {
            return;
        }
        messageSendDto.setContactId(messageSendDto.getSendUserId());
        messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        userChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }
}
