package com.erokin.mokaaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CodeGenTypeEnum {

    HTML("原生 HTML 模式", "HTML"),
    MULTI_FILE("原生多文件模式", "MULTI_FILE");

    private final String description;

    private final String value;

    private static final Map<String, CodeGenTypeEnum> VALUE_MAP = new HashMap<>();

    static {
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            VALUE_MAP.put(anEnum.value, anEnum);
        }
    }

    CodeGenTypeEnum(String description, String value) {
        this.description = description;
        this.value = value;
    }


    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}
