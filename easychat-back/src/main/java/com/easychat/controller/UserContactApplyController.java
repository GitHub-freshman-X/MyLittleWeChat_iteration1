package com.easychat.controller;

import java.util.List;

import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.po.UserContactApply;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UserContactApplyService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 联系人申请 Controller
 */
@RestController("userContactApplyController")
@RequestMapping("/userContactApply")
public class UserContactApplyController extends ABaseController{

	@Resource
	private UserContactApplyService userContactApplyService;
	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserContactApplyQuery query){
		return getSuccessResponseVO(userContactApplyService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserContactApply bean) {
		userContactApplyService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserContactApply> listBean) {
		userContactApplyService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserContactApply> listBean) {
		userContactApplyService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyId查询对象
	 */
	@RequestMapping("/getUserContactApplyByApplyId")
	public ResponseVO getUserContactApplyByApplyId(Integer applyId) {
		return getSuccessResponseVO(userContactApplyService.getUserContactApplyByApplyId(applyId));
	}

	/**
	 * 根据ApplyId修改对象
	 */
	@RequestMapping("/updateUserContactApplyByApplyId")
	public ResponseVO updateUserContactApplyByApplyId(UserContactApply bean,Integer applyId) {
		userContactApplyService.updateUserContactApplyByApplyId(bean,applyId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyId删除
	 */
	@RequestMapping("/deleteUserContactApplyByApplyId")
	public ResponseVO deleteUserContactApplyByApplyId(Integer applyId) {
		userContactApplyService.deleteUserContactApplyByApplyId(applyId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询对象
	 */
	@RequestMapping("/getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVO getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId,String receiveUserId,String contactId) {
		return getSuccessResponseVO(userContactApplyService.getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId));
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId修改对象
	 */
	@RequestMapping("/updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVO updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean,String applyUserId,String receiveUserId,String contactId) {
		userContactApplyService.updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(bean,applyUserId,receiveUserId,contactId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	@RequestMapping("/deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVO deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId,String receiveUserId,String contactId) {
		userContactApplyService.deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		return getSuccessResponseVO(null);
	}
}