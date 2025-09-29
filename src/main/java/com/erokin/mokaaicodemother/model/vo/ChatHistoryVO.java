package com.erokin.mokaaicodemother.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史 VO
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Data
public class ChatHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String message;

    /**
     * user / ai
     */
    private String messageType;

    private Long appId;

    private Long userId;

    private Long parentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}