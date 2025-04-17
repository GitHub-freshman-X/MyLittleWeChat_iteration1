package com.easychat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.ChatSession;
import com.easychat.entity.po.ChatSessionUser;
import com.easychat.entity.query.ChatSessionQuery;
import com.easychat.entity.query.ChatSessionUserQuery;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.ChatSessionMapper;
import com.easychat.mappers.ChatSessionUserMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.utils.CopyTools;
import com.easychat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.easychat.entity.query.ChatMessageQuery;
import com.easychat.entity.po.ChatMessage;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.query.SimplePage;
import com.easychat.mappers.ChatMessageMapper;
import com.easychat.service.ChatMessageService;
import com.easychat.utils.StringTools;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

	@Resource
	private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

	@Resource
	private MessageHandler messageHandler;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatMessage> findListByParam(ChatMessageQuery param) {
		return this.chatMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatMessageQuery param) {
		return this.chatMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatMessage> list = this.findListByParam(param);
		PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatMessage bean) {
		return this.chatMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
		StringTools.checkParam(param);
		return this.chatMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatMessageQuery param) {
		StringTools.checkParam(param);
		return this.chatMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public ChatMessage getChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
		return this.chatMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.deleteByMessageId(messageId);
	}

	@Override
	public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
		if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
			List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
			if (!contactList.contains(chatMessage.getContactId())) {
				UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
				if (UserContactTypeEnum.USER.equals(userContactTypeEnum)) {
					throw new BusinessException(ResponseCodeEnum.CODE_902);
				} else {
					throw new BusinessException(ResponseCodeEnum.CODE_903);
				}
			}
		}

		String sessionId = null;
		String sendUserId = tokenUserInfoDto.getUserId();
		String contactId = chatMessage.getContactId();
		UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if(UserContactTypeEnum.USER==contactTypeEnum){
			sessionId = StringTools.getChatSessionId4User(new String[]{contactId,sendUserId});
		}else{
			sendUserId = StringTools.getChatSessionId4Group(contactId);
		}
		chatMessage.setSessionId(sessionId);
		Long curTime = System.currentTimeMillis();
		chatMessage.setSendTime(curTime);

		MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
		if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[] {MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, chatMessage.getMessageType())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Integer status = MessageTypeEnum.MEDIA_CHAT==messageTypeEnum? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
		chatMessage.setStatus(status);

		String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
		chatMessage.setMessageContent(messageContent);

		//跟新会话
		ChatSession chatSession = new ChatSession();
		chatSession.setLastMessage(messageContent);
		if(UserContactTypeEnum.GROUP==contactTypeEnum){
			chatSession.setLastMessage(tokenUserInfoDto.getNickName()+", "+messageContent);
		}
		chatSession.setLastReceiveTime(curTime);
		chatSessionMapper.updateBySessionId(chatSession,sessionId);

		//记录消息表
		chatMessage.setSendUserId(sendUserId);
		chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
		chatMessage.setContactType(contactTypeEnum.getType());
		chatMessageMapper.insert(chatMessage);
		MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);

		if(Constants.ROBOT_UID.equals(contactId)){
			SysSettingDto sysSettingDto = redisComponent.getSysSetting();
			TokenUserInfoDto robot = new TokenUserInfoDto();
			robot.setUserId(sysSettingDto.getRobotUid());
			robot.setNickName(sysSettingDto.getRobotNickName());
			ChatMessage robotChatMessage = new ChatMessage();
			robotChatMessage.setContactId(sendUserId);
			//这里可以对接AI,实现聊天
			robotChatMessage.setMessageContent("我只是一个机器人，无法识别你的消息");
			robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
			saveMessage(robotChatMessage,robot);
		}else{
			messageHandler.sendMessage(messageSendDto);
		}
		return messageSendDto;
	}
}