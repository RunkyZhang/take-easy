package com.rz.langchain.demo.server.rpc;

import com.rz.langchain.demo.server.dto.BlScoreAllRequestDto;
import com.rz.langchain.demo.server.dto.BlScoreAllResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/*
    #feign配置
    #自定义feignClient超时配置，third是name属性的值
    feign.client.config.bailian.connect-timeout=1000
    feign.client.config.bailian.read-timeout=1000
* */
@FeignClient(name = "bailian", url = "${bailian.url:https://dashscope.aliyuncs.com}")
public interface BailianFeignClient {
    @PostMapping(value = "/compatible-api/v1/reranks")
    BlScoreAllResultDto scoreAll(@RequestHeader HttpHeaders headers, @RequestBody BlScoreAllRequestDto requestDto);
}
