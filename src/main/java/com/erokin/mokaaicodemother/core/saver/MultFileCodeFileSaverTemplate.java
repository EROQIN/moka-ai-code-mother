package com.erokin.mokaaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.erokin.mokaaicodemother.ai.model.MultiFileCodeResult;
import com.erokin.mokaaicodemother.exception.BusinessException;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;

public class MultFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        String htmlCode = result.getHtmlCode();
        String cssCode = result.getCssCode();
        String jsCode = result.getJsCode();
        writeToFile(baseDirPath, "index.html", htmlCode);
        writeToFile(baseDirPath, "style.css", cssCode);
        writeToFile(baseDirPath, "script.js", jsCode);
    }


    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }

    }
}
