package com.erokin.mokaaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * appId
     */
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;



}
