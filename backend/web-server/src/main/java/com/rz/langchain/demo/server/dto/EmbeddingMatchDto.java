package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddingMatchDto implements Serializable {
    // rag的分数
    private Double score;
    // rag的id
    private String embeddingId;
    // rag的文本
    private String text;
    // rag的元数据。包括document_id，name（名称），type（内容类型），url（网页），本地路径（本地文件）
    private EmbeddedMetadataDto metadata;
}
