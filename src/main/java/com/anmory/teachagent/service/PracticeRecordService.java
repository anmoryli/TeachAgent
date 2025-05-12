package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.PracticeRecordMapper;
import com.anmory.teachagent.model.PracticeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-11 上午11:05
 */

@Service
public class PracticeRecordService {
    @Autowired
    PracticeRecordMapper practiceRecordMapper;

    public int insert(int studentId, int questionId, String submittedAnswer, Boolean isCorrect, String errorAnalysis) {
        return practiceRecordMapper.insert(studentId, questionId, submittedAnswer, isCorrect, errorAnalysis);
    }

    public List<PracticeRecord> selectAll() {
        return practiceRecordMapper.selectAll();
    }

    public List<PracticeRecord> selectByStudentId(int studentId) {
        return practiceRecordMapper.selectByStudentId(studentId);
    }

    public List<PracticeRecord> selectByQuestionId(int questionId) {
        return practiceRecordMapper.selectByQuestionId(questionId);
    }

    public List<PracticeRecord> selectByStudentIdAndQuestionId(int studentId, int questionId) {
        return practiceRecordMapper.selectByStudentIdAndQuestionId(studentId, questionId);
    }

    public PracticeRecord selectLatestRecord(int studentId, int questionId) {
        return practiceRecordMapper.selectLatestRecord(studentId, questionId);
    }
}
