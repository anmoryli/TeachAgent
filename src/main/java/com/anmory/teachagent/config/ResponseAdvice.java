//package com.anmory.teachagent.config;
//
//import com.anmory.teachagent.dto.Result;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//@Slf4j
//@ControllerAdvice
//public class ResponseAdvice implements ResponseBodyAdvice {
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @Override
//    public boolean supports(MethodParameter returnType, Class converterType) {
//        // 避免包装 byte[] 返回值，比如文件下载
//        return !byte[].class.isAssignableFrom(returnType.getParameterType());
//    }
//
//    @Override
//    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
//                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//        if (body instanceof Result) {
//            log.info( "返回结果: {}", body);
//            return body;
//        }
//        if (body instanceof String) {
//            try {
//                return objectMapper.writeValueAsString(Result.success("", body));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return Result.success("", body);
//    }
//}