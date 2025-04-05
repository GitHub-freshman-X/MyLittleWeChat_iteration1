package com.easychat.entity.vo;

import java.util.List;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserContact;

public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;

    // 获取用户联系列表
    public List<UserContact> getUserContactList() {
        return userContactList;
    }

    // 设置用户联系列表
    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }

    // 获取群组信息
    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    // 设置群组信息
    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }
}