package com.erokin.mokaaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.erokin.mokaaicodemother.ai.model.HtmlCodeResult;
import com.erokin.mokaaicodemother.ai.model.MultiFileCodeResult;
import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaver {

    // 文件存储路径
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HTML 模式下的代码结果
     * @param htmlCodeResult
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String htmlCode = htmlCodeResult.getHtmlCode();
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", htmlCode);
        return new File(baseDirPath);
    }

    /**
     * 保存多文件模式下的代码结果
     * @param multiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String htmlCode = multiFileCodeResult.getHtmlCode();
        String cssCode = multiFileCodeResult.getCssCode();
        String jsCode = multiFileCodeResult.getJsCode();
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", htmlCode);
        writeToFile(baseDirPath, "style.css", cssCode);
        writeToFile(baseDirPath, "script.js", jsCode);
        return new File(baseDirPath);
    }


    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存代码文件方法
     * @param baseDirPath
     * @param fileName
     * @param content
     */
    private static void writeToFile(String baseDirPath, String fileName, String content) {
        String filePath = baseDirPath + File.separator + fileName;
        FileUtil.writeUtf8String(content, filePath);
    }
}
