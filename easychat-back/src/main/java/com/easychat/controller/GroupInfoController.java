package com.easychat.controller;

import java.io.IOException;
import java.util.List;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.GroupStatusEnum;
import com.easychat.entity.enums.MessageTypeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.vo.GroupInfoVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.GroupInfoService;
import com.easychat.service.UserContactService;
import org.apache.catalina.startup.UserConfig;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
@Validated
public class GroupInfoController extends ABaseController {

	@Resource
	private GroupInfoService groupInfoService;
    @Resource
	private UserContactService userContactService;

	@RequestMapping("/saveGroup")
	@GlobalInterceptor
	public ResponseVO saveGroup(HttpServletRequest request, String groupId, @NotEmpty String groupName, String groupNotice, @NotNull Integer joinType, MultipartFile avatarFile,
								MultipartFile avatarCover) throws IOException {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		GroupInfo groupInfo = new GroupInfo();
		groupInfo.setGroupId(groupId);
		groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfo.setGroupName(groupName);
		groupInfo.setGroupNotice(groupNotice);
		groupInfo.setJoinType(joinType);
		this.groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/loadMyGroup")
	@GlobalInterceptor
	public ResponseVO LoadMyGroup(HttpServletRequest request) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
		groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfoQuery.setOrderBy("create_time desc");
		List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
		return getSuccessResponseVO(groupInfoList);
	}


	@RequestMapping("/getGroupInfo")
	@GlobalInterceptor
	public ResponseVO getGroupInfo(HttpServletRequest request,@NotEmpty String groupId) {
		// 获取群组详细信息
		GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

		// 创建用户联系查询对象
		UserContactQuery userContactQuery = new UserContactQuery();
		userContactQuery.setContactId(groupId); // 设置群组ID为联系ID

		// 通过用户联系服务查询群组成员数量
		Integer memberCount = this.userContactService.findCountByParam(userContactQuery);

		// 设置群组成员数量到群组信息中
		groupInfo.setMemberCount(memberCount);

		// 返回成功的响应VO，包含群组信息
		return getSuccessResponseVO(groupInfo);
	}

	private GroupInfo getGroupDetailCommon(HttpServletRequest request,String groupId){
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

		UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
		if(userContact==null|| !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
			throw new BusinessException("你不在群聊或群聊已解散");
		}
		GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
		if(null==groupInfo|| !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
			throw new BusinessException("群聊不存在或已解散");
		}
		return groupInfo;
	}

	@RequestMapping("/getGroupInfo4Chat")
	@GlobalInterceptor
	public ResponseVO getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId) {
		// 获取群组详细信息
		GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

		// 创建用户联系查询对象
		UserContactQuery userContactQuery = new UserContactQuery();
		userContactQuery.setContactId(groupId); // 设置群组ID为联系ID
		userContactQuery.setQueryUserInfo(true); // 查询用户信息
		userContactQuery.setOrderBy("create_time asc"); // 按创建时间升序排序
		userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus()); // 设置状态为好友

		// 通过用户联系服务查询用户联系列表
		List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);

		// 创建GroupInfoVO对象并设置群组信息和用户联系列表
		GroupInfoVO groupInfoVO = new GroupInfoVO();
		groupInfoVO.setGroupInfo(groupInfo);
		groupInfoVO.setUserContactList(userContactList);

		// 返回成功的响应VO，包含群组信息和用户联系列表
		return getSuccessResponseVO(groupInfoVO);
	}

	@RequestMapping("/addOrRemoveGroupUser")
	@GlobalInterceptor
	public ResponseVO addOrRemoveGroupUser(HttpServletRequest request,
										   @NotEmpty String groupId,
										   @NotEmpty String selectContacts,
										   @NotNull Integer opType) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto, groupId, selectContacts, opType);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/leaveGroup")
	@GlobalInterceptor
	public ResponseVO leaveGroup(HttpServletRequest request, @NotEmpty String groupId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/dissolutionGroup")
	@GlobalInterceptor
	public ResponseVO dissolutionGroup(HttpServletRequest request, @NotEmpty String groupId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(), groupId);
		return getSuccessResponseVO(null);
	}

}