package com.example.springbootboilerplate.domain.sample.presentation;

import com.example.springbootboilerplate.global.common.code.ErrorCode;
import com.example.springbootboilerplate.global.common.code.GlobalErrorCode;
import com.example.springbootboilerplate.global.common.code.SuccessCode;
import com.example.springbootboilerplate.global.common.response.ApiResponse;
import com.example.springbootboilerplate.global.common.response.PageResponse;
import com.example.springbootboilerplate.global.exception.CustomException;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sample")
public class SampleController {

    @GetMapping
    public ApiResponse<Map<String, String>> sample() {
        return ApiResponse.success(SuccessCode.READ_SUCCESS, Map.of("hello", "boilerplate"));
    }

    @PostMapping
    public ApiResponse<Map<String, String>> validate(@RequestBody @Valid SampleRequest request) {
        return ApiResponse.success(SuccessCode.CREATED, Map.of("name", request.getName()));
    }

    @GetMapping("/error")
    public ApiResponse<Void> error() {
        throw new CustomException(GlobalErrorCode.INVALID_REQUEST);
    }

    /**
     * Pagination 응답 테스트용
     * 호출 예:
     * GET /api/v1/sample/page?page=0&size=5&sort=id, desc
     */
    @GetMapping("/page")
    public ApiResponse<PageResponse<Map<String, Object>>> page (@ParameterObject Pageable pageable) {

        // 1) 더미 데이터 30개 생성
        List<Map<String, Object>> all = java.util.stream.IntStream.rangeClosed(1, 30)
                .mapToObj(i -> Map.<String, Object>of("id", i, "name", "item-" + i))
                .toList();

        // 2) Pageable 기반으로 subList 잘라서 content 만들기
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Map<String, Object>> content = (start >= all.size()) ? List.of() : all.subList(start, end);

        // 3) Page 구현체로 감싸기
        Page<Map<String, Object>> page = new PageImpl<>(content, pageable, all.size());

        // 4) 공통 PageResponse로 변환해서 ApiResponse에 얹기
        return ApiResponse.success(SuccessCode.READ_SUCCESS, PageResponse.from(page));

    }
}
