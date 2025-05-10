package com.anmory.teachagent.controller;

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
    ChatMemoryService chatMemoryService;
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
}
