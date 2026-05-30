package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TextSegmentDto implements Serializable {
    private String id;
    private EmbeddedDto embedded;
}
