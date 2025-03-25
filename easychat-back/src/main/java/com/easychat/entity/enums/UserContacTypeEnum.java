package com.easychat.entity.enums;

import com.easychat.utils.StringTools;

public enum UserContacTypeEnum {
    USER(0,"U","好友"),
    GROUP(1,"G","群");
    private Integer type;
    private String prefix;
    private String desc;
    UserContacTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }
    public Integer getType() {
        return type;
    }
    public String getPrefix() {
        return prefix;
    }
    public String getDesc() {
        return desc;
    }
    public static UserContacTypeEnum getByName(String name) {
        try{
            if(StringTools.isEmpty(name)){
                return null;
            }
            return UserContacTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }
    public static UserContacTypeEnum getByPrefix(String prefix) {
        try{
            if(StringTools.isEmpty(prefix)||prefix.trim().length()==0){
                return null;
            }
            prefix=prefix.substring(0,1);
            for(UserContacTypeEnum typeEnum:UserContacTypeEnum.values()){
                if(typeEnum.getPrefix().equals(prefix)){
                    return typeEnum;
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}
