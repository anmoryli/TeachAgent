package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.*;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    RagService ragService;

    @RequestMapping("/uploadFile")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public boolean upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("文件名称:",  file.getOriginalFilename());
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

    @RequestMapping("/getAllCourses")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Course> getAllCourses() {
        log.info("获取所有课程成功");
        return courseService.selectAll();
    }

    @RequestMapping("/createCourse")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public boolean createCourse(String courseName, String discipline, String description) {
        log.info("创建课程成功");
        courseService.insert(courseName, discipline, description);
        return true;
    }

    @RequestMapping("/createLessonPlan")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public boolean createLessonPlan(String courseName, String question, HttpServletRequest request) throws IOException {
        log.info("创建课程计划成功");
        String prompt = ragService.getRelevant(question);
        prompt = "请根据以下内容生成:" + prompt;
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        String plan = aiService.getLessonPlan(prompt, request, user.getCode());
        lessonPlanService.insert(user.getUserId(), courseService.selectByName(courseName).getCourseId(), courseName+"课程计划", plan);
        return true;
    }

    @RequestMapping("/getAllLessonPlans")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<LessonPlan> getLessonPlans() {
        log.info("获取课程计划成功");
        return lessonPlanService.selectAll();
    }

    @RequestMapping("/getLessonPlanByCourseId")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<LessonPlan> getLessonPlanByCourseName(int courseId) {
        log.info("获取课程计划成功");
        return lessonPlanService.selectByCourseId(courseId);
    }

    @RequestMapping("/generateQuestions")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> generateQuestions(Integer lessonPlanId, String questionType, String knowledgePoint, int quantity,
                                            HttpServletRequest request) throws IOException {
        String prompt = ragService.getRelevant(questionType + " " + knowledgePoint);
        log.info(String.valueOf(lessonPlanId));
        for(int i = 0; i < quantity; i++) {
            questionService.insert(lessonPlanId, aiService.getQuestion(questionType, knowledgePoint, prompt, request), questionType,
                    aiService.getReferenceAnswer(aiService.getQuestion(questionType, knowledgePoint, prompt, request), request),
                    knowledgePoint);
        }
        return questionService.selectByLessonPlanId(lessonPlanId);
    }

    @RequestMapping("/getAllQuestions")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getAllQuestions() {
        log.info("获取所有问题成功");
        return questionService.selectAll();
    }

    @RequestMapping("/getQuestionByLessonPlanId")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getQuestionByLessonPlanName(int lessonPlanId) {
        log.info("获取课程问题成功");
        LessonPlan lessonPlan = lessonPlanService.selectById(lessonPlanId);
        return questionService.selectByLessonPlanId(lessonPlan.getLessonPlanId());
    }

    @RequestMapping("/viewLearningAnalysis")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Statistics> viewLearningAnalysis(int courseId, int studentId) {
        return statisticsService.getCourseStuStatistics(courseId, studentId);
    }
}
