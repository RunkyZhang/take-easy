package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChatMessagesDto implements Serializable {
    private String message;
    private List<String> imageBase64s;
    private String documentName;
    private boolean anthropicApiAdapter = false;
}
