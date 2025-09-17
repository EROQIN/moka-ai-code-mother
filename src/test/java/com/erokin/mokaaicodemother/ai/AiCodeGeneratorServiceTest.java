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
        HtmlCodeResult generatedHtmlCode = aiCodeGeneratorService.generateHtmlCode("生成一个登录表单页面");
        assertNotNull(generatedHtmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult generatedMultiFileCode = aiCodeGeneratorService.generateMultiFileCode("生成一个Erokin的工作记录小工具");
        assertNotNull(generatedMultiFileCode);
    }
}