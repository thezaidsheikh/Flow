package com.project.flow.common.response;

public record ErrorDetails(
        String code,
        Object details) {
}
