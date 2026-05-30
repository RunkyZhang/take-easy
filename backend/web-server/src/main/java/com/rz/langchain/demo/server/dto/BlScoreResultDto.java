package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BlScoreResultDto implements Serializable {
    private int index;
    private double relevance_score;
}