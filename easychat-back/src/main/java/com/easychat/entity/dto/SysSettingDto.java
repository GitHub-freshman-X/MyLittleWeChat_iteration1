package com.easychat.entity.dto;

import com.easychat.entity.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {
    private static final long serialVersionUID = -4052701002357141142L;
    //最大群组数
    private Integer maxGroupCount = 5;
    //群组最大人数
    private Integer maxGroupMemberCount = 500;
    //图片大小
    private Integer maxImageSize = 1024;
    //视频大小
    private Integer maxVideoSize = 1024;
    //文件大小
    private Integer maxFileSize = 1024;
    //文件大小
    private String robotUid = Constants.ROBOT_UID;
    private String robotNickName = "EasyChat";
    private String robotWelcome = "欢迎使用EasyChat";

    public Integer getMaxGroupCount() {
        return maxGroupCount;
    }

    public void setMaxGroupCount(Integer maxGroupCount) {
        this.maxGroupCount = maxGroupCount;
    }

    public Integer getMaxGroupMemberCount() {
        return maxGroupMemberCount;
    }

    public void setMaxGroupMemberCount(Integer maxGroupMemberCount) {
        this.maxGroupMemberCount = maxGroupMemberCount;
    }

    public Integer getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(Integer maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public Integer getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(Integer maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }


    public String getRobotUid() {
        return robotUid;
    }

    public void setRobotUid(String robotUid) {
        this.robotUid = robotUid;
    }


    public String getRobotNickName() {
        return robotNickName;
    }

    public void setRobotNickName(String robotNickName) {
        this.robotNickName = robotNickName;
    }


    public String getRobotWelcome() {
        return robotWelcome;
    }

    public void setRobotWelcome(String robotWelcome) {
        this.robotWelcome = robotWelcome;
    }
}
