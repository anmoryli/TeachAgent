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
    @Select("""
            SELECT
                pr.student_id,
                pr.question_id,
                pr.submitted_answer,
                pr.is_correct,
                pr.error_analysis
            FROM
                PracticeRecord pr
                    LEFT JOIN
                Answer a ON pr.question_id = a.answer_id
            WHERE
                pr.is_correct = 0
            order by Answer.create_time desc limit 200""")
    List<Statistics> getCourseStuStatistics(int courseId, int studentId);
}
