package com.erokin.mokaaicodemother.core.parser;

import com.erokin.mokaaicodemother.exception.BusinessException;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;

public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MutiFileCodeParser mutiFileCodeParser = new MutiFileCodeParser();

    public static Object executeParser(String content,CodeGenTypeEnum codeGenTypeEnum){
        return switch(codeGenTypeEnum){
            case HTML -> htmlCodeParser.parserCode(content);
            case MULTI_FILE -> mutiFileCodeParser.parserCode(content);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenTypeEnum);
        };
    }


}
