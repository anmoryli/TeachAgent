package com.anmory.teachagent.service;

import com.anmory.teachagent.model.ChatMemory;
import com.anmory.teachagent.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午3:36
 */

@Service
public class AiService {
    @Autowired
    OpenAiChatModel openAiChatModel;
    @Autowired
    ChatMemoryService chatMemoryService;
    @Autowired
    PromptService promptService;

    public String getLessonPlan(String prompt, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(1).getPromptText();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(user.getUserId());
        String previousChatMemory = "";
        for (ChatMemory chatMemory : chatMemoryList) {
            previousChatMemory += chatMemory.getQuestionText() + "\n";
        }
        prompt = previousChatMemory + prompt;
        return ChatClient.create(openAiChatModel)
                .prompt(prompt)
                .system(systemPrompt)
                .call()
                .content();
    }

    public String getQuestion(String questionType, String knowledgePoint, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(2).getPromptText();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(user.getUserId());
        String previousChatMemory = "";
        for (ChatMemory chatMemory : chatMemoryList) {
            previousChatMemory += chatMemory.getQuestionText() + "\n";
        }
        return ChatClient.create(openAiChatModel)
                .prompt("问题类型：" + questionType + "\n" + "知识点：" + knowledgePoint + "\n" + "请生成一个" + questionType + "类型的问题，关于" + knowledgePoint + "。")
                .system(systemPrompt)
                .call()
                .content();
    }

    public String getReferenceAnswer(String question, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(3).getPromptText();
        return ChatClient.create(openAiChatModel)
                .prompt(question)
                .system(systemPrompt)
                .call()
                .content();
    }
}
