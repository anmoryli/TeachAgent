package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.EfficiencyMetrics;
import com.anmory.teachagent.model.Question;
import com.anmory.teachagent.model.Statistics;
import com.anmory.teachagent.model.User;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午10:00
 */

@RestController
@RequestMapping("/teacher")
@Slf4j
public class TeacherController {
    @Autowired
    MaterialService materialService;
    @Autowired
    CourseService courseService;
    @Autowired
    LessonPlanService lessonPlanService;
    @Autowired
    AiService aiService;
    @Autowired
    QuestionService questionService;
    @Autowired
    StatisticsService statisticsService;
    @Autowired
    ChatMemoryService chatMemoryService;
    @Autowired
    KnowledgeBaseService knowledgeBaseService;
    @RequestMapping("/uploadFile")
    public boolean upload(@RequestParam("file") MultipartFile file) throws IOException {
        String filePath = "/user/local/nginx/files/teach/"+file.getOriginalFilename();
        File dir = new File("/user/local/nginx/files/teach/");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(new File(filePath));
        fos.write(file.getBytes());
        log.info("文件上传成功");
        fos.close();
        materialService.insert(file.getOriginalFilename(), "/user/local/nginx/files/teach/"+file.getOriginalFilename(), "document");
        return true;
    }

    @RequestMapping("/createCourse")
    public boolean createCourse(String courseName, String discipline, String description) {
        log.info("创建课程成功");
        courseService.insert(courseName, discipline, description);
        return true;
    }

    @RequestMapping("/createLessonPlan")
    public boolean createLessonPlan(String courseName, String question, HttpServletRequest request) {
        log.info("创建课程计划成功");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        String plan = aiService.getLessonPlan(question, request);
        chatMemoryService.insert(user.getUserId(), question, plan);
        lessonPlanService.insert(user.getUserId(), courseService.selectByName(courseName).getCourseId(), courseName+"课程计划", plan);
        return true;
    }

    @RequestMapping("/generateQuestions")
    public List<Question> generateQuestions(Integer lessonPlanId, String questionType, String knowledgePoint, int quantity,
                                            HttpServletRequest request) {
        log.info(String.valueOf(lessonPlanId));
        chatMemoryService.insert(lessonPlanId, "生成问题", aiService.getQuestion(questionType, knowledgePoint, request));
        for(int i = 0; i < quantity; i++) {
            questionService.insert(lessonPlanId, aiService.getQuestion(questionType, knowledgePoint, request), questionType,
                    aiService.getReferenceAnswer(aiService.getQuestion(questionType, knowledgePoint, request), request),
                    knowledgePoint);
        }
        return questionService.selectByLessonPlanId(lessonPlanId);
    }

    @RequestMapping("/viewLearningAnalysis")
    public List<Statistics> viewLearningAnalysis(int courseId, int studentId) {
        return statisticsService.getCourseStuStatistics(courseId, studentId);
    }
    
    /**
     * 导入知识库文档
     * 将课程材料导入到知识库中
     */
    @RequestMapping("/importKnowledgeBase")
    public boolean importKnowledgeBase(int courseId) {
        try {
            log.info("导入知识库文档: 课程ID = {}", courseId);
            knowledgeBaseService.importDocuments(courseId);
            return true;
        } catch (Exception e) {
            log.error("导入知识库失败", e);
            return false;
        }
    }
    
    /**
     * 增强版课程计划生成
     * 基于本地知识库和自定义时间分配
     */
    @RequestMapping("/createEnhancedLessonPlan")
    public boolean createEnhancedLessonPlan(String courseName, String knowledgePoints, 
                                         int sessionHours, HttpServletRequest request) {
        log.info("创建增强版课程计划: 课程 = {}, 知识点 = {}, 课时 = {}", courseName, knowledgePoints, sessionHours);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        
        String prompt = "为" + courseName + "课程创建一个详细的" + sessionHours + 
                       "课时教学计划，涵盖以下知识点：" + knowledgePoints + 
                       "。请包含每个课时的教学内容、实践活动和时间分配。";
        
        String plan = aiService.getLessonPlan(prompt, request);
        
        chatMemoryService.insert(user.getUserId(), prompt, plan);
        lessonPlanService.insert(user.getUserId(), 
                                courseService.selectByName(courseName).getCourseId(), 
                                courseName + "增强课程计划", plan);
        return true;
    }
    
