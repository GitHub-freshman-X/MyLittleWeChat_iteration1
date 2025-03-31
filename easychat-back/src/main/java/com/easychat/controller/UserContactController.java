package com.easychat.controller;

import java.util.List;

import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UserContactService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 联系人 Controller
 */
@RestController("userContactController")
@RequestMapping("/userContact")
public class UserContactController extends ABaseController{

	@Resource
	private UserContactService userContactService;
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
}