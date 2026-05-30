package com.rz.langchain.demo.server.mapper;

import com.rz.langchain.demo.server.dto.EmbeddingMetadataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Chroma Embedding Metadata Mapper
 * 用于访问 chroma.sqlite3 数据库中的 embedding_metadata 表
 */
@Mapper
public interface EmbeddingMetadataMapper {
    /**
     * 根据 ID 查询单条记录
     * @param id 记录 ID
     * @return 元数据对象
     */
    @Select("SELECT * FROM embedding_metadata WHERE id = #{id}")
    List<EmbeddingMetadataEntity> selectById(int id);

    @Select("select * from embedding_metadata where `key` = #{key} group by string_value")
    List<EmbeddingMetadataEntity> groupByKey(String key);

}
