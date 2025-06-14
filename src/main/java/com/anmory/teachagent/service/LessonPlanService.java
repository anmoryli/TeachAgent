package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.LessonPlanMapper;
import com.anmory.teachagent.model.LessonPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 上午11:24
 */

@Service
public class LessonPlanService {
    @Autowired
    LessonPlanMapper lessonPlanMapper;

    public int insert(int teacherId, int courseId, String title, String content) {
        return lessonPlanMapper.insert(teacherId, courseId, title, content);
    }

    public List<LessonPlan> selectAll() {
        return lessonPlanMapper.selectAll();
    }

    public int deleteByCourseId(int courseId) {
        return lessonPlanMapper.deleteByCourseId(courseId);
    }

    public LessonPlan selectById(Integer lessonPlanId) {
        return lessonPlanMapper.selectOne(lessonPlanId);
    }

    public Integer getLessonPlanIdByCourseId(int courseId) {
        return lessonPlanMapper.getLessonPlanIdByCourseId(courseId) == null ? 1 : lessonPlanMapper.getLessonPlanIdByCourseId(courseId);
    }

    public int deleteByLessonPlanId(int lessonPlanId) {
        return lessonPlanMapper.deleteByLessonPlanId(lessonPlanId);
    }

    public List<LessonPlan> selectByCourseName(String courseName) {
        return lessonPlanMapper.selectByCourseName(courseName);
    }

    public LessonPlan selectOneByCourseName(String courseName) {
        return lessonPlanMapper.selectOneByCourseName(courseName);
    }

    public List<LessonPlan> selectByLessonPlanName(String lessonPlanName) {
        return lessonPlanMapper.selectByLessonPlanName(lessonPlanName);
    }

    public List<LessonPlan> selectByCourseId(int courseId) {
        return lessonPlanMapper.selectByCourseId(courseId);
    }

    public int update(Integer lessonPlanId, int teacherId, int courseId, String title, String content) {
        return lessonPlanMapper.update(lessonPlanId, teacherId, courseId, title, content);
    }
}
