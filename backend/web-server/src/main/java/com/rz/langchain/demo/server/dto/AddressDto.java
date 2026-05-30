package com.rz.langchain.demo.server.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddressDto implements Serializable {
    // 国家
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 街道/详细地址
     */
    private String street;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String phoneNo;

    /**
     * 备注
     */
    private String remark;
}
