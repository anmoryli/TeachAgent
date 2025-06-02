package com.anmory.teachagent.controller;

import com.anmory.teachagent.config.PasswordUtil;
import com.anmory.teachagent.dto.*;
import com.anmory.teachagent.model.LessonPlan;
import com.anmory.teachagent.model.Material;
import com.anmory.teachagent.model.Question;
import com.anmory.teachagent.model.User;
import com.anmory.teachagent.service.*;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Anmory
 * @description 管理员界面接口
 * @date 2025-05-10 下午9:48
 */

@Slf4j
@RestController
@RequestMapping("/admin")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    MaterialService materialService;
    @Autowired
    LessonPlanService lessonPlanService;
    @Autowired
    AnalyseService analyseService;
    @Autowired
    QuestionService questionService;
    @Autowired
    CountService countService;
    @RequestMapping("/login")
    public Result<User> login(String username, String password, String role, HttpSession session) {
        log.info("用户登录: username = {}, password = {}, role = {}", username, password, role);
        User user = userService.selectByName(username);
        if(user.getCode().equals("001")) {
            return Result.success("管理员登录", user);
        }
        if (user == null) {
            return Result.fail("用户不存在", "用户不存在");
        }
        // 对传入的明文密码进行加密后再比较
        String hashedInputPassword = PasswordUtil.hashPassword(password);
        if (!user.getPassword().equals(hashedInputPassword)) {
            return Result.fail("密码错误", "密码错误");
        }
        if (!user.getRole().equals(role)) {
            return Result.fail("角色不匹配", "角色不匹配");
        }
        session.setAttribute("session_user_key", user);
        return Result.success("登录成功", user);
    }

    @RequestMapping("/getAllQuestions")
    public List<Question> getAllQuestions() {
        log.info("获取所有问题成功");
        return questionService.selectAll();
    }

    @RequestMapping("/register")
    public boolean register(String username, String password, String role, String email, String realName, String code) {
        // 注册时对密码进行加密再存储
        String hashedPassword = PasswordUtil.hashPassword(password);
        return userService.insert(username, hashedPassword, role, email, realName, code) > 0;
    }

    @RequestMapping("/viewUsers")
    public List<User> viewUsers() {
        return userService.selectAll();
    }

    @RequestMapping("/addUser")
    public boolean addUser(String username, String password, String role, String email, String realName, String code) {
        // 注册时对密码进行加密再存储
        String hashedPassword = PasswordUtil.hashPassword(password);
        return userService.insert(username, hashedPassword, role, email, realName, code) > 0;
    }

    @RequestMapping("/deleteUser")
    public boolean deleteUser(String username) {
        return userService.deleteByName(username) > 0;
    }

    @RequestMapping("/updateUser")
    public boolean updateUser(String username, String password, String role, String email) {
        User user = userService.selectByName(username);
        return userService.update(user.getUserId(), username, password, role, email) > 0;
    }

    @RequestMapping("/viewResources")
    public List<Material> viewResources() {
        return materialService.selectAll();
    }

    @RequestMapping("/judgeStudentAnswer")
    public List<JudgeStudentAnswerDto> judgeStudentAnswer() {
        return analyseService.judgeStudentAnswer();
    }

    @RequestMapping("/getStudentAllData")
    public List<GetStudentAllDataDto> getStudentAllData() {
        return analyseService.getStudentAllData();
    }

    @RequestMapping("/teacherUsageByDay")
    public List<TeacherUsageByDayDto> teacherUsageByDay() {
        return analyseService.teacherUsageByDay();
    }

    @RequestMapping("/teacherUsageByWeek")
    public List<TeacherUsageByDayDto> teacherUsageByWeek() {
        return analyseService.teacherUsageByWeek();
    }

    @RequestMapping("/studentUsageByDay")
    public List<TeacherUsageByDayDto> studentUsageByDay() {
        return analyseService.studentUsageByDay();
    }

    @RequestMapping("/studentUsageByWeek")
    public List<TeacherUsageByDayDto> studentUsageByWeek() {
        return analyseService.studentUsageByWeek();
    }

    @RequestMapping("/teachEffect")
    public List<TeachEffectDto> teachEffect() {
        return analyseService.teachEffect();
    }

    @RequestMapping("/studentLearningEffect")
    public List<StudentLearningEffectDto> studentLearningEffect() {
        return analyseService.studentLearningEffect();
    }

    @RequestMapping("/exportResource")
//    @CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public ResponseEntity<byte[]> exportResource(@RequestParam("lessonPlanId") int lessonPlanId) {
        try {
            // 获取课程计划
            LessonPlan lessonPlan = lessonPlanService.selectById(lessonPlanId);
            if (lessonPlan == null) {
                log.error("课程计划不存在: lessonPlanId={}", lessonPlanId);
                throw new RuntimeException("课程计划不存在");
            }

            // 记录内容
            log.info("课程计划标题: {}", lessonPlan.getTitle());
            log.info("课程计划内容: {}", lessonPlan.getContent());

            // 创建 PDF 字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 设置中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            document.setFont(font);

            // 添加内容
            document.add(new Paragraph("课程计划: " + lessonPlan.getTitle()));
            document.add(new Paragraph("内容:").setBold());
            document.add(new Paragraph(lessonPlan.getContent()));

            // 关闭文档
            document.close();

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "lesson_plan_" + lessonPlanId + ".pdf");

            // 返回响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            log.error("导出 PDF 失败: lessonPlanId={}, 错误={}", lessonPlanId, e.getMessage(), e);
            throw new RuntimeException("生成 PDF 文件失败：" + e.getMessage());
        }
    }

    @RequestMapping("/getCount")
    public Result<CountDto> getCount() {
        CountDto countDto = new CountDto();
        countDto.setCourseCnt(countService.getCourseCnt());
        countDto.setStudentCnt(countService.getStudentCnt());
        countDto.setTeacherCnt(countService.getTeacherCnt());
        countDto.setLessonPlanCnt(countService.getLessonPlanCnt());
        countDto.setQuestionCnt(countService.getQuestionCnt());
        countDto.setMaterialCnt(countService.getMaterialCnt());
        countDto.setPracticeRecordCnt(countService.getPracticeRecordCnt());
        countDto.setAnswerCnt(countService.getAnswerCnt());
        countDto.setCorrectCnt(countService.getCorrectCnt());
        countDto.setIncorrectCnt(countService.getIncorrectCnt());
        return Result.success("获取统计信息成功", countDto);
    }
}
