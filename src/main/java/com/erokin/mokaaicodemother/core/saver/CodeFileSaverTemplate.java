package com.erokin.mokaaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.erokin.mokaaicodemother.exception.BusinessException;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public abstract class CodeFileSaverTemplate<T> {
    // 文件存储路径
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法
     * @param result 要保存的结果对象(enums)
     * @return 保存后的目录对象
     */
    public final File saveCode(T result){
        //1.验证参数
        validateInput(result);
        //2.构建唯一目录
        String baseDirPath = buildUniqueDir(result.getClass().getSimpleName());
        //3.保存文件(具体交给子类实现)
        saveFiles(result, baseDirPath);
        //4.返回保存后的目录对象
        return new File(baseDirPath);

    }


    /**
     * 验证参数(可由子类覆盖)
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码结果对象不能为空");
        }
    }


    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     * 不由子类实现
     */
     protected final String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }



    /**
     * 写入单个文件的工具
     * @param baseDirPath
     * @param fileName
     * @param content
     */
    protected final void writeToFile(String baseDirPath, String fileName, String content) {
        String filePath = baseDirPath + File.separator + fileName;
        FileUtil.writeUtf8String(content, filePath);
    }



    /**
     * 获取代码类型（由子类实现）
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();
    /**
     * 保存文件(由子类实现)
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);

}
