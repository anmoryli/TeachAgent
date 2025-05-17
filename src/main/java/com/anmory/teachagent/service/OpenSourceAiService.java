package com.anmory.teachagent.service;

import com.anmory.teachagent.model.ChatMemory;
import com.anmory.teachagent.model.KnowledgeDocument;
import com.anmory.teachagent.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开源AI服务
 * 使用开源模型替代OpenAI，并集成本地知识库
 */
@Service
@Slf4j
public class OpenSourceAiService {
    @Autowired
    private PromptService promptService;
    
    @Autowired
    private ChatMemoryService chatMemoryService;
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    @Value("${chatglm.api-key}")
    private String apiKey;
    
    @Value("${chatglm.base-url}")
    private String baseUrl;
    
    @Value("${chatglm.model-name}")
    private String modelName;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 根据知识库生成课程计划
     * @param prompt 用户提示
     * @param request HTTP请求
     * @return 生成的课程计划
     */
    public String getLessonPlan(String prompt, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(1).getPromptText();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("session_user_key");
            
            List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(user.getUserId());
            String previousChatMemory = chatMemoryList.stream()
                .map(ChatMemory::getQuestionText)
                .collect(Collectors.joining("\n"));
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(prompt, 1, 5);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容生成课程计划：\n\n" + 
                knowledgeContext + "\n\n用户历史请求：\n" + previousChatMemory + 
                "\n\n当前请求：" + prompt;
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("生成课程计划失败", e);
            return "生成课程计划时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 根据知识库和课程内容生成问题
     * @param questionType 问题类型
     * @param knowledgePoint 知识点
     * @param request HTTP请求
     * @return 生成的问题
     */
    public String getQuestion(String questionType, String knowledgePoint, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(2).getPromptText();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("session_user_key");
            
            List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(user.getUserId());
            String previousChatMemory = chatMemoryList.stream()
                .map(ChatMemory::getQuestionText)
                .collect(Collectors.joining("\n"));
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(knowledgePoint, 1, 3);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容生成" + questionType + "类型的问题，关于" + knowledgePoint + "：\n\n" + 
                knowledgeContext + "\n\n用户历史请求：\n" + previousChatMemory;
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("生成问题失败", e);
            return "生成问题时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 生成参考答案
     * @param question 问题
     * @param request HTTP请求
     * @return 参考答案
     */
    public String getReferenceAnswer(String question, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(3).getPromptText();
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(question, 1, 3);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容为问题生成参考答案：\n\n" + 
                knowledgeContext + "\n\n问题：" + question;
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("生成参考答案失败", e);
            return "生成参考答案时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 生成学生问题的答案
     * @param question 问题
     * @param request HTTP请求
     * @return 答案
     */
    public String getStuAnswer(String question, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(4).getPromptText();
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(question, 1, 3);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容回答学生问题：\n\n" + 
                knowledgeContext + "\n\n问题：" + question;
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("生成学生答案失败", e);
            return "生成答案时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 判断学生答案
     * @param studentId 学生ID
     * @param question 问题
     * @param answer 学生答案
     * @param request HTTP请求
     * @return 判断结果
     */
    public String judgeAnswer(int studentId, String question, String answer, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(5).getPromptText();
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(question, 1, 2);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容判断学生答案：\n\n" + 
                knowledgeContext + "\n\n学生ID：" + studentId + 
                "\n问题：" + question + "\n学生答案：" + answer + 
                "\n请判断答案是否正确并给出建议";
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("判断答案失败", e);
            return "判断答案时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 给出学习建议
     * @param studentId 学生ID
     * @param question 问题
     * @param answer 学生答案
     * @param request HTTP请求
     * @return 学习建议
     */
    public String giveSuggest(int studentId, String question, String answer, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(6).getPromptText();
            
            List<KnowledgeDocument> relevantDocs = knowledgeBaseService.searchRelevantDocuments(question, 1, 2);
            String knowledgeContext = relevantDocs.stream()
                .map(KnowledgeDocument::getContent)
                .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = "基于以下知识库内容给学生提供学习建议：\n\n" + 
                knowledgeContext + "\n\n学生ID：" + studentId + 
                "\n问题：" + question + "\n学生答案：" + answer + 
                "\n请给出详细的学习建议";
            
            return callChatGLM(enhancedPrompt, systemPrompt);
        } catch (Exception e) {
            log.error("生成学习建议失败", e);
            return "生成学习建议时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 获取问题类型
     * @param question 问题
     * @param request HTTP请求
     * @return 问题类型
     */
    public String getQuestionType(String question, HttpServletRequest request) {
        try {
            String systemPrompt = promptService.selectByPromptId(7).getPromptText();
            
            return callChatGLM(question, systemPrompt);
        } catch (Exception e) {
            log.error("获取问题类型失败", e);
            return "获取问题类型时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 调用ChatGLM模型
     * @param prompt 用户提示
     * @param systemPrompt 系统提示
     * @return 模型响应
     */
    private String callChatGLM(String prompt, String systemPrompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String apiUrl = baseUrl + "/v1/chat/completions";
            HttpPost httpPost = new HttpPost(apiUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", prompt)
            );
            requestBody.put("messages", messages);
            
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseJson = EntityUtils.toString(response.getEntity());
                Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
                
                return "无法解析模型响应";
            }
        } catch (Exception e) {
            log.error("调用ChatGLM模型失败", e);
            return "调用模型时发生错误: " + e.getMessage();
        }
    }
}
