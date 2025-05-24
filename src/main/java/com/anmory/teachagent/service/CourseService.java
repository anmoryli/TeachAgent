package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.CourseMapper;
import com.anmory.teachagent.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 上午11:03
 */

@Service
public class CourseService {
    @Autowired
    CourseMapper courseMapper;

    public int insert(String courseName, String discipline, String description) {
        return courseMapper.insert(courseName, discipline, description);
    }

    public List<Course> selectAll() {
        return courseMapper.selectAll();
    }

    public Course selectByName(String courseName) {
        return courseMapper.selectByName(courseName);
    }

    public int deleteByName(String courseName) {
        return courseMapper.deleteByName(courseName);
    }

    public int update(int courseId, String courseName, String discipline, String description) {
        return courseMapper.update(courseId, courseName, discipline, description);
    }

    public int getCourseIdByName(String courseName) {
        return courseMapper.getCourseIdByName(courseName);
    }

    public Course selectById(int courseId) {
        return courseMapper.selectById(courseId);
    }
}
