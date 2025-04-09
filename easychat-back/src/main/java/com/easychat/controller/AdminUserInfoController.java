package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.service.UserContactService;
import com.easychat.service.UserInfoService;
import com.easychat.utils.CopyTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("adminUserInfoController")
@RequestMapping("/admin")
public class AdminUserInfoController extends ABaseController{
    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
public ResponseVO loadUser(UserInfoQuery userInfoQuery){
        userInfoQuery.setOrderBy("creat_time desc");
        PaginationResultVO resultVO=userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status, @NotEmpty String userid){

       userInfoService.updateUserStatus(status,userid);
        return  getSuccessResponseVO(null);
    }

    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine (@NotEmpty String userid){

       userInfoService.forceOffLine(userid);
        return  getSuccessResponseVO(null);
    }



}
