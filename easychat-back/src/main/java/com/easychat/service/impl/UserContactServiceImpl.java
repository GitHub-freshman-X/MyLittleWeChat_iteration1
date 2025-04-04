package com.easychat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserContactApply;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.*;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.GroupInfoMapper;
import com.easychat.mappers.UserContactApplyMapper;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.UserContactApplyService;
import com.easychat.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.stereotype.Service;

import com.easychat.entity.po.UserContact;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.mappers.UserContactMapper;
import com.easychat.service.UserContactService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.scanner.Constant;

import static org.apache.ibatis.reflection.ArrayUtil.*;


/**
 * 联系人 业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

	@Resource
	private UserContactApplyService userContactApplyService;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserContact> findListByParam(UserContactQuery param) {
		return this.userContactMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserContactQuery param) {
		return this.userContactMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserContact> findListByPage(UserContactQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserContact> list = this.findListByParam(param);
		PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserContact bean) {
		return this.userContactMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserContact bean, UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Override
	public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId修改
	 */
	@Override
	public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
		return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
	}

	@Override
	public UserContactSearchResultDto searchContact(String userId, String contactId) {
		UserContacTypeEnum typeEnum = UserContacTypeEnum.getByPrefix(contactId);
		if(typeEnum == null) {
			return null;
		}
		UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
		switch (typeEnum) {
			case USER:
				UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
				if(userInfo == null) {
					return null;
				}
				resultDto = CopyTools.copy(userInfo, UserContactSearchResultDto.class);
				break;
			case GROUP:
				GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
				if(groupInfo == null) {
					return null;
				}
				resultDto.setNickName(groupInfo.getGroupName());
				break;
		}
		resultDto.setContactType(typeEnum.toString());
		resultDto.setContactId(contactId);
		if(userId.equals(contactId)) {
			resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			return resultDto;
		}
		//查询是否好友
		UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
		resultDto.setStatus(userContact==null?null:userContact.getStatus());
		return resultDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo){
		UserContacTypeEnum typeEnum = UserContacTypeEnum.getByPrefix(contactId);
		if(typeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//申请人
		String applyUserId = tokenUserInfoDto.getUserId();
		//默认申请信息
		applyInfo=StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE,tokenUserInfoDto.getNickName()) : applyInfo;
		Long curTime = System.currentTimeMillis();
		Integer joinType =null;
		String receiveUserId = contactId;
		//查询对方好友是否已经添加，如果拉黑无法添加
		UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId, contactId);
		if(userContact != null &&
				ArraysUtil.contains(new Integer[]{
						UserContactStatusEnum.BLACKLIST_BE.getStatus(),
						UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus(),
						},userContact.getStatus())) {
			throw new BusinessException("对方已将你拉黑，无法添加");
		}
		if(UserContacTypeEnum.GROUP==typeEnum){
			GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
			if(groupInfo == null||GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo)) {
				throw new BusinessException("群聊不存在或已解散");
			}
			receiveUserId = groupInfo.getGroupId();
			joinType =  groupInfo.getJoinType();
		} else{
			UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
			if(userInfo == null) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			joinType = userInfo.getJoinType();
		}
		//直接加入不用记录
		if(JoinTypeEnum.JOIN.getType().equals(joinType)) {
			userContactApplyService.addContact(applyUserId,receiveUserId,contactId,typeEnum.getType(),applyInfo);
			userContactApplyService.addContact(receiveUserId,applyUserId,applyUserId,typeEnum.getType(),applyInfo);
			return joinType;
		}
		UserContactApply dbApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		if(dbApply == null) {
			UserContactApply contactApply = new UserContactApply();
			contactApply.setApplyUserId(applyUserId);
			contactApply.setContactType(typeEnum.getType());
			contactApply.setContactId(contactId);
			contactApply.setReceiveUserId(receiveUserId);
			contactApply.setLastApplyTime(curTime);
			contactApply.setStatus(UserContatctApplyStatusEnum.INIT.getStatus());
			contactApply.setApplyInfo(applyInfo);
			this.userContactApplyMapper.insert(contactApply);
		}else{
			//更新状态
			UserContactApply contactApply = new UserContactApply();
			contactApply.setStatus(UserContatctApplyStatusEnum.INIT.getStatus());
			contactApply.setLastApplyTime(curTime);
			contactApply.setApplyInfo(applyInfo);
			this.userContactApplyMapper.updateByApplyId(contactApply,dbApply.getApplyId());
		}

		if(dbApply==null||!UserContatctApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
			//TODO 发送ws消息
		}

		return joinType;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum){
		//移除好友
		UserContact userContact = new UserContact();
		userContact.setStatus(statusEnum.getStatus());
		userContactMapper.updateByUserIdAndContactId(userContact,userId,contactId);

		//将好友中也移除自己
		UserContact friendContact = new UserContact();
		if(UserContactStatusEnum.DEL==statusEnum){
			friendContact.setStatus(UserContactStatusEnum.DEL_EB.getStatus());
		}else if(UserContactStatusEnum.BLACKLIST==statusEnum){
			friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
		}
		userContactMapper.updateByUserIdAndContactId(friendContact,contactId,userId);
		//TODO 从我的好友列表缓存中删除好友
		//TODO 从好友列表缓存中删除我
	}
}