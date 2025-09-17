package com.erokin.mokaaicodemother.core;

import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

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
}