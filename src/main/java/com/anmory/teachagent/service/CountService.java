package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.CountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-29 下午7:12
 */

@Service
public class CountService {
    @Autowired
    CountMapper countMapper;

    public int getCourseCnt() {
        return countMapper.getCourseCnt();
    }

    public int getStudentCnt() {
        return countMapper.getStudentCnt();
    }

    public int getTeacherCnt() {
        return countMapper.getTeacherCnt();
    }

    public int getLessonPlanCnt() {
        return countMapper.getLessonPlanCnt();
    }

    public int getQuestionCnt() {
        return countMapper.getQuestionCnt();
    }

    public int getMaterialCnt() {
        return countMapper.getMaterialCnt();
    }

    public int getPracticeRecordCnt() {
        return countMapper.getPracticeRecordCnt();
    }

    public int getAnswerCnt() {
        return countMapper.getAnswerCnt();
    }

    public int getCorrectCnt() {
        return countMapper.getCorrectCnt();
    }

    public int getIncorrectCnt() {
        return countMapper.getIncorrectCnt();
    }
}
