package com.easychat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easychat.entity.dto.MessageSendDto;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.*;
import com.easychat.entity.query.*;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.*;
import com.easychat.redis.RedisComponent;
import com.easychat.utils.CopyTools;
import com.easychat.websocket.ChannelContextUtils;
import com.easychat.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.service.UserContactApplyService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 联系人申请 业务接口实现
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService {

	@Resource
	private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

	@Resource
	private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

	@Resource
	private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private MessageHandler messageHandler;

	@Resource
	private ChannelContextUtils channelContextUtils;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserContactApply> findListByParam(UserContactApplyQuery param) {
		return this.userContactApplyMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserContactApplyQuery param) {
		return this.userContactApplyMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserContactApply> list = this.findListByParam(param);
		PaginationResultVO<UserContactApply> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserContactApply bean) {
		return this.userContactApplyMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserContactApply> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactApplyMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserContactApply bean, UserContactApplyQuery param) {
		StringTools.checkParam(param);
		return this.userContactApplyMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserContactApplyQuery param) {
		StringTools.checkParam(param);
		return this.userContactApplyMapper.deleteByParam(param);
	}

	/**
	 * 根据ApplyId获取对象
	 */
	@Override
	public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
		return this.userContactApplyMapper.selectByApplyId(applyId);
	}

	/**
	 * 根据ApplyId修改
	 */
	@Override
	public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
		return this.userContactApplyMapper.updateByApplyId(bean, applyId);
	}

	/**
	 * 根据ApplyId删除
	 */
	@Override
	public Integer deleteUserContactApplyByApplyId(Integer applyId) {
		return this.userContactApplyMapper.deleteByApplyId(applyId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId获取对象
	 */
	@Override
	public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId修改
	 */
	@Override
	public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean, applyUserId, receiveUserId, contactId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	@Override
	public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void dealWithApply(String userId,Integer applyId,Integer status) {
		UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
		if (statusEnum == null|| UserContactApplyStatusEnum.INIT==statusEnum) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserContactApply applyInfo = this.userContactApplyMapper.selectByApplyId(applyId);
		if(applyInfo == null||!userId.equals(applyInfo.getReceiveUserId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserContactApply updateInfo = new UserContactApply();
		updateInfo.setStatus(statusEnum.getStatus());
		updateInfo.setLastApplyTime(System.currentTimeMillis());

		UserContactApplyQuery applyQuery = new UserContactApplyQuery();
		applyQuery.setApplyId(applyId);
		applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());

		Integer count = this.userContactApplyMapper.updateByParam(updateInfo, applyQuery);
		if(count == 0){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(UserContactApplyStatusEnum.PASS.getStatus().equals(status)){
			this.addContact(applyInfo.getApplyUserId(),applyInfo.getReceiveUserId(),applyInfo.getContactId(),applyInfo.getContactType(),applyInfo.getApplyInfo());
			return;
		}

		if(UserContactApplyStatusEnum.BLACKLIST == statusEnum){
			Date currentDate = new Date();
			UserContact userContact = new UserContact();
			userContact.setUserId(applyInfo.getApplyUserId());
			userContact.setContactId(applyInfo.getContactId());
			userContact.setContactType(applyInfo.getContactType());
			userContact.setCreateTime(currentDate);
			userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
			userContact.setLastUpdateTime(currentDate);
			userContactMapper.insertOrUpdate(userContact);
		}
	}

	@Override
	public void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType,String applyInfo){
		if(UserContacTypeEnum.GROUP.getType().equals(contactType)){
			UserContactQuery userContactQuery = new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer count = userContactMapper.selectCount(userContactQuery);
			SysSettingDto sysSettingDto = redisComponent.getSysSetting();
			if(count>=sysSettingDto.getMaxGroupMemberCount()){
				throw new BusinessException("成员已满，无法加入");
			}
		}
		Date currentDate = new Date();
		//同意，双方添加好友
		List<UserContact> contactList = new ArrayList<>();
		//申请人添加对方
		UserContact userContact = new UserContact();
		userContact.setUserId(applyUserId);
		userContact.setContactId(contactId);
		userContact.setContactType(contactType);
		userContact.setCreateTime(currentDate);
		userContact.setLastUpdateTime(currentDate);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactList.add(userContact);
		//如果申请好友，接收人添加好友，群组不用添加对方好友
		if(UserContacTypeEnum.USER.getType().equals(contactType)){
			userContact = new UserContact();
			userContact.setUserId(receiveUserId);
			userContact.setContactId(applyUserId);
			userContact.setContactType(contactType);
			userContact.setCreateTime(currentDate);
			userContact.setLastUpdateTime(currentDate);
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contactList.add(userContact);
		}
		//批量加入
		userContactMapper.insertOrUpdateBatch(contactList);
		if(UserContactTypeEnum.USER.getType().equals(contactType)){
			redisComponent.addUserContact(receiveUserId, applyUserId);
		}
		redisComponent.addUserContact(applyUserId,contactId);

		//创建对话
		String sessionId =null;
		if(UserContactTypeEnum.USER.getType().equals(contactType)){
			sessionId = StringTools.getChatSessionId4User(new String[]{applyUserId, contactId}) ;
		}else {
			sessionId = StringTools.getChatSessionId4Group(contactId);
		}

		List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			// 创建会话
			ChatSession chatSession = new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(applyInfo);
			chatSession.setLastReceiveTime(currentDate.getTime());
			this.chatSessionMapper.insertOrUpdate(chatSession);

			// 申请人session
			ChatSessionUser applySessionUser = new ChatSessionUser();
			applySessionUser.setUserId(applyUserId);
			applySessionUser.setContactId(contactId);
			applySessionUser.setSessionId(sessionId);
			UserInfo contactUser = this.userInfoMapper.selectByUserId(contactId);
			applySessionUser.setContactName(contactUser.getNickName());
			chatSessionUserList.add(applySessionUser);

			// 接受人session
			ChatSessionUser contactSessionUser = new ChatSessionUser();
			contactSessionUser.setUserId(contactId);
			contactSessionUser.setContactId(applyUserId);
			contactSessionUser.setSessionId(sessionId);
			UserInfo applyUserInfo = this.userInfoMapper.selectByUserId(applyUserId);
			contactSessionUser.setContactName(applyUserInfo.getNickName());
			chatSessionUserList.add(contactSessionUser);
			this.chatSessionUserMapper.insertOrUpdateBatch(chatSessionUserList);

			// 记录消息表
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
			chatMessage.setMessageContent(applyInfo);
			chatMessage.setSendUserId(applyUserId);
			chatMessage.setSendUserNickName(applyUserInfo.getNickName());
			chatMessage.setSendTime(currentDate.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.USER.getType());
			chatMessageMapper.insert(chatMessage);

			MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
			// 发送给接受还有申请人
			messageHandler.sendMessage(messageSendDto);

			// 既是申请人，发送人就是接受人，联系人就是申请人
			messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
			messageSendDto.setContactId(applyUserId);
			messageSendDto.setExtendData(contactUser);
			messageHandler.sendMessage(messageSendDto);

		}else{
			//加入群组
			ChatSessionUser chatSessionUser = new ChatSessionUser();
			chatSessionUser.setUserId(applyUserId);
			chatSessionUser.setContactId(contactId);
			GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
			chatSessionUser.setContactId(groupInfo.getGroupName());//groupInfo.getGroupId()
			chatSessionUser.setSessionId(sessionId);
			this.chatSessionUserMapper.insertOrUpdate(chatSessionUser);

			UserInfo appyUserInfo = this.userInfoMapper.selectByUserId(applyUserId);
			String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(),appyUserInfo.getNickName());

			//增加session信息
			ChatSession chatSession = new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastReceiveTime(currentDate.getTime());
			chatSession.setLastMessage(sendMessage);
			this.chatSessionMapper.insertOrUpdate(chatSession);
			//增加聊天消息
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
			chatMessage.setMessageContent(sendMessage);
			chatMessage.setSendTime(currentDate.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
			this.chatMessageMapper.insert(chatMessage);
			//将群组添加到联系人
			redisComponent.addUserContact(applyUserId,groupInfo.getGroupId());
			//将联系人添加到群组通道
			channelContextUtils.addUser2Group(applyUserId,groupInfo.getGroupId());
			//发送消息
			MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
			messageSendDto.setContactId(contactId);

			//获取群组数量
			UserContactQuery userContactQuery = new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer memberCount = this.userContactMapper.selectCount(userContactQuery);
			messageSendDto.setMemberCount(memberCount);
			messageSendDto.setContactName(groupInfo.getGroupName());
			messageHandler.sendMessage(messageSendDto);
		}
	}
}