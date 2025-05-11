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
        // 获取用户历史问题
        StringBuilder sb = new StringBuilder();
        for (ChatMemory chatMemory : chatMemoryList) {
            sb.append(chatMemory.getQuestionText()).append("\n");
        }
        int courseId = courseService.getCourseIdByName(courseName);
        int lessonPlanId = lessonPlanService.getLessonPlanIdByCourseId(courseId);
        String question = sb.toString() + "生成一个关于" + courseName + "的" + knowledgePoint + "的题目";
        for(int i = 0; i < quantity; i++) {
            Question que = new Question();
            String q = aiService.getQuestion(question, knowledgePoint, request);
            que.setQuestionText(q);
            que.setKnowledgePoint(knowledgePoint);
            que.setReferenceAnswer(aiService.getReferenceAnswer(aiService.getQuestion(question, knowledgePoint, request), request));
            que.setLessonPlanId(lessonPlanId);
            questions.add(que);
        }
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
        return practiceRecord;
    }
}
