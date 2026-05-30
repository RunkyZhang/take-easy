package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class EmbeddedMetadataDto implements Serializable {
    private Map<String, Object> metadata;
}
