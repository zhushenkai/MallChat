package com.abin.mallchat.custom.chat.controller;


import com.abin.mallchat.common.common.annotation.FrequencyControl;
import com.abin.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.abin.mallchat.common.common.domain.vo.response.ApiResult;
import com.abin.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.abin.mallchat.common.common.utils.RequestHolder;
import com.abin.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.abin.mallchat.common.user.service.cache.UserCache;
import com.abin.mallchat.custom.chat.domain.vo.request.*;
import com.abin.mallchat.custom.chat.domain.vo.response.*;
import com.abin.mallchat.custom.chat.service.ChatService;
import com.abin.mallchat.custom.user.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 群聊相关接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-03-19
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserCache userCache;

    @GetMapping("/public/room/page")
    @ApiOperation("会话列表")
    public ApiResult<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq request) {
        return ApiResult.success(chatService.getRoomPage(request, RequestHolder.get().getUid()));
    }

    @GetMapping("/public/member/page")
    @ApiOperation("群成员列表")
    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid CursorPageBaseReq request) {
//        black(request);
        CursorPageBaseResp<ChatMemberResp> memberPage = chatService.getMemberPage(request);
        filterBlackMember(memberPage);
        return ApiResult.success(memberPage);
    }

    @GetMapping("/public/member/page/v1")
    @ApiOperation("群成员列表/v1")
    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMemberRespV1>> getMemberPage1(@Valid CursorPageBaseReq request) {
        CursorPageBaseResp<ChatMemberResp> memberPage = chatService.getMemberPage(request);
        filterBlackMember(memberPage);
        List<ChatMemberRespV1> collect = memberPage.getList().stream().map(a -> {
            ChatMemberRespV1 v1 = new ChatMemberRespV1();
            BeanUtils.copyProperties(a, v1);
            return v1;
        }).collect(Collectors.toList());
        return ApiResult.success(CursorPageBaseResp.init(memberPage, collect));
    }

    @GetMapping("/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public ApiResult<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq chatMessageMemberReq) {
        return ApiResult.success(chatService.getMemberList(chatMessageMemberReq));
    }

    private void filterBlackMember(CursorPageBaseResp<ChatMemberResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getUid().toString()));
    }

    private Set<String> getBlackUidSet() {
        return userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
    }

    @GetMapping("public/member/statistic")
    @ApiOperation("群成员人数统计")
    public ApiResult<ChatMemberStatisticResp> getMemberStatistic() {
        return ApiResult.success(chatService.getMemberStatistic());
    }

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/public/msg/page/v1")
    @ApiOperation("消息列表/v1")
    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMessageRespV1>> getMsgPage(@Valid ChatMessagePageReq request) {
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        filterBlackMsg(msgPage);
        List<ChatMessageRespV1> collect = msgPage.getList().stream().map(a -> {
            ChatMessageRespV1 v1 = new ChatMessageRespV1();
            BeanUtils.copyProperties(a, v1);
            return v1;
        }).collect(Collectors.toList());
        return ApiResult.success(CursorPageBaseResp.init(msgPage, collect));
    }

    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMsgPage1(@Valid ChatMessagePageReq request) {
//        black(request);
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        filterBlackMsg(msgPage);
        return ApiResult.success(msgPage);
    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getFromUser().getUid().toString()));
        System.out.println(1);
    }

    @PostMapping("/msg")
    @ApiOperation("发送消息")
    @FrequencyControl(time = 5, count = 2, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 30, count = 5, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 60, count = 10, target = FrequencyControl.Target.UID)
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq request) {
        Long msgId = chatService.sendMsg(request, RequestHolder.get().getUid());
        //返回完整消息格式，方便前端展示
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    @FrequencyControl(time = 10, count = 5, target = FrequencyControl.Target.UID)
    public ApiResult<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq request) {
        chatService.setMsgMark(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    @FrequencyControl(time = 20, count = 3, target = FrequencyControl.Target.UID)
    public ApiResult<Void> recallMsg(@Valid @RequestBody ChatMessageBaseReq request) {
        chatService.recallMsg(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }
}

