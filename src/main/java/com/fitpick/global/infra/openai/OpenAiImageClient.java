package com.fitpick.global.infra.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Image Edits API (gpt-image-1) 클라이언트.
 * POST /v1/images/edits — multipart/form-data, 입력 이미지 다중은 field "image[]" 반복.
 * 응답: data[0].b64_json (gpt-image-1은 URL 미지원).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiImageClient {

    private static final String EDITS_PATH = "/v1/images/edits";

    private final RestClient openAiRestClient;
    private final OpenAiProperties props;

    public byte[] editImage(String prompt, ImageInput personImage, ImageInput productImage) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 설정되지 않았습니다.");
        }
        if (props.imageModel() == null || props.imageModel().isBlank()) {
            throw new IllegalStateException("app.openai.image-model 설정이 비어 있습니다.");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", props.imageModel());
        body.add("prompt", prompt);
        if (props.imageSize() != null && !props.imageSize().isBlank()) {
            body.add("size", props.imageSize());
        }
        if (props.imageQuality() != null && !props.imageQuality().isBlank()) {
            body.add("quality", props.imageQuality());
        }
        body.add("image[]", toResource(personImage));
        body.add("image[]", toResource(productImage));

        @SuppressWarnings("rawtypes")
        Map response = openAiRestClient.post()
                .uri(EDITS_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }
        Object dataObj = response.get("data");
        if (!(dataObj instanceof List<?> dataList) || dataList.isEmpty()) {
            throw new IllegalStateException("OpenAI 응답에 data 배열이 없습니다.");
        }
        Object first = dataList.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            throw new IllegalStateException("OpenAI 응답 data[0] 형식이 잘못되었습니다.");
        }
        Object b64 = firstMap.get("b64_json");
        if (!(b64 instanceof String b64Str) || b64Str.isBlank()) {
            throw new IllegalStateException("OpenAI 응답에 b64_json이 없습니다.");
        }
        return Base64.getDecoder().decode(b64Str);
    }

    private ByteArrayResource toResource(ImageInput input) {
        return new ByteArrayResource(input.bytes()) {
            @Override
            public String getFilename() {
                return input.filename();
            }
        };
    }
}
