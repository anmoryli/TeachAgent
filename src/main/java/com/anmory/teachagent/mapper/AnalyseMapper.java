package com.anmory.teachagent.mapper;

import com.anmory.teachagent.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-24 下午10:48
 */

@Mapper
public interface AnalyseMapper {
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
                pr.is_correct = 0;
            """)
    List<JudgeStudentAnswerDto> judgeStudentAnswer();

    @Select("""
            SELECT
                s.student_id,
                s.knowledge_point,
                AVG(s.correct_rate) AS avg_correct_rate,
                GROUP_CONCAT(DISTINCT s.common_errors SEPARATOR ', ') AS common_errors,
                GROUP_CONCAT(DISTINCT s.teaching_suggestion SEPARATOR ', ') AS teaching_suggestions
            FROM
                Statistics s
            GROUP BY
                s.student_id, s.knowledge_point;
            """)
    List<GetStudentAllDataDto> getStudentAllData();

    @Select("""
            SELECT
                COUNT(*) AS count,
                role,
                module
            FROM
                ActivityLog
            WHERE
                DATE(action_time) = CURDATE() and role='teacher'
            GROUP BY
                role, module;
            """)
    List<TeacherUsageByDayDto> teacherUsageByDay();

    @Select("""
            SELECT
                COUNT(*) AS count,
                role,
                module
            FROM
                ActivityLog
            WHERE
                WEEK(action_time) = WEEK(CURDATE()) and role='teacher'
            GROUP BY
                role, module;
            """)
    List<TeacherUsageByDayDto> teacherUsageByWeek();

    @Select("""
            SELECT
                COUNT(*) AS count,
                role,
                module
            FROM
                ActivityLog
            WHERE
                DATE(action_time) = CURDATE() and role='student'
            GROUP BY
                role, module;
            """)
    List<TeacherUsageByDayDto> studentUsageByDay();

    @Select("""
            SELECT
                COUNT(*) AS count,
                role,
                module
            FROM
                ActivityLog
            WHERE
                WEEK(action_time) = WEEK(CURDATE()) and role='student'
            GROUP BY
                role, module;
            """)
    List<TeacherUsageByDayDto> studentUsageByWeek();

    @Select("""
            SELECT
                em.teacher_id,
                AVG(em.prep_time) AS avg_prep_time,
                AVG(em.correction_time) AS avg_correction_time,
                AVG(em.avg_correct_rate) AS avg_correct_rate,
                GROUP_CONCAT(DISTINCT em.high_freq_error_point SEPARATOR ', ') AS high_freq_error_points,
                GROUP_CONCAT(DISTINCT em.optimization_suggestion SEPARATOR ', ') AS optimization_suggestions
            FROM
                EfficiencyMetrics em
            GROUP BY
                em.teacher_id;
            """)
    List<TeachEffectDto> teachEffect();

    @Select("""
            SELECT
                s.student_id,
                s.knowledge_point,
                AVG(s.correct_rate) AS avg_correct_rate,
                GROUP_CONCAT(DISTINCT s.common_errors SEPARATOR ', ') AS common_errors,
                GROUP_CONCAT(DISTINCT s.teaching_suggestion SEPARATOR ', ') AS teaching_suggestions
            FROM
                Statistics s
            GROUP BY
                s.student_id, s.knowledge_point;
            """)
    List<StudentLearningEffectDto> studentLearningEffect();

}
