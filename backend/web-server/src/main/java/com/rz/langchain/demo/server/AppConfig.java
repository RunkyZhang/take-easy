package com.rz.langchain.demo.server;

import com.rz.langchain.demo.server.rag.BailianScoringModel;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${ai.model.bailian.codeplan.apiKey}")
    public String bailianApiKey;
    @Value("${ai.model.deepseek.apiKey}")
    public String deepseekApiKey;
    @Value("${ai.rag.withInMemoryEmbeddingStore.switch:false}")
    private boolean withInMemoryEmbeddingStore;

    // http debug：SyncRequestExecutor
    @Bean("qwen_3_5_plus")
    public OpenAiChatModel openAiChatModel_Qwen_3_5_plus() {
        return OpenAiChatModel.builder()
                .baseUrl("https://coding.dashscope.aliyuncs.com/v1")
                .apiKey(bailianApiKey)
                .modelName("qwen3.5-plus")
                .returnThinking(true)
                .timeout(Duration.ofSeconds(300))
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean("glm_5")
    public OpenAiChatModel openAiChatModel_Qlm_5() {
        return OpenAiChatModel.builder()
                .baseUrl("https://coding.dashscope.aliyuncs.com/v1")
                .apiKey(bailianApiKey)
                .modelName("glm-5")
                .returnThinking(true)
                .timeout(Duration.ofSeconds(300))
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean("deepSeek_v4_pro")
    public OpenAiChatModel openAiChatModel_DeepSeek_v4_pro() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(deepseekApiKey)
                .modelName("deepseek-v4-pro")
                .returnThinking(true)
                .timeout(Duration.ofSeconds(300))
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean("deepSeek_v4_flash")
    public OpenAiChatModel openAiChatModel_DeepSeek_v4_flash() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(deepseekApiKey)
                .modelName("deepseek-v4-flash")
                .returnThinking(true)
                .timeout(Duration.ofSeconds(300))
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://coding.dashscope.aliyuncs.com/v1")
                .apiKey(bailianApiKey)
                .modelName("qwen3.5-plus")
                .returnThinking(true)
                .timeout(Duration.ofSeconds(300))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public ChatMemory ChatMemory() {
        // memoryId: 一般被设计为sessionId
        return MessageWindowChatMemory.builder()
                .id(1111)
                .maxMessages(50)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore,
                                                         ChromaEmbeddingStore chromaEmbeddingStore,
                                                         EmbeddingModel embeddingModel) {
        // TokenCountEstimator：token用量估算器
        // recursive 方法实际上是一个高层封装，它将 DocumentByParagraphSplitter、DocumentByLineSplitter、DocumentBySentenceSplitter、DocumentByWordSplitter、DocumentByCharacterSplitter 串联成一个责任链，并自动配置好各自的 subSplitter。开发者无需手动构建分层结构，直接调用此方法即可获得最佳实践的分割器。
        // DocumentSplitters.recursive(...)
        // 按段落边界分割文档，保持段落的完整性。适用场景：适合结构化较好的文档，如文章、报告等。
        // DocumentByParagraphSplitter();
        // 按换行符分割文档，每行作为一个单元。。适用场景：适合代码文件、CSV 文件、日志文件等按行组织的内容。
        // DocumentByLineSplitter();
        // 按句子边界（句号、问号、感叹号等）分割文档。适用场景：需要保持句子完整性的场景，如问答系统、语义搜索。
        // DocumentBySentenceSplitter;
        // 按单词边界分割文档。适用场景：需要精细控制片段大小的场景，但可能破坏语义完整性。
        // DocumentByWordSplitter;
        // 最简单的分割方式，直接按字符数切割。适用场景：简单快速的分割，但可能在单词或句子中间切断
        // DocumentByCharacterSplitter;
        // 使用自定义正则表达式作为分隔符进行分割。适用场景：需要自定义分割规则的場景，如按章节、按特定标记分割。举例："(?m)^#\\s+"是按 Markdown 一级标题分割
        // DocumentByRegexSplitter;

        // TODO，注意：貌似maxOverlapSizeInChars不生效。下面是验证代码
//        String longString = "a".repeat(100);
//        longString += "b".repeat(100);
//        Document doc = Document.from(longString);
//        DocumentSplitter splitter = DocumentSplitters.recursive(100, 50);
//        List<TextSegment> segments = splitter.split(doc);
//        for(TextSegment seg : segments) {
//            System.out.println(seg.text().length() + " : " + seg.text().substring(0, Math.min(20, seg.text().length())));
//        }

        EmbeddingStore<TextSegment> embeddingStore = withInMemoryEmbeddingStore ? inMemoryEmbeddingStore : chromaEmbeddingStore;

        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    @Bean
    public InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    // 本地安装
    // 构建虚拟环境（创建一个文件夹）：python3 -m venv chroma
    // 激活虚拟环境（只在当前shell生效，不影响全局变量，下次使用还要在/chroma平级目录执行激活）：source chroma/bin/activate
    // 安装chroma（可能python3需要升级python3 -m pip install --upgrade pip）：pip install chromadb
    // 启动服务（只监听ipv6）：chroma run --path ./data --port 8000
    @Bean
    public ChromaEmbeddingStore chromaEmbeddingStore() {
        // 默认使用 ChromaApiVersion.V2
        // TODO注意：Chroma服务启动默认只监听ipv6。所以123.0.0.1:8000访问不了。localhost:8000也访问不了因为Java组件中默认使用ipv4导致localhost解析成123.0.0.1。
        //          所以自能使用ipv6的本地地址[::1]:8000
        return ChromaEmbeddingStore.builder()
                .apiVersion(ChromaApiVersion.V2)
                .baseUrl("http://[::1]:8000")
                .tenantName("default") // 如果不传默认租户名，则默认使用default。如果没有则创建default租户
                .databaseName("default") // 如果不传默认数据库名，则默认使用default。如果没有则创建default数据库
                .collectionName("default")
                .timeout(Duration.ofSeconds(5))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhV15QuantizedEmbeddingModel();
    }

    @Bean
    public ScoringModel scoringModel() {
        return new BailianScoringModel("qwen3-rerank", 5, null);
    }
}
