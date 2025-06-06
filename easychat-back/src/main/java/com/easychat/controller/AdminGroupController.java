package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.query.UserInfoBeautyQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.GroupInfoService;
import com.easychat.service.UserInfoBeautyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.management.Query;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("adminGroupController")
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController{
    @Resource
    private GroupInfoService groupInfoService;

    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
public ResponseVO loadGroup (GroupInfoQuery query){

        query.setOrderBy("creat_time desc");
        query.setQueryMemberConut(true);
        query.setQueryGroupOwnerName(true);
        PaginationResultVO resultVO=groupInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup (@NotEmpty String groupId){
        GroupInfo groupInfo =groupInfoService.getGroupInfoByGroupId(groupId);
        if(groupInfo==null){
               throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
       groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(),groupId);
        return getSuccessResponseVO(null);
    }


}
