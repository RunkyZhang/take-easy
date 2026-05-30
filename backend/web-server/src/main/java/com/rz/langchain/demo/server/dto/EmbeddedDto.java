package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedDto implements Serializable {
    private String text;
    private EmbeddedMetadataDto metadata;
}
