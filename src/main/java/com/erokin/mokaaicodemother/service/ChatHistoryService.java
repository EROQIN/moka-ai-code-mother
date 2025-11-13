package com.erokin.mokaaicodemother.service;

import com.erokin.mokaaicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.erokin.mokaaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.erokin.mokaaicodemother.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息（便捷重载，无父消息）
     *
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 添加对话消息（带父消息 id）
     *
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId);

    /**
     * 按应用查询最新消息（支持游标向前加载），按 createTime 降序，最多 pageSize 条
     */
    java.util.List<ChatHistory> listLatestByApp(Long appId, int pageSize, java.time.LocalDateTime beforeTime);

    /**
     * 管理员分页查看所有应用的对话历史，按时间降序
     */
    com.mybatisflex.core.paginate.Page<ChatHistory> listAllByAdmin(int pageNum, int pageSize);

    /**
     * 关联删除某应用的全部对话历史
     */
    boolean deleteByAppId(Long appId);

    /**
     * 转换为 VO
     */
    com.erokin.mokaaicodemother.model.vo.ChatHistoryVO getChatHistoryVO(ChatHistory chat);

    /**
     * 批量转换为 VO 列表
     */
    java.util.List<com.erokin.mokaaicodemother.model.vo.ChatHistoryVO> getChatHistoryVOList(java.util.List<ChatHistory> list);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);


    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    //加载对话历史
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
