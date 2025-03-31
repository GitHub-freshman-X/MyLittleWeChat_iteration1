package com.easychat.controller;

import java.util.List;

import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.GroupInfoService;
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
@RequestMapping("/groupInfo")
public class GroupInfoController extends ABaseController {

	@Resource
	private GroupInfoService groupInfoService;

	@RequestMapping("/saveGroup")
	public ResponseVO saveGroup(HttpServletRequest request,String groupId, @NotEmpty String groupName, String groupNotice, @NotNull Integer joinType, MultipartFile avatarFile,
								MultipartFile avatarCover) {
		TokenUserInfoDto tokenUserInfoDto= getTokenUserInfo(request);

		return getSuccessResponseVO(null);
	}
}