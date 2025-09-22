package com.erokin.mokaaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员应用更新请求
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Data
public class AppUpdateByAdminRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}