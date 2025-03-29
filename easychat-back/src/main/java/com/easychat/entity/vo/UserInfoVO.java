package com.easychat.entity.vo;
import java.io.Serializable;

/**
 * 用户信息VO类
 */
public class UserInfoVO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:女 1:男
     */
    private Integer sex;

    /**
     * 加入类型
     */
    private Integer joinType;

    /**
     * 个性签名
     */
    private String personalSignature;


    private String areaCode;
    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 令牌
     */
    private String token;

    /**
     * 是否管理员
     */
    private Boolean admin;

    /**
     * 联系状态
     */
    private Integer contactStatus;

    // Getters and Setters

    public void setContactStatus(Integer contactStatus) {
        this.contactStatus = contactStatus;
    }

    public Integer getContactStatus() {
        return contactStatus;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
