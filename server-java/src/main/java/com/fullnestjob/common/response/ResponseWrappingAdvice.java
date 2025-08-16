package com.fullnestjob.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseWrappingAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        int status = 200;
        if (response instanceof ServletServerHttpResponse servletResponse) {
            status = servletResponse.getServletResponse().getStatus();
        }

        Message messageAnn = returnType.getMethodAnnotation(Message.class);
        String message = messageAnn != null ? messageAnn.value() : "";

        // Do not wrap file/binary streaming responses
        if (body instanceof org.springframework.core.io.Resource) {
            return body;
        }

        // Also skip wrapping if content type indicates binary/image
        if (selectedContentType != null && (
                MediaType.APPLICATION_OCTET_STREAM.includes(selectedContentType)
                        || MediaType.IMAGE_JPEG.includes(selectedContentType)
                        || MediaType.IMAGE_PNG.includes(selectedContentType)
                        || MediaType.IMAGE_GIF.includes(selectedContentType)
        )) {
            return body;
        }

        if (body instanceof ApiResponse<?>) {
            return body;
        }

        return new ApiResponse<>(body, status, message);
    }
}


