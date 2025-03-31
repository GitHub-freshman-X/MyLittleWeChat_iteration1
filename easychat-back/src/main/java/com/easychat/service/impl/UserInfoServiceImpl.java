package com.easychat.service.impl;

import java.util.*;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.UserInfoBeautyQuery;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.UserInfoBeautyMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.redis.RedisUtils;
import com.easychat.utils.CopyTools;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.stereotype.Service;

import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.query.SimplePage;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.service.UserInfoService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private UserInfoBeautyMapper <UserInfoBeauty,UserInfoQuery> userInfoBeautyMapper;

	@Resource
	private Appconfig appconfig;

	@Resource
	private RedisComponent redisComponent;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}


	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password) {

		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);
		if(null!=userInfo){
			throw new BusinessException("邮箱账号已经存在");
		}
		String userId= StringTools.getUserID();
		UserInfoBeauty beautyAccount=this.userInfoBeautyMapper.selectByEmail(email);
		Boolean useBeautyAccount = null != beautyAccount && BeautyAccountStatusEnum.No_USE.getStatus().equals(beautyAccount.getStatus());
		if(useBeautyAccount){
			userId=UserContacTypeEnum.USER.getPrefix()+beautyAccount.getUserId();
		}
		Date curDate=new Date();
		userInfo=new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setNickName(nickName);
		userInfo.setEmail(email);
		userInfo.setPassword(StringTools.encodeMd5(password));
		userInfo.setCreateTime(curDate);
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setLastOffTime(curDate.getTime());
		userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
		this.userInfoMapper.insert(userInfo);
		if(useBeautyAccount){
			UserInfoBeauty updateBeauty=new UserInfoBeauty();
			updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
			this.userInfoBeautyMapper.updateById(updateBeauty,beautyAccount.getId());
		}
		//TODO 创建机器人好友
	}

	@Override
	public UserInfoVO login(String email, String password) {
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);

		if(null==userInfo || !userInfo.getPassword().equals(password)){
			throw new BusinessException("账号或密码错误");
		}

		if(UserStatusEnum.DISABLE.equals(userInfo.getStatus())){
			throw new BusinessException("账号已禁用");
		}
		//TODO 	查询我的群组
		//TODO 查询我的联系人

		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(userInfo);
		Long LastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
		if(null!=LastHeartBeat){
			throw new BusinessException("此账号已经在别处登录，请退出再登录");
		}
		//保存登录信息到redis中
		String token=StringTools.encodeMd5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_20));
		tokenUserInfoDto.setToken(token);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

		UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
		userInfoVO.setToken(tokenUserInfoDto.getToken());
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
		return userInfoVO;
	}
	private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo){
		TokenUserInfoDto tokenUserInfoDto=new TokenUserInfoDto();
		tokenUserInfoDto.setUserId(userInfo.getUserId());
		tokenUserInfoDto.setNickName(userInfo.getNickName());
		String adminEmail=appconfig.getAdminEmail();
		if(!StringTools.isEmpty(adminEmail)&& ArrayUtils.contains(adminEmail.split(","),userInfo.getEmail())){
			tokenUserInfoDto.setAdmin(true);
		}else{
			tokenUserInfoDto.setAdmin(false);
		}
		return tokenUserInfoDto;
	}
}