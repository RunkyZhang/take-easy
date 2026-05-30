package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BlResultDto implements Serializable {
    private String model;
    private String id;
    // list
    private String object;
    private Usage usage;

    private String code;
    private String message;
    private String request_id;

    @Data
    public static class Usage {
        private Integer total_tokens;
    }
}