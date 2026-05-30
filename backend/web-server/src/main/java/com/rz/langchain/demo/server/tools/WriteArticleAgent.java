package com.rz.langchain.demo.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 创建一个AI作家。把AI Agent包装成一个工具。实现多Agent之间最简单交互
@Service
public class WriteArticleAgent {
    @Resource(name = "deepSeek_v4_pro")
    private OpenAiChatModel openAiChatModel;

    // 测试prompt：帮我写一个短文，内容是【父子游崂山】，风格是父子情深，长度是500字”，关键元素是回忆，夕阳，温情
    @Tool("这是一个AI作家Agent，最擅长的是些短片小故事。可以完成文本相关的创作。例如故事，作文，散文，小说，讲演稿，短文，文章，文档等")
    public String handoffWriteArticle(@P("用自然语言描述需要生成什么内容，包括主题、风格、长度、关键元素等要求") String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = SystemMessage.from("你是一个作家Agent，最擅长的是些短片小故事");
        ChatMessage userMessage = UserMessage.from(prompt);
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatResponse chatResponse = openAiChatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }
}
