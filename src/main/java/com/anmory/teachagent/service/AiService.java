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

import java.io.IOException;
import java.util.List;

/**
 * @author Anmory
 * @description RAG的过程应当在这里进行
 * @date 2025-05-10 下午3:36
 */

@Service
public class AiService {
    @Autowired
    OpenAiChatModel openAiChatModel;
    @Autowired
    PromptService promptService;
    @Autowired
    RagService ragService;
    @Autowired
    JudgePrommingService judgePrommingService;
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

    public String getQuestion(String questionType, String knowledgePoint, String prompt, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(2).getPromptText();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt("问题类型：" + questionType + "\n" + "知识点：" + knowledgePoint + "\n" + "请生成一个" + questionType + "类型的问题，关于" + knowledgePoint + "。" +
                        "你需要参考的知识:" + prompt)
                .system(systemPrompt)
                .advisors(messageChatMemoryAdvisor)
                .call()
                .content();
    }

    public String getReferenceAnswer(String question, HttpServletRequest request) throws IOException {
        String prompt = ragService.getRelevant(question);
        String finalQuestion = "问题：" + question + "\n" + "参考资料：" + prompt;
        User user = (User) request.getSession().getAttribute("session_user_key");
        String systemPrompt = promptService.selectByPromptId(3).getPromptText();
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
        return chatClient
                .prompt(finalQuestion)
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

    public String giveSuggest(int studentId, String question, String answer, String referenceAnswer, HttpServletRequest request) throws IOException, InterruptedException {
        String systemPrompt = promptService.selectByPromptId(6).getPromptText();
        User user = (User) request.getSession().getAttribute("session_user_key");
        String type = getQuestionType(question,request);
        if("编程题".equals(type)) {
            // 执行代码判断结果
            // TODO
            return judgePrommingService.judge(answer);
        }
        else {
            var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
            ChatClient chatClient = ChatClient.builder(openAiChatModel)
                    .defaultSystem(systemPrompt)
                    .defaultAdvisors(messageChatMemoryAdvisor)
                    .build();
            return chatClient
                    .prompt("学生id" + studentId + "问题：" + question + "\n" + "参考答案：" + referenceAnswer + "\n,学生的答案是" + answer + ",请给出建议")
                    .system(systemPrompt)
                    .advisors(messageChatMemoryAdvisor)
                    .call()
                    .content();
        }
    }

    public String giveSumAndTeachSuggest(List<Object> questions, HttpServletRequest request) {
        String systemPrompt = promptService.selectByPromptId(8).getPromptText();
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            return "请先登录";
        }
        var messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory,user.getCode(),10000);
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();

        return chatClient
                .prompt("你需要根据" + questions +"给出知识掌握情况总结和教学建议，并且给出的格式是纯文本的格式，将这两个方面分开给出，不能带有任何其他格式" +
                        "可以稍微使用一点markdown但不能太多。")
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
