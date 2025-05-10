package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.QuestionMapper;
import com.anmory.teachagent.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午5:28
 */

@Service
public class QuestionService {
    @Autowired
    QuestionMapper questionMapper;

    public int insert(int lessonPlanId, String questionText, String questionType, String referenceAnswer, String knowledgePoint) {
        return questionMapper.insert(lessonPlanId, questionText, questionType, referenceAnswer, knowledgePoint);
    }

    public List<Question> selectByLessonPlanId(int lessonPlanId) {
        return questionMapper.selectByLessonPlanId(lessonPlanId);
    }
}
