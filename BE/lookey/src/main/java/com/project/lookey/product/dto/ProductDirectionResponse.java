package com.project.lookey.product.dto;

public class ProductDirectionResponse {

    public record Result(
            String caseType,
            Target target,
            Info info
    ) {}

    public record Target(
            String name,
            String directionBucket
    ) {}

    public record Info(
            String name,
            Integer price,
            String event,
            Boolean allergy
    ) {}
}