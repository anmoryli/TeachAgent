package com.anmory.teachagent.service;

import com.anmory.teachagent.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    PromptService promptService;
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    public String getLessonPlan(String prompt, HttpServletRequest request,String conId) {
        String systemPrompt = promptService.selectByPromptId(1).getPromptText();
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,conId,10000);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt(prompt)
                .advisors(messageChatMemoryAdvisor)
                .system(systemPrompt)
                .call()
                .content();
    }

    public String getQuestion(String questionType, String knowledgePoint, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(2).getPromptText();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt("问题类型：" + questionType + "\n" + "知识点：" + knowledgePoint + "\n" + "请生成一个" + questionType + "类型的问题，关于" + knowledgePoint + "。")
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String getReferenceAnswer(String question, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        String systemPrompt = promptService.selectByPromptId(3).getPromptText();
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt(question)
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String getStuAnswer(String question, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        String systemPrompt = promptService.selectByPromptId(4).getPromptText();
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt(question)
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String judgeAnswer(int studentId, String question, String answer, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(5).getPromptText();
        User user = (User) request.getSession().getAttribute("session_user_key");
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt("学生id" + studentId + "问题：" + question + "\n" + "参考答案：" + answer + "\n" + "请判断参考答案并给出建议")
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String giveSuggest(int studentId, String question, String answer, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(6).getPromptText();
        User user = (User) request.getSession().getAttribute("session_user_key");
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt("学生id" + studentId + "问题：" + question + "\n" + "参考答案：" + answer + "\n" + "请给出建议")
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String getQuestionType(String q, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(7).getPromptText();
        User user = (User) request.getSession().getAttribute("session_user_key");
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt(q)
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }
}
