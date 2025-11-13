package com.erokin.mokaaicodemother.ai;

import com.erokin.mokaaicodemother.ai.model.HtmlCodeResult;
import com.erokin.mokaaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Test
    void generateHtmlCode() {
        HtmlCodeResult generatedHtmlCode1 = aiCodeGeneratorService.generateHtmlCode(1,"做个工具网站，总代码量不超过 20 行");
        HtmlCodeResult generatedHtmlCode2 = aiCodeGeneratorService.generateHtmlCode(1,"不要生成代码，告诉我你刚刚做了什么");
        assertNotNull(generatedHtmlCode1);
        assertNotNull(generatedHtmlCode2);
        HtmlCodeResult generatedHtmlCode3 = aiCodeGeneratorService.generateHtmlCode(2,"不要生成代码，我是Erokin");
        HtmlCodeResult generatedHtmlCode4 = aiCodeGeneratorService.generateHtmlCode(2,"我是谁？不要生成代码");
        assertNotNull(generatedHtmlCode3);
        assertNotNull(generatedHtmlCode4);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult generatedMultiFileCode = aiCodeGeneratorService.generateMultiFileCode("生成一个Erokin的工作记录小工具");
        assertNotNull(generatedMultiFileCode);
    }
}