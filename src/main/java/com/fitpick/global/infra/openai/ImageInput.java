package com.fitpick.global.infra.openai;

public record ImageInput(
        String filename,
        String contentType,
        byte[] bytes
) {
}
