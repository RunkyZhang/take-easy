package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Chroma Embedding Metadata 实体类
 * 对应 chroma.sqlite3 数据库中的 embedding_metadata 表
 */
@Data
public class EmbeddingMetadataEntity implements Serializable {

    /**
     * 主键 ID
     */
    private int id;

    /**
     * 元数据键
     */
    private String key;

    /**
     * 元数据值
     */
    private String string_value;

    private Integer int_value;

    private Float float_value;

    private Boolean bool_value;
}
