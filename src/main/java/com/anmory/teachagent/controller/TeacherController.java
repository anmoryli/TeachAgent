package com.anmory.teachagent.controller;

import com.anmory.teachagent.dto.Result;
import com.anmory.teachagent.model.*;
import com.anmory.teachagent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    @Autowired
    ActivityLogService activityLogService;
    @Autowired
    PPTService pptService;

    @RequestMapping("/uploadFile")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public boolean upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            log.error("用户未登录");
            return false;
        }
        // 记录使用的模块
        log.info("文件名称: {}", file.getOriginalFilename());
        // 记录开始时间
        long start = System.currentTimeMillis();
        String filePath = "/user/local/nginx/files/teach/" + file.getOriginalFilename();
        File dir = new File("/user/local/nginx/files/teach/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File targetFile = new File(filePath);
        boolean isFileExists = targetFile.exists();

        // 覆盖文件
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(file.getBytes());
            log.info("文件上传成功");
        }

        // 如果文件已存在，跳过向量化操作
        if (!isFileExists) {
            String url = "http://175.24.205.213:91/usr/local/nginx/files/teach/" + file.getOriginalFilename();
            ragService.embedding(url);
            log.info("文件进行向量化处理");
        } else {
            log.info("文件已存在，跳过向量化处理");
        }

        // 插入数据库（无论文件是否已存在）
        materialService.insert(file.getOriginalFilename(), filePath, "document");
        long  end = System.currentTimeMillis();
        long  costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "teacher", "上传文件", costTime);
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
    public boolean createCourse(String courseName, String discipline,
                                String description,
                                HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            log.error("用户未登录");
            return false;
        }
        long  start = System.currentTimeMillis();
        log.info("创建课程成功");
        courseService.insert(courseName, discipline, description);
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "teacher", "创建课程", costTime);
        return true;
    }

    @RequestMapping("/createLessonPlan")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public boolean createLessonPlan(int courseId, String question, HttpServletRequest request) throws IOException {
        long start = System.currentTimeMillis();
        String prompt = ragService.getRelevant(question);
        prompt = "请根据以下内容生成:" + prompt;
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user_key");
        String plan = aiService.getLessonPlan(prompt, request, user.getCode());
        lessonPlanService.insert(user.getUserId(), courseService.selectById(courseId).getCourseId(),
                courseService.selectById(courseId).getCourseName()+"课程计划", plan);
        log.info("创建课程计划成功");
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "teacher", "备课", costTime);
        return true;
    }

    // 上传视频然后向量化
    @RequestMapping("uploadVideo")
    public boolean uploadVideoAndEmbedding(MultipartFile file) {
        // 先把文件保存起来
        String filePath = "/user/local/nginx/files/teach/" + file.getOriginalFilename();
        File dir = new File("/user/local/nginx/files/teach/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(file.getBytes());
            log.info("视频上传成功");
        } catch (IOException e) {
            log.error("视频上传失败: {}", e.getMessage(), e);
            return false;
        }

        // 调用接口转文字然后向量化
        return ragService.uploadVideo("http://anmory.com:91" + filePath);
    }

    // 生成ppt接口
    @RequestMapping("/generatePPT")
    public String generatePPT(String subject) throws IOException {
        return pptService.genPPT(subject).getData();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/deleteLessonPlan")
    public boolean deleteLessonPlan(int lessonPlanId) {
        lessonPlanService.deleteByLessonPlanId(lessonPlanId);
        log.info("删除课程计划成功");
        return true;
    }

    @RequestMapping("/updateLessonPlan")
    public boolean updateLessonPlan(int lessonPlanId, String courseName, String title, String content, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        log.info("更新课程计划成功");
        lessonPlanService.update(lessonPlanId, user.getUserId(), courseService.selectByName(courseName).getCourseId(), title, content);
        return true;
    }

    @RequestMapping("/deleteCourse")
    public boolean deleteCourse(String courseName) {
        log.info("删除课程成功");
        courseService.deleteByName(courseName);
        return true;
    }

    @RequestMapping("updateCourse")
    public boolean updateCourse(String courseName, String discipline, String description) {
        log.info("更新课程成功");
        courseService.update(courseService.getCourseIdByName(courseName), courseName, discipline, description);
        return true;
    }

    @RequestMapping("/getAllMaterial")
    public List<Material> getAllMaterial() {
        log.info("获取所有资料成功");
        return materialService.selectAll();
    }

    @RequestMapping("/deleteMaterial")
    public boolean deleteMatrial(int materialId) {
        log.info("删除资料成功");
        materialService.deleteById(materialId);
        return true;
    }

    @RequestMapping("/updateQuestion")
    public boolean updateQuestion(int questionId, String question, String answer, String questionType, String knowledgePoint) {
        log.info("更新问题成功");
        questionService.update(questionId, question, answer, questionType, knowledgePoint);
        return true;
    }

    @RequestMapping("/deleteQuestion")
    public boolean deleteQuestion(int questionId) {
        log.info("删除问题成功");
        questionService.deleteById(questionId);
        return true;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
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

    @RequestMapping(value = "/generateQuestions", method = {RequestMethod.GET, RequestMethod.OPTIONS})
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> generateQuestions(
            @RequestParam Integer lessonPlanId,
            @RequestParam String questionType,
            @RequestParam String knowledgePoint,
            @RequestParam int quantity,
            HttpServletRequest request) throws IOException {
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            log.error("用户未登录");
            return null;
        }
        long start = System.currentTimeMillis();
        // 检查 lessonPlanId 是否存在
        LessonPlan lessonPlan = lessonPlanService.selectById(lessonPlanId);
        if (lessonPlan == null) {
            log.error("无效的 lessonPlanId: {}", lessonPlanId);
            throw new IllegalArgumentException("无效的 lessonPlanId: " + lessonPlanId);
        }

        String prompt = null;
        try {
            prompt = ragService.getRelevant(questionType + " " + knowledgePoint);
        } catch (Exception e) {
            log.error("获取 prompt 失败: {}", e.getMessage(), e);
            throw new IOException("无法获取相关 prompt: " + e.getMessage(), e);
        }
        log.info("开始为 lessonPlanId: {} 生成 {} 个问题", lessonPlanId, quantity);

        // 捕获请求上下文以确保线程安全
        RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);

        // 配置 OkHttpClient，增加超时时间和连接池
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();

        // 使用自定义线程池，控制并发
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(quantity, 2));
        String finalPrompt = prompt;
        List<CompletableFuture<Void>> futures = IntStream.range(0, quantity)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        // 在线程内重新设置请求上下文
                        RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);

                        String question = null;
                        String referenceAnswer = null;

                        // 重试机制
                        int maxRetries = 5;
                        int retryCount = 0;
                        boolean success = false;

                        while (retryCount < maxRetries && !success) {
                            try {
                                question = aiService.getQuestion(questionType, knowledgePoint, finalPrompt, request);
                                referenceAnswer = aiService.getReferenceAnswer(question, request);
                                success = true;
                            } catch (Exception e) {
                                retryCount++;
                                if (retryCount == maxRetries) {
                                    log.error("生成问题 {} 失败，重试 {} 次后仍失败: {}", i + 1, maxRetries, e.getMessage(), e);
                                    return; // 跳过此任务，不抛异常
                                }
                                log.warn("生成问题 {} 第 {} 次重试: {}", i + 1, retryCount, e.getMessage());
                                try {
                                    Thread.sleep(3000 * retryCount); // 指数退避
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    log.error("重试等待被中断: {}", ie.getMessage(), ie);
                                    return;
                                }
                            }
                        }

                        // 插入问题
                        if (question != null && referenceAnswer != null) {
                            try {
                                questionService.insert(lessonPlanId, question, questionType, referenceAnswer, knowledgePoint, user.getUserId());
                                log.debug("成功插入问题 {} for lessonPlanId: {}", i + 1, lessonPlanId);
                            } catch (Exception e) {
                                log.error("插入问题 {} 到数据库失败: {}", i + 1, e.getMessage(), e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("生成问题 {} 失败: {}", i + 1, e.getMessage(), e);
                    }
                }, executor))
                .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("线程池关闭被中断: {}", e.getMessage(), e);
        }

        // 记录失败的任务
        futures.forEach(future -> future.exceptionally(throwable -> {
            log.error("任务执行失败: {}", throwable.getMessage(), throwable);
            return null;
        }));

        // 查询并返回已成功插入的问题
        List<Question> questions = questionService.selectByLessonPlanId(lessonPlanId);
        log.info("为 lessonPlanId: {} 检索到 {} 个问题", lessonPlanId, questions.size());
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "teacher", "生成问题", costTime);
        return questions;
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

    @RequestMapping("/getAllQuestionsByUserId")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public List<Question> getAllQuestionsByTeacherId(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        if(user == null) {
            log.error("用户未登录");
            return null;
        }
        log.info("获取教师所有问题成功");
        return questionService.selectByUserId(user.getUserId());
    }

    @RequestMapping("/viewLearningAnalysis")
    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public String viewLearningAnalysis(int courseId, int studentId,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("session_user_key");
        if( user == null) {
            return "请先登录";
        }
        long start = System.currentTimeMillis();
        List<Object> statistics = Collections.singletonList(statisticsService.getCourseStuStatistics(courseId, studentId));
        log.info(statistics.toString());
        long end = System.currentTimeMillis();
        long costTime = end - start;
        costTime = costTime / 1000;
        activityLogService.insert(user.getUserId(), "teacher", "查看数据", costTime);
        return aiService.giveSumAndTeachSuggest(statistics, request);
    }
}
