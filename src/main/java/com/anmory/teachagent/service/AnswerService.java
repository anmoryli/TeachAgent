package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.AnswerMapper;
import com.anmory.teachagent.model.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-11 上午10:37
 */


@Service
public class AnswerService {
    @Autowired
    AnswerMapper answerMapper;

    public Answer selectByQuestionText(String questionText) {
        return answerMapper.selectByQuestionText(questionText);
    }

    public int insert(int studentId, String questionText, String answerText) {
        return answerMapper.insert(studentId, questionText, answerText);
    }

    public List<Answer> selectByStudentId(int studentId) {
        return answerMapper.selectByStudentId(studentId);
    }
}
