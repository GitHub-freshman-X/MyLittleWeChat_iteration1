package com.easychat.entity.enums;

public enum BeautyAccountStatusEnum {
    No_USE(0,"未使用"),
    USEED(1,"已使用");

    private Integer status;
    private String decs;

    BeautyAccountStatusEnum(Integer status, String decs) {
        this.status = status;
        this.decs = decs;
    }

    public static BeautyAccountStatusEnum getByStatus(Integer status) {
        for(BeautyAccountStatusEnum item : BeautyAccountStatusEnum.values()) {
            if(item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }
    public String getDecs() {
        return decs;
    }

    public void setStatus(String decs) {this.decs = decs;}
}
