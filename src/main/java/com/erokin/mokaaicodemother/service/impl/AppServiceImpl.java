package com.erokin.mokaaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.erokin.mokaaicodemother.exception.BusinessException;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.exception.ThrowUtils;
import com.erokin.mokaaicodemother.model.dto.app.AppQueryRequest;
import com.erokin.mokaaicodemother.model.vo.AppVO;
import com.erokin.mokaaicodemother.service.AppService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.erokin.mokaaicodemother.model.entity.App;
import com.erokin.mokaaicodemother.mapper.AppMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "应用初始化提示词不能为空");
        }
        // 有参数则校验
        if (StrUtil.isNotBlank(appName) && appName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化提示词过长");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        queryWrapper.eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));

        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // 精选应用：优先级不为空且大于0
        queryWrapper.isNotNull("priority")
                .gt("priority", 0)
                .like("appName", appName)
                .orderBy(StrUtil.isNotBlank(sortField) ? sortField : "priority", "ascend".equals(sortOrder));

        return queryWrapper;
    }
}
