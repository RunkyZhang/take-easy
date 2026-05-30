package com.rz.langchain.demo.server.rpc;

import com.rz.langchain.demo.server.dto.WcImResultDto;
import com.rz.langchain.demo.server.dto.WcImSendRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*
    #feign配置
    #自定义feignClient超时配置，third是name属性的值
    feign.client.config.weComIm.connect-timeout=1000
    feign.client.config.weComIm.read-timeout=1000
* */
@FeignClient(name = "weComIm", url = "${wecom.im.url:https://qyapi.weixin.qq.com}")
public interface WeComImFeignClient {
    @PostMapping(value = "/cgi-bin/webhook/send?key={key}", headers = "application/json;charset=UTF-8")
    WcImResultDto webhookSend(@RequestBody WcImSendRequestDto requestDto, @RequestParam("key") String key);
}
