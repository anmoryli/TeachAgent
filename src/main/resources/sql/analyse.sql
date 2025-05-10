# 1. 对学生提交的答案进行自动化检测，提供错误定位与修正建议
# 这个部分需要结合 PracticeRecord 和 Answer 表来实现。假设我们有一个规则来判断答案是否正确，并且有一个错误分析字段来存储修正建议。
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


# 2. 对学生整体数据进行分析，总结知识掌握情况与教学建议
# 这个部分需要结合 Statistics 表来实现。
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

# 大屏概览
# 1. 教师使用次数统计/活跃板块(当日/本周)
-- 当日教师使用次数统计
SELECT
    COUNT(*) AS count,
    role,
    module
FROM
    ActivityLog
WHERE
    DATE(action_time) = CURDATE()
GROUP BY
    role, module;

-- 本周教师使用次数统计
SELECT
    COUNT(*) AS count,
    role,
    module
FROM
    ActivityLog
WHERE
    WEEK(action_time) = WEEK(CURDATE())
GROUP BY
    role, module;

-- 2. 学生使用次数统计/活跃板块(当日/本周)
--  当日学生使用次数统计
SELECT
    COUNT(*) AS count,
    role,
    module
FROM
    ActivityLog
WHERE
    DATE(action_time) = CURDATE()
    AND role = 'student'
GROUP BY
    role, module;

-- 本周学生使用次数统计
SELECT
    COUNT(*) AS count,
    role,
    module
FROM
    ActivityLog
WHERE
    WEEK(action_time) = WEEK(CURDATE())
    AND role = 'student'
GROUP BY
    role, module;

# 3. 教学效率指数(备课与修正耗时、课后练习设计与修正耗时、课程优化方向)
# 这个部分需要结合 EfficiencyMetrics 表来实现。
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

# 4. 学生学习效果(平均正确率趋势、知识点掌握情况，高频错误知识点等)
# 这个部分需要结合 Statistics 表来实现。
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


