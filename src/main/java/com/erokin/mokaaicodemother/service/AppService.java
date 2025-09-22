package com.erokin.mokaaicodemother.service;

import com.erokin.mokaaicodemother.model.dto.app.AppQueryRequest;
import com.erokin.mokaaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.erokin.mokaaicodemother.model.entity.App;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用
     *
     * @param app
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 分页获取应用封装
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest);
}