    /**
     * 生成多种类型的题目
     * 支持同时生成多种类型的题目
     */
    @RequestMapping("/generateMultiTypeQuestions")
    public List<Question> generateMultiTypeQuestions(Integer lessonPlanId, String knowledgePoint, 
                                                  int quantity, @RequestParam("questionTypes") String[] questionTypes,
                                                  HttpServletRequest request) {
        log.info("生成多种类型题目: 课程计划ID = {}, 知识点 = {}, 数量 = {}, 题型 = {}", 
                lessonPlanId, knowledgePoint, quantity, String.join(",", questionTypes));
        
        List<Question> questions = new ArrayList<>();
        
        for (String questionType : questionTypes) {
            for (int i = 0; i < quantity / questionTypes.length; i++) {
                String questionText = aiService.getQuestion(questionType, knowledgePoint, request);
                String referenceAnswer = aiService.getReferenceAnswer(questionText, request);
                
                questionService.insert(lessonPlanId, questionText, questionType, 
                                      referenceAnswer, knowledgePoint);
                
                Question q = new Question();
                q.setLessonPlanId(lessonPlanId);
                q.setQuestionText(questionText);
                q.setQuestionType(questionType);
                q.setReferenceAnswer(referenceAnswer);
                q.setKnowledgePoint(knowledgePoint);
                questions.add(q);
            }
        }
        
        return questions;
    }
    
    /**
     * 获取学生知识点掌握情况
     */
    @RequestMapping("/getKnowledgePointMastery")
    public Map<String, Double> getKnowledgePointMastery(int courseId, int studentId) {
        log.info("获取知识点掌握情况: 课程ID = {}, 学生ID = {}", courseId, studentId);
        List<Statistics> stats = statisticsService.getCourseStuStatistics(courseId, studentId);
        Map<String, Double> mastery = new HashMap<>();
        
        for (Statistics stat : stats) {
            mastery.put(stat.getKnowledgePoint(), stat.getCorrectRate());
        }
        
        return mastery;
    }
    
    /**
     * 获取高频错误知识点
     */
    @RequestMapping("/getFrequentErrors")
    public List<Map<String, Object>> getFrequentErrors(int courseId, int studentId) {
        log.info("获取高频错误知识点: 课程ID = {}, 学生ID = {}", courseId, studentId);
        List<Statistics> stats = statisticsService.getCourseStuStatistics(courseId, studentId);
        List<Map<String, Object>> errors = new ArrayList<>();
        
        for (Statistics stat : stats) {
            if (stat.getCorrectRate() < 0.6) { // 正确率低于60%的知识点
                Map<String, Object> error = new HashMap<>();
                error.put("knowledgePoint", stat.getKnowledgePoint());
                error.put("correctRate", stat.getCorrectRate());
                error.put("errorCount", stat.getTotalCount() - stat.getCorrectCount());
                errors.add(error);
            }
        }
        
        return errors;
    }
    
    /**
     * 获取教学效率指标
     */
    @RequestMapping("/getTeachingEfficiency")
    public Map<String, Object> getTeachingEfficiency(int courseId, HttpServletRequest request) {
        log.info("获取教学效率指标: 课程ID = {}", courseId);
        User user = (User) request.getSession().getAttribute("session_user_key");
        
        List<Statistics> stats = statisticsService.getCourseStatistics(courseId);
        
        double totalCorrect = 0;
        double totalQuestions = 0;
        for (Statistics stat : stats) {
            totalCorrect += stat.getCorrectCount();
            totalQuestions += stat.getTotalCount();
        }
        
        double overallCorrectRate = totalQuestions > 0 ? totalCorrect / totalQuestions : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("teacherId", user.getUserId());
        result.put("courseId", courseId);
        result.put("overallCorrectRate", overallCorrectRate);
        result.put("totalStudents", stats.size());
        result.put("knowledgePointsCount", stats.stream()
                .map(Statistics::getKnowledgePoint)
                .distinct()
                .count());
        
        return result;
    }
}
