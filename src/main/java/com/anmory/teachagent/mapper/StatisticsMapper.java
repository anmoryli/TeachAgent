package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-10 下午7:47
 */

@Mapper
public interface StatisticsMapper {
    @Select("select * from Statistics where course_id = #{courseId} and student_id = #{studentId}")
    List<Statistics> getCourseStuStatistics(int courseId, int studentId);
}
