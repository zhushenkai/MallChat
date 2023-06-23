package com.abin.mallchat.common.chat.domain.entity.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Description: 消息扩展属性
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-05-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageExtra implements Serializable {
    private static final long serialVersionUID = 1L;
    //url跳转链接
    private Map<String, String> urlTitleMap;
    //消息撤回详情
    private MsgRecall recall;
    //艾特的uid
    private List<Long> atUidList;
}
