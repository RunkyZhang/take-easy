package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WcImSendRequestDto implements Serializable {
    private String msgtype;
    private WcImSendTextContentDto text;

    private String robotKey;
}
