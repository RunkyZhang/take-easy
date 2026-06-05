/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rz.langchain.demo.server;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.rz.langchain.demo.server.dto.*;
import com.rz.langchain.demo.server.mapper.EmbeddingMetadataMapper;
import com.rz.langchain.demo.server.rpc.RpcProxy;
import com.rz.langchain.demo.server.tools.FormatAddressAgent;
import com.rz.langchain.demo.server.tools.ToolsSelector;
import dev.langchain4j.Experimental;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Slf4j
@Controller
public class BasicController {
    private static Map<String, DocumentDto> savedDocuments = new HashMap<>();

    @Value("${ai.rag.withInMemoryEmbeddingStore.switch:false}")
    private boolean withInMemoryEmbeddingStore;

    @Resource
    private RpcProxy rpcProxy;
    @Resource(name = "opencode_go_glm_5.1")
    private OpenAiChatModel openAiChatModel;
    @Resource(name = "opencode_go_qwen3.7_max")
    private AnthropicChatModel anthropicChatModel;
    @Resource
    private StreamingChatModel streamingChatModel;
    @Resource
    private ChatMemory chatMemory;
    @Resource
    private ToolsSelector toolsSelector;
    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;
    @Resource
    private InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore;
    @Resource
    private EmbeddingModel embeddingModel;
    @Resource
    private ScoringModel scoringModel;
    @Resource
    private EmbeddingMetadataMapper embeddingMetadataMapper;
    @Resource
    private ResourceLoader resourceLoader;

    // TODO：session隔离查看代码 /customerSupportAgent

