package com.wins.base.verifycode;

public enum VerifyEnum {
    SUCCES("succes","校验成功"),
    NULL("null","验证码失效"),
    ERROR("error","验证码不正确"),
    TIMED_OUT("timed_out","连接超时");

    private String key;
    private String name;

    private VerifyEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static VerifyEnum getInstanceByKey(String key) {
        for (VerifyEnum enumItem : VerifyEnum.values()) {
            enumItem.name();
            if (enumItem.getKey().equals(key)) {
                return enumItem;
            }
        }
        return null;
    }

    public static String getNameByKey(String key) {
        VerifyEnum enumItem = getInstanceByKey(key);
        return enumItem != null ? enumItem.getName() : null;
    }
}
