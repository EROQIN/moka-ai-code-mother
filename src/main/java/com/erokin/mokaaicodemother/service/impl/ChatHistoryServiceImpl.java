package com.erokin.mokaaicodemother.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.erokin.mokaaicodemother.constant.UserConstant;
import com.erokin.mokaaicodemother.exception.ErrorCode;
import com.erokin.mokaaicodemother.exception.ThrowUtils;
import com.erokin.mokaaicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.erokin.mokaaicodemother.model.entity.App;
import com.erokin.mokaaicodemother.model.entity.User;
import com.erokin.mokaaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.erokin.mokaaicodemother.model.vo.ChatHistoryVO;
import com.erokin.mokaaicodemother.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.erokin.mokaaicodemother.model.entity.ChatHistory;
import com.erokin.mokaaicodemother.mapper.ChatHistoryMapper;
import com.erokin.mokaaicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        return addChatMessage(appId, message, messageType, userId, null);
    }

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chat = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .parentId(parentId)
                .build();
        this.save(chat);

        return false;
    }

    @Override
    public java.util.List<ChatHistory> listLatestByApp(Long appId, int pageSize, java.time.LocalDateTime beforeTime) {
        QueryWrapper qw = QueryWrapper.create()
                .eq("appId", appId);
        if (beforeTime != null) {
            qw.lt("createTime", beforeTime);
        }
        qw.orderBy("createTime", false)
          .limit(pageSize);
        return this.list(qw);
    }

    @Override
    public com.mybatisflex.core.paginate.Page<ChatHistory> listAllByAdmin(int pageNum, int pageSize) {
        QueryWrapper qw = QueryWrapper.create()
                .orderBy("createTime", false);
        return this.page(com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chat) {
        if (chat == null) {
            return null;
        }

        ChatHistoryVO vo = new ChatHistoryVO();
        vo.setId(chat.getId());
        vo.setMessage(chat.getMessage());
        vo.setMessageType(chat.getMessageType());
        vo.setAppId(chat.getAppId());
        vo.setUserId(chat.getUserId());
        vo.setParentId(chat.getParentId());
        vo.setCreateTime(chat.getCreateTime());
        vo.setUpdateTime(chat.getUpdateTime());
        return vo;
    }

    @Override
    public java.util.List<ChatHistoryVO> getChatHistoryVOList(java.util.List<ChatHistory> list) {
        java.util.List<ChatHistoryVO> result = new java.util.ArrayList<>();
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (ChatHistory chat : list) {
            result.add(getChatHistoryVO(chat));
        }
        return result;
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        QueryWrapper qw = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(qw);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    //加载对话历史
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount){
        try{
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("appId", appId)
                    .orderBy("createTime", false)
                    .limit(1,maxCount)
                    ;
            List<ChatHistory> chatHistoryList = this.list(queryWrapper);
            if(CollectionUtil.isEmpty(chatHistoryList)){
                return 0;
            }
            //反转列表，让旧的信息在前
            chatHistoryList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory chatHistory : chatHistoryList) {
                if(chatHistory.getMessageType()
                        .equals(ChatHistoryMessageTypeEnum.USER.getValue())){
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                    ++loadedCount;
                }
                if(chatHistory.getMessageType()
                        .equals(ChatHistoryMessageTypeEnum.AI.getValue())){
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                    ++loadedCount;
                }

            }
            log.info("成功对appId：{}加载了{}条对话历史",appId,loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载对话历史失败：appId:{} error:{}",appId,e.getMessage());
            return 0;
        }
    }



}
