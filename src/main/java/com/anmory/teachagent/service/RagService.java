package com.anmory.teachagent.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * @date 2025-05-22 上午8:46
 */

@Slf4j
@Service
public class RagService {
    private final String baseUrl = "http://127.0.0.1:8001";

    public String getRelevant(String question) throws IOException {
        log.info("RAG被调用，问题是:" + question);
        // 编码查询参数
        String encodedQuestion = URLEncoder.encode(question, StandardCharsets.UTF_8);
        String url = baseUrl + "/query?question=" + encodedQuestion;

        // 创建okhttp客户端
        OkHttpClient httpClient = new OkHttpClient();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        if(!response.isSuccessful()) {
            throw new IOException("请求失败，状态码: " + response);
        }

        // 获取响应体
        String responseBody = response.body().string();
        System.out.println("原始响应"+responseBody);

        // 使用gson解析json
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        JsonArray relevant = jsonResponse.getAsJsonArray("results");
        System.out.println("匹配结果"+relevant);

        // 只返回第一个匹配
        return relevant.get(0).getAsJsonObject().get("text").getAsString();
    }
}
