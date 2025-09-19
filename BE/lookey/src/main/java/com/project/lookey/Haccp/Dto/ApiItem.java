package com.project.lookey.Haccp.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiItem {
    private String prdlstNm; // 상품명
    private String allergy;  // "난백, 대두" 또는 "없음"
    private String prdkindstate;
    private String manufacture;
    private String rnum;
    private String prdkind;
    private String rawmtrl;
    private String imgurl1;
    private String imgurl2;
    private String productGb;
    private String prdlstReportNo;
    private String seller; // 선택적
}
