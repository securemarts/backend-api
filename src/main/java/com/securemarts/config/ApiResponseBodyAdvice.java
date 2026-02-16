package com.securemarts.config;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.common.dto.Meta;
import com.securemarts.common.dto.PageResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType() == org.springframework.http.ResponseEntity.class;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse) {
            return body;
        }
        if (body == null) {
            return ApiResponse.success(null);
        }
        if (body instanceof PageResponse) {
            PageResponse<?> pr = (PageResponse<?>) body;
            Meta meta = new Meta(pr.getPage(), pr.getSize(), pr.getTotalElements(), pr.getTotalPages());
            return ApiResponse.paginated(pr.getContent(), meta);
        }
        return ApiResponse.success(body);
    }
}
