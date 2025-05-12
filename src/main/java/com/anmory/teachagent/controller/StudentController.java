package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.ChatMemory;
import com.anmory.teachagent.model.PracticeRecord;
import com.anmory.teachagent.model.Question;
import com.anmory.teachagent.model.User;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-11 上午10:31
 */

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    ChatMemoryService chatMemoryService;
    @Autowired
    CourseService courseService;
    @Autowired
    AnswerService answerService;
    @Autowired
    AiService aiService;
    @Autowired
    LessonPlanService lessonPlanService;
    @Autowired
    QuestionService questionService;
    @Autowired
    PracticeRecordService practiceRecordService;
    @RequestMapping("/askQuestion")
    public String askQuestion(String courseName, String questionText, HttpServletRequest request) {
        // 获取用户ID
        User user = (User) request.getSession().getAttribute("session_user_key");
        int userId = user.getUserId();
        // 获取用户历史问题
        List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(userId);
        // 获取用户历史问题
        StringBuilder sb = new StringBuilder();
        for (ChatMemory chatMemory : chatMemoryList) {
            sb.append(chatMemory.getQuestionText()).append("\n");
       }
//        questionText = sb.toString() + "生成一个关于" + courseName + "的" + questionText;
        questionText = "生成一个关于" + courseName + "的" + questionText;
        String answer = aiService.getStuAnswer(questionText, request);
        answerService.insert(userId, questionText, answer);
        chatMemoryService.insert(userId, questionText, answer);
        return answer;
    }

    @RequestMapping("/getPracticeQuestions")
    public List<Question> getPracticeQuestions(String courseName, String knowledgePoint, int quantity, HttpServletRequest request) {
        List<Question> questions = new ArrayList<>();
        // 获取用户ID
        User user = (User) request.getSession().getAttribute("session_user_key");
        int userId = user.getUserId();
        // 获取用户历史问题
        List<ChatMemory> chatMemoryList = chatMemoryService.selectByUserId(userId);
        StringBuilder sb = new StringBuilder();
        for (ChatMemory chatMemory : chatMemoryList) {
            sb.append(chatMemory.getQuestionText()).append("\n");
        }
        // 获取课程和课时计划ID
        int courseId = courseService.getCourseIdByName(courseName);
        int lessonPlanId = lessonPlanService.getLessonPlanIdByCourseId(courseId);
        String question = "生成一个关于" + courseName + "的" + knowledgePoint + "的题目";
        // 用线程池并行生成问题
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(quantity, 10)); // 限制最大线程数，防止爆
        List<CompletableFuture<Question>> futures = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            CompletableFuture<Question> future = CompletableFuture.supplyAsync(() -> {
                String ans = String.valueOf(questionService.generateQuestionAsync(question, knowledgePoint, request, lessonPlanId));
                return questionService.generateQuestionAsync(question, knowledgePoint, request, lessonPlanId).join();
            }, executor);
            futures.add(future);
        }
        // 合并结果
        for (CompletableFuture<Question> future : futures) {
            try {
                questions.add(future.get());
                questionService.insert(lessonPlanId, question, future.get().getQuestionType(), aiService.getReferenceAnswer(question, request), knowledgePoint);

            } catch (Exception e) {
                e.printStackTrace(); // 出错打印日志，防炸
            }
        }
        executor.shutdown(); // 关线程池
        return questions;
    }

    @RequestMapping("/submitPractice")
    PracticeRecord submitPractice(int questionId, String submittedAnswer, HttpServletRequest request) {
        PracticeRecord practiceRecord = new PracticeRecord();
        practiceRecord.setQuestionId(questionId);
        practiceRecord.setSubmittedAnswer(submittedAnswer);
        User user = (User) request.getSession().getAttribute("session_user_key");
        int userId = user.getUserId();
        practiceRecord.setStudentId(user.getUserId());
        String questionText = questionService.getQuestionTextById(questionId);
        Boolean isCorrect = Boolean.valueOf(aiService.judgeAnswer(userId, questionText, submittedAnswer, request));
        practiceRecord.setIsCorrect(isCorrect);
        String errorAnalysis = aiService.giveSuggest(userId, questionText, submittedAnswer, request);
        practiceRecord.setErrorAnalysis(errorAnalysis);
        practiceRecordService.insert(userId, questionId, submittedAnswer, isCorrect, errorAnalysis);
        return practiceRecord;
    }
}
