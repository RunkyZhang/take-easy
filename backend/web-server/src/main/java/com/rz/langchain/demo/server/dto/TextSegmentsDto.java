package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TextSegmentsDto  implements Serializable {
    private List<TextSegmentDto> entries;
}
