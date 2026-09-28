package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 规格项（含规格值） */
@Data
public class SpecVO implements Serializable {

    private Long id;
    private String name;
    private List<SpecValueVO> values = new ArrayList<>();

    @Data
    public static class SpecValueVO implements Serializable {
        private Long id;
        private String value;
    }
}
