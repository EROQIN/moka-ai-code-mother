package com.erokin.mokaaicodemother.core.saver;

import com.erokin.mokaaicodemother.ai.model.HtmlCodeResult;
import com.erokin.mokaaicodemother.ai.model.MultiFileCodeResult;
import com.erokin.mokaaicodemother.exception.BusinessException;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final MultFileCodeFileSaverTemplate multFileCodeFileSaverTemplate = new MultFileCodeFileSaverTemplate();

    /**
     * 执行保存器
     * @param codeResult
     * @param codeGenTypeEnum
     * @return
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenTypeEnum);
        };
    }


}
