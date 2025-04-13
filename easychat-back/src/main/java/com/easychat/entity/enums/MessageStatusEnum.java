package com.easychat.entity.enums;

public enum MessageStatusEnum {
    SENDING(0,"发送中"),
    SENDED(1,"已发送");

    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum e : MessageStatusEnum.values()) {
            if(e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
