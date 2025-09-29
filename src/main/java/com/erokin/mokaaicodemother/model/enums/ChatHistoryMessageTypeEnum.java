package com.erokin.mokaaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息类型枚举
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Getter
public enum ChatHistoryMessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI消息", "ai");

    private final String description;

    private final String value;

    private static final Map<String, ChatHistoryMessageTypeEnum> VALUE_MAP = new HashMap<>();

    static {
        for (ChatHistoryMessageTypeEnum anEnum : ChatHistoryMessageTypeEnum.values()) {
            VALUE_MAP.put(anEnum.value, anEnum);
        }
    }

    ChatHistoryMessageTypeEnum(String description, String value) {
        this.description = description;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ChatHistoryMessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}