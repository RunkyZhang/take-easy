package com.rz.langchain.demo.server.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BlScoreAllResultDto extends BlResultDto {
    private List<BlScoreResultDto> results;
}