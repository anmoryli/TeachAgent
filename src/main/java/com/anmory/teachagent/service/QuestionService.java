package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.QuestionMapper;
import com.anmory.teachagent.model.Question;
import com.anmory.teachagent.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午5:28
 */

@Service
public class QuestionService {
    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    AiService aiService;
    @Autowired
    LessonPlanService lessonPlanService;
    @Autowired
    CourseService courseService;

    public int insert(int lessonPlanId, String questionText, String questionType, String referenceAnswer, String knowledgePoint) {
        return questionMapper.insert(lessonPlanId, questionText, questionType, referenceAnswer, knowledgePoint);
    }

    public List<Question> selectByLessonPlanId(int lessonPlanId) {
        return questionMapper.selectByLessonPlanId(lessonPlanId);
    }

    public String getQuestionTextById(int questionId) {
        return questionMapper.getQuestionTextById(questionId);
    }

    public CompletableFuture<Question> generateQuestionAsync(String question, String knowledgePoint, String prompt, HttpServletRequest request, int lessonPlanId) throws IOException {
        Question que = new Question();
        String q = aiService.getQuestion(question, knowledgePoint, prompt, request);
        String type = aiService.getQuestionType(q, request);
        que.setQuestionType(type);
        que.setQuestionText(q);
        que.setKnowledgePoint(knowledgePoint);
        que.setReferenceAnswer(aiService.getReferenceAnswer(aiService.getQuestion(question, knowledgePoint, prompt, request), request));
        que.setLessonPlanId(lessonPlanId);
        return CompletableFuture.completedFuture(que);
    }

    public List<Question> selectAll() {
        return questionMapper.selectAll();
    }
}
