package com.rz.langchain.demo.server.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WcImSendTextContentDto extends WcImSendContentDto {
    private String content;
}
