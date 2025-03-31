package com.easychat.entity.enums;
import com.easychat.utils.StringTools;
/**
 * 枚举类，用于定义联合类型。
 */
public enum JoinTypeEnum {
    JOIN(0, "直接加入"),
    APPLY(1, "需要审核");

    private Integer type;
    private String desc;

    /**
     * 私有构造函数，用于创建枚举实例。
     * @param type 类型
     * @param desc 描述
     */
    JoinTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 获取类型。
     * @return 类型
     */
    public Integer getType() {return type;}

    /**
     * 获取描述。
     * @return 描述
     */
    public String getDesc() {return desc;}

    /**
     * 根据名称获取枚举实例。
     * @param name 名称
     * @return 枚举实例，如果不存在则返回null
     */
    public static JoinTypeEnum getByName(String name) {
        if (StringTools.isEmpty(name)) {
            return null;
        }
        try {
            return JoinTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 根据类型获取枚举实例。
     * @param joinType 类型
     * @return 枚举实例，如果不存在则返回null
     */
    public static JoinTypeEnum getByType(Integer joinType) {
        try{
            for (JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()) {
              if (joinTypeEnum.getType().equals(joinType)) {
                return joinTypeEnum;
              }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}