    // http://localhost:8080/hello?value=介绍一下你自己
    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(value = "value", defaultValue = "介绍一下你自己") String value) {
//        String text =
//                "北京市（Beijing），简称“京”，古称燕京、北平，是中华人民共和国首都、直辖市、国家中心城市、超大城市， [185]国务院批复确定的中国政治中心、文化中心、国际交往中心、科技创新中心， [1]中国历史文化名城和古都之一，世界一线城市。 [3] [142] [188]截至2023年10月，北京市下辖16个区，总面积16410.54平方千米。 [82] [193] [195]2023年末，北京市常住人口2185.8万人。 [214-215]";
//        String query = "中国首都是哪座城市";
//        Response<Double> response = scoringModel.score(text, query);
//        System.out.println(response);

        String answer = "";
        answer = getAnswerWithJson(value);
        // answer = getAnswerWithSystemMessage(value);
        // answer = getAnswerWithTools(value);
        // answer = getAnswerWithMemory(value);
        // answer = getAnswerByStream();
        // answer = getAnswerByMessages();
        // answer = getAnswerByImage();
        // answer = getAnswerByImageBase64();
        // answer = getAnswerByMessage(value);
        // answer = getAnswer(value);

        return answer;
    }

    // 测试：给00545579发一条企业微信消息，内容是【厉害呀】
    // 测试：先帮我写一个短文，内容是【父子游崂山】，风格是父子情深，长度是500字”，关键元素是回忆，夕阳，温情。然后把短文内容通过企业微信发给00545579
    // http://localhost:8080/index.html
    @PostMapping("/chat")
    @ResponseBody
    public String chat(@RequestBody ChatMessagesDto requestDto) {
        Assert.notNull(requestDto, "Assert.notNull: requestDto");
        Assert.hasText(requestDto.getMessage(), "Assert.hasText: requestDto.getMessage()");

        ChatModel chatModel = requestDto.isAnthropicApiAdapter() ? anthropicChatModel : openAiChatModel;

        // 获取历史会话
        List<ChatMessage> chatMessages = chatMemory.messages();
        chatMessages.addAll(chatMemory.messages());

        // 创建用户消息
        List<Content> contents = new ArrayList<>();
        // 问题
        Content messageContent = TextContent.from(requestDto.getMessage());
        contents.add(messageContent);
        if (!CollectionUtils.isEmpty(requestDto.getImageBase64s())) {
            for (String imageBase64 : requestDto.getImageBase64s()) {
                ImageContent imageContent = ImageContent.from(imageBase64, "image/jpg");
                contents.add(imageContent);
            }
        }
        // Rag搜索结果
        if (!CollectionUtils.isEmpty(savedDocuments) && !StringUtils.isBlank(requestDto.getDocumentName())) {
            List<EmbeddingMatchDto> embeddingMatchDtos = ragSearch(requestDto.getMessage(), true, "all", requestDto.getDocumentName());
            if (!CollectionUtils.isEmpty(embeddingMatchDtos)) {
                String ragText = "这是知识库查询结果的json数据：\n";
                ragText += "========以下是json数据类型描述========\n";
                ragText += "EmbeddingMatchDto对象数组，每个对象包含以下字段：\n";
                ragText += "- score (Double): 相似度分数，范围0-1，越接近1表示匹配度越高\n";
                ragText += "- embeddingId (String): 向量ID，唯一标识符\n";
                ragText += "- text (String): 匹配的文本内容片段\n";
                ragText += "- metadata (EmbeddedMetadataDto): 元数据对象，包含以下字段：\n";
                ragText += "  - metadata (Map<String, Object>): 元数据键值对，包含document_id(文档ID)、name(名称)、type(内容类型)、url(网页地址)、本地路径(本地文件路径)等\n";
                ragText += "========以下是json数据========\n";
                ragText += JacksonHelper.toJson(embeddingMatchDtos, false);
                Content ragContent = TextContent.from(ragText);
                contents.add(ragContent);
            }
        }
        ChatMessage userMessage = UserMessage.from(contents);
        chatMessages.add(userMessage);
        // 存储
        chatMemory.add(userMessage);

        int times = 0;
        while (true) {
            // 调用模型
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(chatMessages)
                    .toolSpecifications(toolsSelector.getToolSpecifications())
                    .build();
            ChatResponse chatResponse = chatModel.chat(chatRequest);
            AiMessage aiMessage = chatResponse.aiMessage();
            chatMessages.add(aiMessage);
            // 存储
            chatMemory.add(aiMessage);

            if (CollectionUtils.isEmpty(aiMessage.toolExecutionRequests())) {
                return aiMessage.text();
            }

            // 限制和LLM交互次数
            times++;
            if (10 < times) {
                return "超过和LLM交互的最大次数。请重试！";
            }

            for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                Object tools = toolsSelector.getTool(toolExecutionRequest.name());
                if (null == tools) {
                    continue;
                }
                ToolExecutor toolExecutor = new DefaultToolExecutor(tools, toolExecutionRequest);
                String executorResult = toolExecutor.execute(toolExecutionRequest, UUID.randomUUID().toString());
                ToolExecutionResultMessage toolExecutionResultMessages =
                        ToolExecutionResultMessage.from(toolExecutionRequest, executorResult);
                chatMessages.add(toolExecutionResultMessages);
                // 存储
                chatMemory.add(toolExecutionResultMessages);
            }
        }
    }

    private String getAnswer(String value) {
        return openAiChatModel.chat(value);
    }

    private String getAnswerByMessage(String value) {
        ChatMessage userMessage = UserMessage.from(value);
        ChatResponse chatResponse = openAiChatModel.chat(userMessage);
        return chatResponse.aiMessage().text();
    }

    private String getAnswerWithSystemMessage(String value) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = SystemMessage.from("你是AI相关知识学的小助手，叫王二麻子。");
        ChatMessage userMessage = UserMessage.from(value);
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatResponse chatResponse = openAiChatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    // 多模态图片识别http body
    //{
    //  "model" : "qwen3.5-plus",
    //  "messages" : [ {
    //    "role" : "user",
    //    "content" : [ {
    //      "type" : "text",
    //      "text" : "看看这个图片内容是什么"
    //    }, {
    //      "type" : "image_url",
    //      "image_url" : {
    //        "url" : "https://c-ssl.duitang.com/uploads/item/202003/21/20200321005919_jfuql.jpg",
    //        "detail" : "low"
    //      }
    //    } ]
    //  } ],
    //  "stream" : false
    //}
    private String getAnswerByImage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("看看这个图片内容是什么"),
                ImageContent.from("https://c-ssl.duitang.com/uploads/item/202003/21/20200321005919_jfuql.jpg")
        );
        ChatResponse response = openAiChatModel.chat(userMessage);
        return response.aiMessage().text();
    }

    // 多模态图片识别。使用base64方式传图片
    private String getAnswerByImageBase64() {
        byte[] imageBytes = Utils.readBytes("https://c-ssl.duitang.com/uploads/item/202003/21/20200321005919_jfuql.jpg");
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        ImageContent imageContent = ImageContent.from(base64Data, "image/jpg");
        UserMessage userMessage = UserMessage.from(
                TextContent.from("看看这个图片内容是什么"),
                imageContent
        );
        ChatResponse response = openAiChatModel.chat(userMessage);
        return response.aiMessage().text();
    }

    // 多轮对话。给ai更多聊天上下文。后续可以使用聊天记忆功能替代。这只是个demo
    //{
    //  "model" : "qwen3.5-plus",
    //  "messages" : [ {
    //    "role" : "user",
    //    "content" : "你好，我是王二麻子。"
    //  }, {
    //    "role" : "assistant",
    //    "content" : "你好，王二麻子。我是大模型我有什么可以帮助你？"
    //  }, {
    //    "role" : "user",
    //    "content" : "你现在是一个咖啡专家。"
    //  }, {
    //    "role" : "assistant",
    //    "content" : "是的，我是你的咖啡专家。你可以提出咖啡相关问题。"
    //  }, {
    //    "role" : "user",
    //    "content" : "我现在在鞍山我适合喝什么咖啡？"
    //  } ],
    //  "stream" : false
    //}
    private String getAnswerByMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from("你好，我是王二麻子。"));
        messages.add(AiMessage.from("你好，王二麻子。我是大模型我有什么可以帮助你？"));
        messages.add(UserMessage.from("你现在是一个咖啡专家。"));
        messages.add(AiMessage.from("是的，我是你的咖啡专家。你可以提出咖啡相关问题。"));
        messages.add(UserMessage.from("我现在在鞍山我适合喝什么咖啡？"));

        ChatResponse response = openAiChatModel.chat(messages);
        return response.aiMessage().text();
    }

    // 聊天流式返回
    // {
    //  "model" : "qwen3.5-plus",
    //  "messages" : [ {
    //    "role" : "user",
    //    "content" : "现在你是一个咖啡专家，请回答我的问题。"
    //  } ],
    //  "stream" : true,
    //  "stream_options" : {
    //    "include_usage" : true
    //  }
    //}
    private String getAnswerByStream() {
        CompletableFuture<ChatResponse> completableFuture = new CompletableFuture<>();
        streamingChatModel.chat(
                List.of(UserMessage.from("现在你是一个咖啡专家，请回答我的问题。")),
                new StreamingChatResponseHandler() {
                    @Override
                    @Experimental
                    public void onPartialThinking(PartialThinking partialThinking) {
                        log.info("partialThinking: {}", partialThinking.text());
                    }

                    @Override
                    public void onPartialResponse(String partialResponse) {
                        log.info("partialResponse: {}", partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        completableFuture.complete(completeResponse);
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("error: {}", error);
                    }
                });

        try {
            return completableFuture.get().aiMessage().text();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAnswerWithMemory(String message) {
        List<ChatMessage> allChatMessages = chatMemory.messages();
        allChatMessages.addAll(chatMemory.messages());
        ChatMessage userMessage = UserMessage.from(message);
        allChatMessages.add(userMessage);
        AiMessage aiMessage = openAiChatModel.chat(allChatMessages).aiMessage();
        chatMemory.add(userMessage, aiMessage);

        return aiMessage.text();
    }

    private String getAnswerWithTools(String message) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage userMessage = UserMessage.from(message);
        chatMessages.add(userMessage);
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(userMessage)
                .toolSpecifications(toolsSelector.getToolSpecifications())
                .build();
        ChatResponse chatResponse = openAiChatModel.chat(chatRequest);
        AiMessage aiMessage = chatResponse.aiMessage();
        chatMessages.add(aiMessage);
        if (CollectionUtils.isEmpty(aiMessage.toolExecutionRequests())) {
            return aiMessage.text();
        }

        for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
            Object tools = toolsSelector.getTool(toolExecutionRequest.name());
            if (null == tools) {
                continue;
            }
            ToolExecutor toolExecutor = new DefaultToolExecutor(tools, toolExecutionRequest);
            String executorResult = toolExecutor.execute(toolExecutionRequest, UUID.randomUUID().toString());
            ToolExecutionResultMessage toolExecutionResultMessages =
                    ToolExecutionResultMessage.from(toolExecutionRequest, executorResult);

            chatMessages.add(toolExecutionResultMessages);
        }

        chatResponse = openAiChatModel.chat(chatMessages);
        return chatResponse.aiMessage().text();
    }

    // 测试prompt：解析以下地址：快递请邮寄到上海市浦东新区陆家嘴环路1000号恒生银行大厦18层，13688889999王五先生收就好了。电话记得要脱敏，如果人没接电话那么就把快递请放前台
    private String getAnswerWithJson(String message) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage userMessage = UserMessage.from(message);
        ChatMessage systemMessage = UserMessage.from(FormatAddressAgent.SYSTEM_PROMPT);
        chatMessages.add(systemMessage);
        chatMessages.add(userMessage);
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(chatMessages)
                .build();

        ChatResponse chatResponse = openAiChatModel.chat(chatRequest);
        return chatResponse.aiMessage().text();
    }

    // rag吸收
    @GetMapping("/ragIngest")
    @ResponseBody
    public Collection<DocumentDto> ragIngest() throws IOException {
        if (!CollectionUtils.isEmpty(savedDocuments)) {
            return savedDocuments.values();
        }

        // 最远的距离
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:The_farthest_distance.txt");
        Document txtDocument = FileSystemDocumentLoader.loadDocument(resource.getFile().toPath(), new TextDocumentParser());
        txtDocument.metadata().put("document_id", UUID.randomUUID().toString());
        txtDocument.metadata().put("type", "小说");
        txtDocument.metadata().put("name", "最远的距离");
        savedDocuments.put(txtDocument.metadata().getString("document_id"), toDocumentDto(txtDocument));

        // 短篇小说写作指南
        Document pdfDocument = FileSystemDocumentLoader.loadDocument("/tmp/短篇小说写作指南.pdf", new ApachePdfBoxDocumentParser());
        pdfDocument.metadata().put("document_id", UUID.randomUUID().toString());
        pdfDocument.metadata().put("type", "技能");
        pdfDocument.metadata().put("name", "短篇小说写作指南");
        savedDocuments.put(pdfDocument.metadata().getString("document_id"), toDocumentDto(pdfDocument));

        // 3年、1万人，快手技术团队首次系统披露AI研发范式升级历程
        Document urlDocument = UrlDocumentLoader.load("https://news.qq.com/rain/a/20260209A03PA600", new TextDocumentParser());
        HtmlToTextDocumentTransformer htmlToTextDocumentTransformer = new HtmlToTextDocumentTransformer();
        // 把html中的内容转换成纯文本
        Document webDocument = htmlToTextDocumentTransformer.transform(urlDocument);
        webDocument.metadata().put("document_id", UUID.randomUUID().toString());
        webDocument.metadata().put("type", "新闻");
        webDocument.metadata().put("name", "3年、1万人，快手技术团队首次系统披露AI研发范式升级历程");
        webDocument.metadata().put("core", "AI编程尝试");
        savedDocuments.put(webDocument.metadata().getString("document_id"), toDocumentDto(webDocument));

        // ApachePoiDocumentParser for office
        // ApacheTikaDocumentParser for default

        // 吸收，保存
        embeddingStoreIngestor.ingest(txtDocument, pdfDocument, webDocument);

        // format
        String entriesJson = inMemoryEmbeddingStore.serializeToJson();
        TextSegmentsDto textSegmentsDto = JacksonHelper.toObj(entriesJson, TextSegmentsDto.class, true);
        if (null == textSegmentsDto || null == textSegmentsDto.getEntries()) {
            return null;
        }
        for (TextSegmentDto textSegmentDto : textSegmentsDto.getEntries()) {
            if (null == textSegmentDto || null == textSegmentDto.getEmbedded() ||
                    null == textSegmentDto.getEmbedded().getMetadata() ||
                    CollectionUtils.isEmpty(textSegmentDto.getEmbedded().getMetadata().getMetadata())) {
                continue;
            }

            Object value = textSegmentDto.getEmbedded().getMetadata().getMetadata().get("document_id");
            String documentId = null == value ? "" : value.toString();
            DocumentDto documentDto = savedDocuments.get(documentId);
            if (null == documentDto) {
                continue;
            }

            documentDto.getTextSegments().add(textSegmentDto);
        }

        return savedDocuments.values();
    }

    // http://localhost:8080/ragSearch?value=林曦尝试过使用AI辅助编程去开发一个写短篇小说的App吗？
    // 查不出来：http://localhost:8080/ragSearch?value=林曦尝试过使用AI辅助编程去开发一个写短篇小说的App吗？&type=小说&name=短篇小说写作指南
    // 林曦都和谁谈过恋爱？
    @GetMapping("/ragSearch")
    @ResponseBody
    public List<EmbeddingMatchDto> ragSearch(@RequestParam(value = "value", defaultValue = "介绍一下林曦") String value,
                                             @RequestParam(value = "rerank", defaultValue = "true") boolean rerank,
                                             @RequestParam(value = "type", defaultValue = "all") String type,
                                             @RequestParam(value = "name", defaultValue = "all") String name) {
        // 条件
        Embedding queryEmbedding = embeddingModel.embed(value).content();
        Filter filter = null;
        Filter typeFilter = MetadataFilterBuilder.metadataKey("type").isEqualTo(type);
        Filter nameFilter = MetadataFilterBuilder.metadataKey("name").isEqualTo(name);
        if (!"all".equals(type) && !"all".equals(name)) {
            filter = Filter.and(typeFilter, nameFilter);
        } else if (!"all".equals(type)) {
            filter = typeFilter;
        } else if (!"all".equals(name)) {
            filter = nameFilter;
        }
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .maxResults(10)
                .minScore(0.5)
                .queryEmbedding(queryEmbedding)
                .filter(filter)
                .build();

        // 查询
        EmbeddingSearchResult<TextSegment> embeddingSearchResult = inMemoryEmbeddingStore.search(embeddingSearchRequest);

        // 评分
        List<Double> rerankScores = new ArrayList<>();
        if (rerank) {
            List<TextSegment> textSegments = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> embeddingMatch : embeddingSearchResult.matches()) {
                textSegments.add(null == embeddingMatch ? null : embeddingMatch.embedded());
            }

            if (!CollectionUtils.isEmpty(textSegments)) {
                try {
                    Response<List<Double>> response = this.scoringModel.scoreAll(textSegments, value);
                    rerankScores = response.content();
                } catch (Exception e) {
                    log.error("failed to rerank from bailian rerank model", e);
                }
            }
        }

        // 转换
        List<EmbeddingMatchDto> embeddingMatchDtos = new ArrayList<>();
        int index = -1;
        for (EmbeddingMatch<TextSegment> embeddingMatch : embeddingSearchResult.matches()) {
            index++;
            if (null == embeddingMatch) {
                continue;
            }

            // 获取rerank分数。如果rerank分数是0，则pass这条数据
            Double rerankScore = null;
            if (!CollectionUtils.isEmpty(rerankScores) && index < rerankScores.size()) {
                rerankScore = rerankScores.get(index);
                if (0 == rerankScore) {
                    continue;
                }
            }

            // 填充数据
            EmbeddingMatchDto embeddingMatchDto = new EmbeddingMatchDto();
            embeddingMatchDto.setScore(null == rerankScore ? embeddingMatch.score() : rerankScore);
            embeddingMatchDto.setEmbeddingId(embeddingMatch.embeddingId());
            embeddingMatchDtos.add(embeddingMatchDto);

            TextSegment textSegment = embeddingMatch.embedded();
            if (null == textSegment) {
                continue;
            }
            embeddingMatchDto.setText(textSegment.text());
            if (null == textSegment.metadata() || CollectionUtils.isEmpty(textSegment.metadata().toMap())) {
                continue;
            }
            EmbeddedMetadataDto embeddedMetadataDto = new EmbeddedMetadataDto();
            embeddingMatchDto.setMetadata(embeddedMetadataDto);
            embeddedMetadataDto.setMetadata(textSegment.metadata().toMap());
        }

        return embeddingMatchDtos;
    }

    // 词性标注
    // https://hanlp.hankcs.com/demos/pos.html
    @GetMapping("/nlp")
    @ResponseBody
    public List<String> ragSearch(@RequestParam(value = "value", defaultValue = "马云在杭州阿里巴巴总部发表了《杭州环保互联网化》的演讲。") String value) {
        List<String> result = new ArrayList<>();
        // 词性可以在com.hankcs.hanlp.corpus.tag.Nature注释中查看文定义
        // 频次是该词在com.hankcs.hanlp.HanLP.Config总定义的文件中出现的次数
        List<Term> termList = StandardTokenizer.segment(value);
        for (Term term : termList) {
            result.add(String.format("【%s】, 词性：%s，频次：%d", term.word, term.nature, term.getFrequency()));
        }

        return result;
    }

    // http://127.0.0.1:8080/html
    @RequestMapping("/html")
    public String html() {
        return "index.html";
    }

    private Map<String, DocumentDto> findDocuments() {
        Map<String, DocumentDto> savedDocuments = new HashMap<>();
        List<EmbeddingMetadataEntity> embeddingMetadataEntities = embeddingMetadataMapper.groupByKey("document_id");
        Set<Integer> documentIds = embeddingMetadataEntities.stream().filter(Objects::nonNull).map(EmbeddingMetadataEntity::getId).collect(Collectors.toSet());
        for (Integer documentId : documentIds) {
            embeddingMetadataEntities = embeddingMetadataMapper.selectById(documentId);
            if (CollectionUtils.isEmpty(embeddingMetadataEntities)) {
                continue;
            }

            DocumentDto documentDto = new DocumentDto();
            for (EmbeddingMetadataEntity embeddingMetadataEntity : embeddingMetadataEntities) {
                if (null == embeddingMetadataEntity) {
                    continue;
                }

                if ("name".equals(embeddingMetadataEntity.getKey())) {
                    documentDto.setName(embeddingMetadataEntity.getString_value());
                } else if ("type".equals(embeddingMetadataEntity.getKey())) {
                    documentDto.setType(embeddingMetadataEntity.getString_value());
                } else if ("document_id".equals(embeddingMetadataEntity.getKey())) {
                    documentDto.setId(embeddingMetadataEntity.getString_value());
                }
            }
            if (StringUtils.isBlank(documentDto.getId()) || StringUtils.isBlank(documentDto.getName())) {
                continue;
            }
            savedDocuments.put(documentDto.getId(), documentDto);
        }

        return savedDocuments;
    }

    private DocumentDto toDocumentDto(Document document) {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setText(document.text());
        String documentId = document.metadata().getString("document_id");
        if (StringUtils.isBlank(documentId)) {
            documentId = UUID.randomUUID().toString();
            document.metadata().put("document_id", documentId);
        }
        documentDto.setId(documentId);
        documentDto.setType(document.metadata().getString("type"));
        documentDto.setName(document.metadata().getString("name"));
        documentDto.setMetadata(null == document.metadata() ? new HashMap<>() : document.metadata().toMap());

        return documentDto;
    }
}
