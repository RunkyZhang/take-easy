package com.rz.langchain.demo.server.rpc;

import com.rz.langchain.demo.server.dto.BlScoreAllRequestDto;
import com.rz.langchain.demo.server.dto.BlScoreAllResultDto;
import com.rz.langchain.demo.server.dto.WcImResultDto;
import com.rz.langchain.demo.server.dto.WcImSendRequestDto;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class RpcProxy {
    @Value("${ai.model.bailian.apiKey}")
    private String bailianApiKey;

    @Resource
    private WeComImFeignClient weComImFeignClient;
    @Resource
    private BailianFeignClient bailianFeignClient;

    public BlScoreAllResultDto blScoreAll(List<String> documents, String query, String modelName, int topN, String instruct) {
        Assert.notEmpty(documents, "Assert.notEmpty: documents");
        Assert.hasText(query, "Assert.hasText: query");
        if (topN <= 0) {
            topN = 5;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bailianApiKey);
        headers.add("Content-Type", "application/json");

        BlScoreAllRequestDto requestDto = new BlScoreAllRequestDto();
        requestDto.setDocuments(documents);
        requestDto.setQuery(query);
        requestDto.setModel(modelName);
        requestDto.setTop_n(topN);
        requestDto.setInstruct(instruct);

        BlScoreAllResultDto result = bailianFeignClient.scoreAll(headers, requestDto);
        if (null == result) {
            throw new RuntimeException("result is null");
        }
        if (null != result.getCode()) {
            throw new RuntimeException(result.getCode() + "; " + result.getMessage());
        }

        return result;
    }

    public void wcImWebhookSend(WcImSendRequestDto wcImSendRequestDto) {
        Assert.notNull(wcImSendRequestDto, "Assert.notNull: wcImSendRequestDto");

        WcImResultDto result = weComImFeignClient.webhookSend(wcImSendRequestDto, wcImSendRequestDto.getRobotKey());
        if (null == result) {
            throw new RuntimeException("-1; result is null");
        }
        if (result.getErrcode() != WcImResultDto.SUCCESS_CODE) {
            throw new RuntimeException(result.getErrcode() + "; " + result.getErrmsg());
        }
    }
}
