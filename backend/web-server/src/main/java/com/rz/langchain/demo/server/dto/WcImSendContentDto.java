package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public abstract class WcImSendContentDto implements Serializable {
    private List<String> mentioned_list;
    private List<String> mentioned_mobile_list;
}
