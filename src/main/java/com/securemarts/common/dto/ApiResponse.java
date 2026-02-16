package com.securemarts.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Unified API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Whether the request succeeded")
    private boolean success;

    @Schema(description = "Optional success or info message")
    private String message;

    @Schema(description = "Response payload (success)")
    private T data;

    @Schema(description = "Error details (when success is false)")
    private ApiResponseError error;

    @Schema(description = "Pagination metadata (for paginated lists)")
    private Meta meta;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage(message);
        res.setData(data);
        return res;
    }

    public static ApiResponse<?> error(String code, String message) {
        ApiResponse<Object> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setError(new ApiResponseError(code, message));
        return res;
    }

    public static <T> ApiResponse<T> paginated(T data, Meta meta) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(data);
        res.setMeta(meta);
        return res;
    }
}
