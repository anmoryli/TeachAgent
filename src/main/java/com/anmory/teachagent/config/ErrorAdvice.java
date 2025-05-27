package com.anmory.teachagent.config;

import com.anmory.teachagent.dto.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-26 下午10:08
 */

@ControllerAdvice
@ResponseBody
public class ErrorAdvice {
    @ExceptionHandler
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail("服务器异常", e.getMessage());
    }

    @ExceptionHandler
    public Result error(Throwable e) {
        e.printStackTrace();
        return Result.fail("服务器异常", e.getMessage());
    }

    @ExceptionHandler
    public Result error(NullPointerException e) {
        e.printStackTrace();
        return Result.fail("空指针异常", e.getMessage());
    }
}
