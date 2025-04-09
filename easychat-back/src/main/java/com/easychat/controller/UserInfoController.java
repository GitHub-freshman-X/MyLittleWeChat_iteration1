package com.easychat.controller;

import java.io.IOException;
import java.util.List;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.service.UserInfoService;
import com.easychat.utils.CopyTools;
import com.easychat.utils.StringTools;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

	@Resource
	private UserInfoService userInfoService;
	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserInfoQuery query){
		return getSuccessResponseVO(userInfoService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserInfo bean) {
		userInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId查询对象
	 */
	@RequestMapping("/getUserInfoByUserId")
	public ResponseVO getUserInfoByUserId(String userId) {
		return getSuccessResponseVO(userInfoService.getUserInfoByUserId(userId));
	}

	/**
	 * 根据UserId修改对象
	 */
	@RequestMapping("/updateUserInfoByUserId")
	public ResponseVO updateUserInfoByUserId(UserInfo bean,String userId) {
		userInfoService.updateUserInfoByUserId(bean,userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId删除
	 */
	@RequestMapping("/deleteUserInfoByUserId")
	public ResponseVO deleteUserInfoByUserId(String userId) {
		userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email查询对象
	 */
	@RequestMapping("/getUserInfoByEmail")
	public ResponseVO getUserInfoByEmail(String email) {
		return getSuccessResponseVO(userInfoService.getUserInfoByEmail(email));
	}

	/**
	 * 根据Email修改对象
	 */
	@RequestMapping("/updateUserInfoByEmail")
	public ResponseVO updateUserInfoByEmail(UserInfo bean,String email) {
		userInfoService.updateUserInfoByEmail(bean,email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email删除
	 */
	@RequestMapping("/deleteUserInfoByEmail")
	public ResponseVO deleteUserInfoByEmail(String email) {
		userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVO(null);
	}

	// 定义获取用户信息的接口
	@RequestMapping("/getUserInfo")
	@GlobalInterceptor
	public ResponseVO getUserInfo(HttpServletRequest request) {
		// 从请求中获取Token用户信息
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		// 通过用户ID获取用户信息
		UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
		// 将用户信息复制到VO对象中
		UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
		// 设置用户是否为管理员
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
		// 返回成功响应
		return getSuccessResponseVO(userInfoVO);
	}

	@RequestMapping("/saveUserInfo")
	@GlobalInterceptor
	public ResponseVO saveUserInfo(
			HttpServletRequest request,
			UserInfo userInfo,
			MultipartFile avatarFile,
			MultipartFile avatarCover) throws IOException {

		// 获取当前用户的Token信息
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		// 设置用户ID
		userInfo.setUserId(tokenUserInfoDto.getUserId());
		// 将密码、状态、创建时间和最后登录时间设置为null
		userInfo.setPassword(null);
		userInfo.setStatus(null);
		userInfo.setCreateTime(null);
		userInfo.setLastLoginTime(null);
		// 更新用户信息
		userInfoService.updateUserInfo(userInfo, avatarFile, avatarCover);
		// 返回用户信息
		return getUserInfo(request);
	}

	@RequestMapping("/updatePassword")
	@GlobalInterceptor
	public ResponseVO updatePassword(HttpServletRequest request,
									 @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD)
									 String password) {

		// 获取当前用户的Token信息
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		// 创建用户信息对象
		UserInfo userInfo = new UserInfo();
		// 设置用户密码（使用MD5加密）
		userInfo.setPassword(StringTools.encodeMd5(password));
		// 通过用户ID更新用户信息
		this.userInfoService.updateUserInfoByUserId(userInfo, tokenUserInfoDto.getUserId());
		//TODO 强制退出，重新登录
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/logout")
	@GlobalInterceptor
	public ResponseVO logout(HttpServletRequest request) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		//TODO 退出登录 关闭WS连接
		return getSuccessResponseVO(null);
	}
}