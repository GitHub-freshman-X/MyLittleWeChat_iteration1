package com.easychat.entity.enums;

public enum GroupStatusEnum {
    NORMAL(1, "正常"),
    DISSOLUTION(0, "解散");

    private Integer status;
    private String desc;

    // 构造函数，初始化状态和描述
    GroupStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    // 根据状态获取枚举实例
    public static GroupStatusEnum getByStatus(Integer status) {
        for (GroupStatusEnum item : GroupStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    // 获取状态
    public Integer getStatus() {
        return status;
    }

    // 获取描述
    public String getDesc() {
        return desc;
    }
}