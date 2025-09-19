package com.project.lookey.Haccp.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private Body body;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private List<ItemWrapper> items;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ItemWrapper {
            private ApiItem item;
        }
    }
}
