package com.anmory.teachagent.config;

import com.anmory.teachagent.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常对象
     * @return 响应实体
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // 判断是否是文件流接口（例如：/exportResource）
        boolean isFileDownloadRequest = isExportResourceRequest(e);

        if (isFileDownloadRequest) {
            // 文件下载接口发生异常时，不包装为 Result，直接返回 500 错误码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 其他接口正常返回 Result 格式
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("服务器异常", e.getMessage()));
    }

    /**
     * 判断异常是否发生在 /exportResource 请求中
     *
     * @param e 异常对象
     * @return 是否为文件导出请求中的异常
     */
    private boolean isExportResourceRequest(Exception e) {
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().contains("ExportController") &&
                    "exportResource".equals(element.getMethodName())) {
                return true;
            }
        }
        return false;
    }
}
