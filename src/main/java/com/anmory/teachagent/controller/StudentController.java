package com.anmory.teachagent.controller;

import com.anmory.teachagent.dto.Result;
import com.anmory.teachagent.model.*;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

@Slf4j
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
    @Autowired
    JudgePrommingService judgePrommingService;
    @Autowired
    ActivityLogService activityLogService;

    @RequestMapping("/askQuestion")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public String askQuestion(int courseId, String questionText, HttpServletRequest request) throws IOException {
        long start = System.currentTimeMillis();
        // 获取 RAG 检索的相关内容
        String prompt = ragService.getRelevant(questionText);
        // 获取课程信息
        Course course = courseService.selectById(courseId);
        if (course == null) {
            throw new IOException("课程不存在");
        }
        // 获取用户ID
        User user = (User) request.getSession().getAttribute("session_user_key");
        if (user == null) {
            throw new IOException("用户未登录");
        }
        int userId = user.getUserId();
        // 构造提示词，结合课程名和检索内容
        questionText = "生成一个关于 " + course.getCourseName() + " 的 " + questionText +
                " 需要参考的资料是: " + prompt;
        // 获取 AI 回答
        String answer = aiService.getStuAnswer(questionText, request);
        // 记录问题和答案
        answerService.insert(userId, questionText, answer);
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "student", "提问", costTime);
        return answer;
    }

    @RequestMapping("/getAllQuestionsByStudentId")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Answer> getAllQuestionsByStudentId(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        int studentId = user.getUserId();
        return answerService.selectByStudentId(studentId);
    }

    @RequestMapping("/getAllQuestions")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getAllQuestions() {
        log.info("获取所有问题成功");
        return questionService.selectAll();
    }

    @RequestMapping("/getPracticeQuestions")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getPracticeQuestions(int courseId, String knowledgePoint, int quantity, HttpServletRequest request) throws IOException {
        List<Question> questions = new ArrayList<>();
        String courseName = courseService.selectById(courseId).getCourseName();
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            return null;
        }
        List<PracticeRecord> practiceRecord = practiceRecordService.selectByStudentId(user.getUserId());
        // 获取课程和课时计划ID
        int lessonPlanId = lessonPlanService.getLessonPlanIdByCourseId(courseId);
        String question = "生成一个关于" + courseName + "的" + knowledgePoint + "的题目,你需要基于这些历史的练习来生成：" + practiceRecord.toString();
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

    @RequestMapping("/getPracticeHistory")
    public Result<List<PracticeRecord>> getPracticeHistory(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        int studentId = user.getUserId();
        List<PracticeRecord> practiceRecord = practiceRecordService.selectByStudentId(studentId);
        return Result.success("获取练习记录成功", practiceRecord);
    }

    @PostMapping("/submitPractice")
    public PracticeRecord submitPractice(@RequestBody SubmitRequest request, HttpServletRequest httpRequest) throws Exception {
        long start = System.currentTimeMillis();
        int questionId = request.getQuestionId();
        String submittedAnswer = request.getSubmittedAnswer();
        String questionType = aiService.getQuestionType(submittedAnswer, httpRequest);

        String referenceAnswer;
        // 获取参考答案
        String questionText = questionService.getQuestionTextById(questionId);
        if("编程题".equals(questionType)) {
            referenceAnswer = judgePrommingService.judge(submittedAnswer);
        }
        else {
            referenceAnswer = aiService.getReferenceAnswer(questionText, httpRequest);
        }

        // 创建练习记录
        PracticeRecord practiceRecord = new PracticeRecord();
        practiceRecord.setQuestionId(questionId);
        practiceRecord.setSubmittedAnswer(submittedAnswer);

        // 获取用户信息
        User user = (User) httpRequest.getSession().getAttribute("session_user_key");
        if (user == null) {
            throw new IllegalStateException("User not logged in");
        }
        int userId = user.getUserId();
        practiceRecord.setStudentId(userId);

        // 判断答案
        Boolean isCorrect = Boolean.valueOf(aiService.judgeAnswer(userId, questionText, submittedAnswer, httpRequest));
        practiceRecord.setIsCorrect(isCorrect);

        // 获取错误分析
        String errorAnalysis = aiService.giveSuggest(userId, questionText, submittedAnswer, referenceAnswer, httpRequest);
        practiceRecord.setErrorAnalysis(errorAnalysis);

        // 保存记录
        practiceRecordService.insert(userId, questionId, submittedAnswer, isCorrect, errorAnalysis);
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
         activityLogService.insert(user.getUserId(), "student", "做题", costTime);
        return practiceRecord;
    }

    @Data
    static class SubmitRequest {
        private int questionId;
        private String submittedAnswer;
    }
}