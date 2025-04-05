package com.easychat.entity.enums;

import com.easychat.entity.po.UserContactApply;
import com.easychat.utils.StringTools;

public enum UserContatctApplyStatusEnum {
    INIT(0,"待处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑");
    private Integer status;
    private String desc;
    UserContatctApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public Integer getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }
    public static UserContatctApplyStatusEnum getByStatus(String status) {
        try{
            if(StringTools.isEmpty(status)){
                return null;
            }
            return UserContatctApplyStatusEnum.valueOf(status.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static UserContatctApplyStatusEnum getByStatus(Integer status) {
        for(UserContatctApplyStatusEnum item : UserContatctApplyStatusEnum.values()){
            if(item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }
}
