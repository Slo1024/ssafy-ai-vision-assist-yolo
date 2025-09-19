//package com.project.lookey.Haccp.Dto;
//
//import lombok.Data;
//import java.util.List;
//
//@Data
//public class HaccpResponse {
//    private Header header;
//    private Body body;
//
//    @Data
//    public static class Header {
//        private String resultCode;
//        private String resultMessage;
//    }
//
//    @Data
//    public static class Body {
//        private int numOfRows;
//        private int pageNo;
//        private int totalCount;
//        private List<Item> items;
//    }
//
//    @Data
//    public static class Item {
//        private String PRDLST_NM;   // 제품명
//        private String ALLERGY;     // 알레르기 정보
//        private String BRAND;       // 브랜드명 (필요시)
//        private String PRDLST_DCNM; // 유형명
//        // 필요한 필드들 추가
//    }
//}
//
