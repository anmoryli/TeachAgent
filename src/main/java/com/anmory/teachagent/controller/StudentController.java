package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.*;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    @Autowired
    RagService ragService;

    @RequestMapping("/askQuestion")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public String askQuestion(int courseId, String questionText, HttpServletRequest request) throws IOException {
        String prompt = ragService.getRelevant(questionText);
        Course course = courseService.selectById(courseId);
        // 获取用户ID
        User user = (User) request.getSession().getAttribute("session_user_key");
        int userId = user.getUserId();
        questionText = "生成一个关于" + course.getCourseName() + "的" + questionText + "需要参考的资料是:" + prompt;
        String answer = aiService.getStuAnswer(questionText, request);
        answerService.insert(userId, questionText, answer);
        return answer;
    }

    @RequestMapping("/getAllQuestionsByStudentId")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Answer> getAllQuestionsByStudentId(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        int studentId = user.getUserId();
        return answerService.selectByStudentId(studentId);
    }

    @RequestMapping("/getPracticeQuestions")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getPracticeQuestions(int courseId, String knowledgePoint, int quantity, HttpServletRequest request) throws IOException {
        List<Question> questions = new ArrayList<>();
        String courseName = courseService.selectById(courseId).getCourseName();
        // 获取课程和课时计划ID
        int lessonPlanId = lessonPlanService.getLessonPlanIdByCourseId(courseId);
        String question = "生成一个关于" + courseName + "的" + knowledgePoint + "的题目";
        String prompt = ragService.getRelevant(question);
        question = question + "，需要参考的资料是:" + prompt;
        // 用线程池并行生成问题
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(quantity, 10)); // 限制最大线程数，防止爆
        List<CompletableFuture<Question>> futures = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String finalQuestion = question;
            CompletableFuture<Question> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String ans = String.valueOf(questionService.generateQuestionAsync(finalQuestion, knowledgePoint, prompt, request, lessonPlanId));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    return questionService.generateQuestionAsync(finalQuestion, knowledgePoint, prompt, request, lessonPlanId).join();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    PracticeRecord submitPractice(int questionId, String submittedAnswer, HttpServletRequest request) throws IOException {
        String referenceAnswer = aiService.getReferenceAnswer(questionService.getQuestionTextById(questionId), request);
        PracticeRecord practiceRecord = new PracticeRecord();
        practiceRecord.setQuestionId(questionId);
        practiceRecord.setSubmittedAnswer(submittedAnswer);
        User user = (User) request.getSession().getAttribute("session_user_key");
        int userId = user.getUserId();
        practiceRecord.setStudentId(user.getUserId());
        String questionText = questionService.getQuestionTextById(questionId);
        Boolean isCorrect = Boolean.valueOf(aiService.judgeAnswer(userId, questionText, submittedAnswer, request));
        practiceRecord.setIsCorrect(isCorrect);
        String errorAnalysis = aiService.giveSuggest(userId, questionText, submittedAnswer, referenceAnswer, request);
        practiceRecord.setErrorAnalysis(errorAnalysis);
        practiceRecordService.insert(userId, questionId, submittedAnswer, isCorrect, errorAnalysis);
        return practiceRecord;
    }
}