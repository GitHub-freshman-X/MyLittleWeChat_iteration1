package com.easychat.controller;

import java.util.List;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.dto.UserContactSearchResultDto;
import com.easychat.entity.enums.PageSize;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.enums.UserContacTypeEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.UserContactApply;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.UserContactApplyService;
import com.easychat.service.UserContactService;
import com.easychat.service.UserInfoService;
import com.easychat.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 联系人 Controller
 */
@RestController("userContactController")
@RequestMapping("/contact")
public class UserContactController extends ABaseController{

	@Resource
	private UserContactService userContactService;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private UserContactApplyService userContactApplyService;

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserContactQuery query){
		return getSuccessResponseVO(userContactService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserContact bean) {
		userContactService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserContact> listBean) {
		userContactService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserContact> listBean) {
		userContactService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserIdAndContactId查询对象
	 */
	@RequestMapping("/getUserContactByUserIdAndContactId")
	public ResponseVO getUserContactByUserIdAndContactId(String userId,String contactId) {
		return getSuccessResponseVO(userContactService.getUserContactByUserIdAndContactId(userId,contactId));
	}

	/**
	 * 根据UserIdAndContactId修改对象
	 */
	@RequestMapping("/updateUserContactByUserIdAndContactId")
	public ResponseVO updateUserContactByUserIdAndContactId(UserContact bean,String userId,String contactId) {
		userContactService.updateUserContactByUserIdAndContactId(bean,userId,contactId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@RequestMapping("/deleteUserContactByUserIdAndContactId")
	public ResponseVO deleteUserContactByUserIdAndContactId(String userId,String contactId) {
		userContactService.deleteUserContactByUserIdAndContactId(userId,contactId);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/search")
	@GlobalInterceptor
	public ResponseVO search(HttpServletRequest request, @NotEmpty String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContactSearchResultDto rersultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(),contactId);
		return getSuccessResponseVO(rersultDto);
	}

	@RequestMapping("/applyAdd")
	@GlobalInterceptor
	public ResponseVO applyAdd(HttpServletRequest request, @NotEmpty String contactId, String applyInfo) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		Integer joinType=userContactService.applyAdd(tokenUserInfoDto,contactId,applyInfo);
		return getSuccessResponseVO(joinType);
	}

	@RequestMapping("loadApply")
	@GlobalInterceptor
	public ResponseVO loadApply(HttpServletRequest request, Integer pageNo) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

		UserContactApplyQuery applyQuery=new UserContactApplyQuery();
		applyQuery.setOrderBy("last_apply_time");
		applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
		applyQuery.setPageNo(pageNo);
		applyQuery.setPageSize(PageSize.SIZE15.getSize());
		applyQuery.setQueryContactInfo(true);
		PaginationResultVO resultVO = userContactApplyService.findListByPage(applyQuery);
		return getSuccessResponseVO(resultVO);
	}

	@RequestMapping("dealWithApply")
	@GlobalInterceptor
	public ResponseVO dealWithApply(HttpServletRequest request, @NotNull Integer applyId,@NotNull Integer status) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		this.userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(),applyId,status);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("loadContact")
	@GlobalInterceptor
	public ResponseVO loadContact(HttpServletRequest request, @NotNull String contactType) {
		UserContacTypeEnum contacTypeEnum = UserContacTypeEnum.getByName(contactType);
		if(contacTypeEnum==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContactQuery contactQuery=new UserContactQuery();
		contactQuery.setUserId(tokenUserInfoDto.getUserId());
		contactQuery.setContactType(contacTypeEnum.getType());
		if(contacTypeEnum.USER == contacTypeEnum){
			contactQuery.setQueryContactUserInfo(true);
		}else if(contacTypeEnum.GROUP == contacTypeEnum){
			contactQuery.setQueryGroupInfo(true);
			contactQuery.setExcludeMyGroup(true);
		}
		contactQuery.setOrderBy("last_apply_time desc");
		contactQuery.setStatusArray(new Integer[]{
				UserContactStatusEnum.FRIEND.getStatus(),
				UserContactStatusEnum.DEL_EB.getStatus(),
				UserContactStatusEnum.BLACKLIST_BE.getStatus(),
		});
		List<UserContact> contactList = userContactService.findListByParam(contactQuery);
        return getSuccessResponseVO(contactList);
	}

	/**
	 * 获取联系人信息 不一定是好友
	 * @param request
	 * @param contactId
	 * @return
	 */
	@RequestMapping("getContactInfo")
	@GlobalInterceptor
	public ResponseVO getContactInfo(HttpServletRequest request, @NotNull String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
		UserInfoVO userInfoVO = CopyTools.copy(userInfo,UserInfoVO.class);
		userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());

		UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
		if(userContact!=null){
			userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
		}
		return getSuccessResponseVO(userInfoVO);
	}

	/**
	 * 获取联系人信息 一定是好友
	 * @param request
	 * @param contactId
	 * @return
	 */
	@RequestMapping("getContactUserInfo")
	@GlobalInterceptor
	public ResponseVO getContactUserInfo(HttpServletRequest request, @NotNull String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
		if(userContact==null|| !ArraysUtil.contains(new Integer[]{
				UserContactStatusEnum.FRIEND.getStatus(),
				UserContactStatusEnum.DEL_EB.getStatus(),
				UserContactStatusEnum.BLACKLIST_BE.getStatus(),
		},userContact.getStatus())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
		UserInfoVO userInfoVO = CopyTools.copy(userInfo,UserInfoVO.class);
		return getSuccessResponseVO(userInfoVO);
	}

	@RequestMapping("delContact")
	@GlobalInterceptor
	public ResponseVO delContact(HttpServletRequest request, @NotNull String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.DEL);

		return getSuccessResponseVO(null);
	}

	@RequestMapping("addContact2BlackList")
	@GlobalInterceptor
	public ResponseVO addContact2BlackList(HttpServletRequest request, @NotNull String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);

		return getSuccessResponseVO(null);
	}
}