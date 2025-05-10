package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.StatisticsMapper;
import com.anmory.teachagent.model.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午8:10
 */

@Service
public class StatisticsService {
    @Autowired
    StatisticsMapper statisticsMapper;

    public List<Statistics> getCourseStuStatistics(int courseId, int studentId) {
        return statisticsMapper.getCourseStuStatistics(courseId, studentId);
    }
}
