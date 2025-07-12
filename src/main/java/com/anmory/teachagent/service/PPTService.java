package com.anmory.teachagent.service;

import com.anmory.teachagent.dto.Result;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-07-12 上午8:55
 */

@Slf4j
@Service
public class PPTService {
    private final String baseUrl = "http://127.0.0.1:8002";
    public Result<String> genPPT(String subject) throws IOException {
        String finalUrl = baseUrl + "/gen_ppt?subject=" + subject;

        // 创建okhttp客户端
        OkHttpClient httpClient = new OkHttpClient();
        // 构建请求体
        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .build();

        // 构建响应体
        Response response = httpClient.newCall(request).execute();

        if(!response.isSuccessful()) {
            throw new IOException("请求失败，状态码: " + response);
        }

        // 获取响应体
        String responseBody = response.body().string();
        System.out.println("原始响应"+responseBody);

        return Result.success("success", responseBody);
    }
}
