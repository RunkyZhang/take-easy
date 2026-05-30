package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class DocumentDto implements Serializable {
    private String id;
    private String text;
    private String name;
    private String type;
    private Map<String, Object> metadata;
    private final List<TextSegmentDto> textSegments = new ArrayList<>();
}
