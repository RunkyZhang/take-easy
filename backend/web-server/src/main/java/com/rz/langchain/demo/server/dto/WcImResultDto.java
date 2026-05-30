package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WcImResultDto implements Serializable {
    public static final int SUCCESS_CODE = 0;

    private int errcode;
    private String errmsg;
}