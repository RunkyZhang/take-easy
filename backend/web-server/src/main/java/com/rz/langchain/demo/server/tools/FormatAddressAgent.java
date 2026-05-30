package com.rz.langchain.demo.server.tools;

import com.rz.langchain.demo.server.JacksonHelper;
import com.rz.langchain.demo.server.dto.AddressDto;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;

@Service
public class FormatAddressAgent {
    public static final String SYSTEM_PROMPT = """
            你是一个AI地址格式化Agent，最擅长的是把一段包含省市区，姓名，电话等信息的字符串格式化成指定json字符串
            EXAMPLE JSON OUTPUT（返回的json完全遵循以下格式）：
            {
                "country": "string类型；表示所在国家",
                "province": "string类型；表示省份",
                "city": "string类型；表示城市",
                "district": "string类型；表示区县",
                "street": "string类型；表示街道/详细地址包括门牌号",
                "name": "string类型；表示姓名",
                "phoneNo": "string类型；表示电话号码",
                "remark": "string类型；表示备注信息"
            }
            """;

    @Resource(name = "deepSeek_v4_flash")
    private OpenAiChatModel openAiChatModel;
    private ResponseFormat responseFormat;

    @PostConstruct
    private void init() {
        JsonSchemaElement jsonObjectSchema = JsonObjectSchema.builder()
                .addStringProperty("country")
                .addStringProperty("province")
                .addStringProperty("city")
                .addStringProperty("district")
                .addStringProperty("street")
                .addStringProperty("name")
                .addStringProperty("phoneNo")
                .addStringProperty("remark")
                .required("phoneNo")
                .build();

        this.responseFormat = ResponseFormat.builder()
                .type(JSON) // type 可以是 TEXT (默认) 或 JSON
                .jsonSchema(JsonSchema.builder()
                        .name("AddressDto") // OpenAI 需要为 schema 指定名称
                        .rootElement(jsonObjectSchema)
                        .build())
                .build();
    }

    // 测试地址字符串：快递请邮寄到上海市浦东新区陆家嘴环路1000号恒生银行大厦18层，13688889999王五先生收就好了。电话记得要脱敏，如果人没接电话那么就把快递请放前台，勿打电话
    @Tool("这是一个AI地址格式化Agent，最擅长的是把一段包含省市区，姓名，电话等信息的字符串格式化成指定json字符串")
    public AddressDto handoffFormatAddressString(@P("包含省市区，姓名，电话等信息的字符串") String addressString) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);
        ChatMessage userMessage = UserMessage.from(addressString);
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatRequest chatRequest = ChatRequest.builder()
                //.responseFormat(this.responseFormat) // 目前使用的几个模型无法识别该设置（不生效或直接报错）。故注释掉
                .messages(messages)
                .build();

        ChatResponse chatResponse = openAiChatModel.chat(chatRequest);
        String json = chatResponse.aiMessage().text();

        return JacksonHelper.toObj(json, AddressDto.class, true);
    }
}
