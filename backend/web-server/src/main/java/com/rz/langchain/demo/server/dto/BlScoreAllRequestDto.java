package com.rz.langchain.demo.server.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
public class BlScoreAllRequestDto implements Serializable {
    private String model;
    private List<String> documents;
    private String query;
    private int top_n;
    private String instruct = "Given a web search query, retrieve relevant passages that answer the query.";
}