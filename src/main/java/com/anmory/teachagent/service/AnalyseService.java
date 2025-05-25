package com.anmory.teachagent.service;

import com.anmory.teachagent.dto.*;
import com.anmory.teachagent.mapper.AnalyseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-24 下午10:55
 */

@Service
public class AnalyseService {
    @Autowired
    AnalyseMapper analyseMapper;

    public List<JudgeStudentAnswerDto> judgeStudentAnswer() {
        return analyseMapper.judgeStudentAnswer();
    }

    public List<GetStudentAllDataDto> getStudentAllData() {
        return analyseMapper.getStudentAllData();
    }

    public List<TeacherUsageByDayDto> teacherUsageByDay() {
        return analyseMapper.teacherUsageByDay();
    }

    public List<TeacherUsageByDayDto> teacherUsageByWeek() {
        return analyseMapper.teacherUsageByWeek();
    }

    public List<TeacherUsageByDayDto> studentUsageByDay() {
        return analyseMapper.studentUsageByDay();
    }

    public List<TeacherUsageByDayDto> studentUsageByWeek() {
        return analyseMapper.studentUsageByWeek();
    }

    public List<TeachEffectDto> teachEffect() {
        return analyseMapper.teachEffect();
    }

    public List<StudentLearningEffectDto> studentLearningEffect() {
        return analyseMapper.studentLearningEffect();
    }
}
