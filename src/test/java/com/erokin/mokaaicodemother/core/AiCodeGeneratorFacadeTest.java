package com.erokin.mokaaicodemother.core;

import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateAndSaveHtmlCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个HTML界面，介绍你自己。", CodeGenTypeEnum.HTML);
        assertNotNull(file);
    }
    @Test
    void generateAndSaveMultiFileCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成制作一个日历小工具", CodeGenTypeEnum.MULTI_FILE);
        assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        assertNotNull(result);
        String completeContent = String.join("", result);
        assertNotNull(completeContent);
    }

    @Test
    void generateAndSaveHtmlCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个HTML界面，介绍你自己。", CodeGenTypeEnum.HTML);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        assertNotNull(result);
        String completeContent = String.join("", result);
        assertNotNull(completeContent);
    }



}