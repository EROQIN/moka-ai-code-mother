package com.erokin.mokaaicodemother.core.parser;

import com.erokin.mokaaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {
    // 在markdown中提取代码块中的html代码的正则表达式
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parserCode(String codeContent) {
            HtmlCodeResult result = new HtmlCodeResult();
            // 提取 HTML 代码
            String htmlCode = extractHtmlCode(codeContent);
            if (htmlCode != null && !htmlCode.trim().isEmpty()) {
                result.setHtmlCode(htmlCode.trim());
            } else {
                // 如果没有找到代码块，将整个内容作为HTML
                result.setHtmlCode(codeContent.trim());
            }
            return result;
    }

        /**
         * 提取HTML代码内容
         *
         * @param content 原始内容
         * @return HTML代码
         */
        private String extractHtmlCode(String content) {
            Matcher matcher = HTML_CODE_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
}
