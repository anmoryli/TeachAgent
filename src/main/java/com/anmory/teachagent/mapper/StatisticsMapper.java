package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory
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
            pr.error_analysis,
            q.knowledge_point
        FROM
            PracticeRecord pr
            INNER JOIN Question q ON pr.question_id = q.question_id
            INNER JOIN LessonPlan lp ON q.lesson_plan_id = lp.lesson_plan_id
            INNER JOIN Course c ON lp.course_id = c.course_id
        WHERE
            pr.is_correct = 0
            AND c.course_id = #{courseId}
            AND pr.student_id = #{studentId}
        ORDER BY pr.submitted_at DESC
        LIMIT 200
""")
    List<Statistics> getCourseStuStatistics(@Param("courseId") int courseId, @Param("studentId") int studentId);
}
