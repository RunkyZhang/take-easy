package com.rz.langchain.demo.server.tools;

import com.rz.langchain.demo.server.dto.WcImSendRequestDto;
import com.rz.langchain.demo.server.dto.WcImSendTextContentDto;
import com.rz.langchain.demo.server.rpc.RpcProxy;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class WcImTools {
    @Resource
    private RpcProxy rpcProxy;

    @Tool("给指定的用户发送企业微信消息，到固定的企业微信群")
    public void sendTextMessage(@P("消息内容，目前只能发送text消息") String content,
                                @P(value = "接收人id", required = false) String userId) {
        userId = userId == null ? "00545579" : userId;
        WcImSendRequestDto wcImSendRequestDto = new WcImSendRequestDto();
        wcImSendRequestDto.setRobotKey("02ed44a0-025f-4821-a10e-31d975e30c44");
        wcImSendRequestDto.setMsgtype("text");
        WcImSendTextContentDto wcImSendTextContentDto = new WcImSendTextContentDto();
        wcImSendTextContentDto.setContent(content);
        wcImSendTextContentDto.setMentioned_list(Collections.singletonList(userId));
        wcImSendRequestDto.setText(wcImSendTextContentDto);
        rpcProxy.wcImWebhookSend(wcImSendRequestDto);
    }

    @Tool("通过姓名获取企业微信userId，用于发送企业微信消息时作为传入userId参数使用")
    public String getUserIdByName(@P("用户姓名") String name) {
        if ("张仁杰".equals(name) || "仁杰".equals(name)) {
            return "00545579";
        }

        return null;
    }
}
