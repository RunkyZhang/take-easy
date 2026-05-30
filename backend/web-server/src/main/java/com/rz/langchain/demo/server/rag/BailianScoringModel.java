package com.rz.langchain.demo.server.rag;

import com.rz.langchain.demo.server.dto.BlScoreAllResultDto;
import com.rz.langchain.demo.server.dto.BlScoreResultDto;
import com.rz.langchain.demo.server.rpc.RpcProxy;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.scoring.ScoringModel;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class BailianScoringModel implements ScoringModel {
    private final String modelName;
    private final int topN;
    private final String instruct;

    @Resource
    private RpcProxy rpcProxy;

    public BailianScoringModel(String modelName, int topN, String instruct) {
        this.modelName = StringUtils.isBlank(modelName) ? "qwen3-rerank" : modelName;
        this.topN = 0 >= topN ? 5 : topN;
        this.instruct = StringUtils.isBlank(instruct) ? "Given a web search query, retrieve relevant passages that answer the query." : instruct;
    }

    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        Assert.notEmpty(segments, "Assert.notEmpty: segments");
        Assert.hasText(query, "Assert.hasText: query");

        // 初始化所有分数为0
        List<Double> scores = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            scores.add(0.0);
        }

        // 提取segments中的文本内容
        boolean hasValidDocument = false;
        List<String> documents = new ArrayList<>();
        for (TextSegment textSegment : segments) {
            if (null == textSegment || StringUtils.isBlank(textSegment.text())) {
                documents.add("");
                continue;
            }

            hasValidDocument = true;
            documents.add(textSegment.text());
        }
        if (!hasValidDocument) {
            return new Response<>(scores, null, FinishReason.STOP);
        }

        // 调用RPC接口获取评分结果
        BlScoreAllResultDto blScoreAllResultDto = rpcProxy.blScoreAll(documents, query, modelName, topN, instruct);
        TokenUsage tokenUsage = new TokenUsage(0, 0, null == blScoreAllResultDto.getUsage() ? 0 : blScoreAllResultDto.getUsage().getTotal_tokens());
        if (CollectionUtils.isEmpty(blScoreAllResultDto.getResults())) {
            return new Response<>(scores, tokenUsage, FinishReason.STOP);
        }

        // 根据index填充对应的分数
        for (BlScoreResultDto blScoreResultDto : blScoreAllResultDto.getResults()) {
            if (null == blScoreResultDto) {
                continue;
            }

            int index = blScoreResultDto.getIndex();
            // 确保index在有效范围内
            if (index >= 0 && index < scores.size()) {
                scores.set(index, blScoreResultDto.getRelevance_score());
            }
        }

        return new Response<>(scores, tokenUsage, FinishReason.STOP);
    }
}
