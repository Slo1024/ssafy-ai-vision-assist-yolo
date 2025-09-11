package com.project.lookey.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodSafetyResponse(
        @JsonIgnoreProperties(ignoreUnknown = true) C002 C002
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record C002(
            Integer total_count,
            List<Row> row,
            Result RESULT   // ★ 추가: 코드/메시지
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Row(
            String PRDLST_NM,   // 품목명
            String BSSH_NM,     // 업소명
            String RAWMTRL_NM   // 원재료 문자열
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            String CODE,        // 예: "INFO-000" (정상), "INFO-200"(결과없음) 등
            String MSG          // 메시지
    ) {}
}
