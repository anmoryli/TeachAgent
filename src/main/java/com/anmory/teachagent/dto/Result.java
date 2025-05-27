package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-26 下午9:42
 */

@Data
public class Result<T> {
    private String code;
    private String message;
    private T data;

    public Result(boolean b, String message, Object data) {
    }

    public Result() {
    }

    public static Result success(String message, Object data) {
        Result result = new Result();
        result.setCode(String.valueOf(ResultStatus.SUCCESS));
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static Result fail(String message, Object data) {
        Result result = new Result();
        result.setCode(String.valueOf(ResultStatus.FAIL));
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
