package com.erokin.mokaaicodemother.core.parser;

/**
 * 代码解析器策略接口
 * @param <T>
 */
public interface CodeParser<T> {
    /**
     * 解析代码
     * @param codeContent 代码内容
     * @return
     */
    T parserCode(String codeContent);
}
