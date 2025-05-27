package com.anmory.teachagent.config;

import com.anmory.teachagent.dto.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-26 下午9:45
 */

@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice {
    @Autowired
    ObjectMapper objectMapper;
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 返回更加灵活
        if(body instanceof Result) {
            return body;
        }
        if(body instanceof String) {
            try {
                return objectMapper.writeValueAsString(Result.success("",body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return Result.fail("",body);
    }
}
