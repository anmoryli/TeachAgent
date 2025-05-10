-- 测试数据插入脚本
-- 数据库：教学管理系统
-- 说明：按表依赖顺序插入数据，确保外键约束满足。数据覆盖用户、课程、课件、备课、题目、练习、问答、统计、日志、效率指标和对话记忆功能。

-- 注意：
-- 1. 确保数据库已创建并执行了提供的 schema（包含 User, Course, Material, LessonPlan, Question, PracticeRecord, Answer, Statistics, ActivityLog, EfficiencyMetrics, ChatMemory, Prompt 表）。
-- 2. 所有插入语句使用合法值，满足外键约束（如 ON DELETE CASCADE）。
-- 3. 可通过 `mysql -u username -p database_name < test_data.sql` 执行。

-- --------------------------------------
-- 1. 插入 User 表数据（无依赖）
-- 描述：创建 5 个用户，涵盖管理员、教师和学生角色，用于后续关联。
-- --------------------------------------
INSERT INTO User (username, password, role, email) VALUES
                                                       ('admin1', 'hashed_password_1', 'admin', 'admin1@example.com'), -- user_id: 1, 管理员
                                                       ('teacher1', 'hashed_password_2', 'teacher', 'teacher1@example.com'), -- user_id: 2, 教师 1
                                                       ('teacher2', 'hashed_password_3', 'teacher', 'teacher2@example.com'), -- user_id: 3, 教师 2
                                                       ('student1', 'hashed_password_4', 'student', 'student1@example.com'), -- user_id: 4, 学生 1
                                                       ('student2', 'hashed_password_5', 'student', 'student2@example.com'); -- user_id: 5, 学生 2

-- --------------------------------------
-- 2. 插入 Course 表数据（无依赖）
-- 描述：创建 2 门课程，关联不同学科。
-- --------------------------------------
INSERT INTO Course (course_name, discipline, description) VALUES
                                                              ('Python 编程基础', '计算机', '介绍 Python 编程的基本语法和应用'), -- course_id: 1
                                                              ('数据库原理', '计算机', '学习数据库设计和 SQL 查询'); -- course_id: 2

-- --------------------------------------
-- 3. 插入 Material 表数据（无依赖）
-- 描述：为课程添加 3 个课件资源（文档和练习）。
-- --------------------------------------
INSERT INTO Material (title, file_path, material_type) VALUES
                                                           ('Python 基础讲义', '/uploads/python_basics.pdf', 'document'), -- material_id: 1
                                                           ('SQL 查询练习', '/uploads/sql_exercises.pdf', 'exercise'), -- material_id: 2
                                                           ('Python 编程案例', '/uploads/python_cases.pdf', 'document'); -- material_id: 3

-- --------------------------------------
-- 4. 插入 Prompt 表数据（无依赖）
-- 描述：添加 2 个系统提示词，用于大模型对话。
-- --------------------------------------
INSERT INTO Prompt (prompt_text) VALUES
                                     ('You are a teacher creating lesson plans for programming courses.'), -- prompt_id: 1
                                     ('You are an assistant helping students understand coding concepts.'); -- prompt_id: 2

-- --------------------------------------
-- 5. 插入 LessonPlan 表数据（依赖 User, Course）
-- 描述：为课程创建 2 个备课计划，关联教师和课程。
-- --------------------------------------
INSERT INTO LessonPlan (teacher_id, course_id, title, content) VALUES
                                                                   (2, 1, 'Python 循环与条件语句', '讲解 for/while 循环和 if 语句，包含示例和练习'), -- lesson_plan_id: 1
                                                                   (3, 2, 'SQL 基础查询', '介绍 SELECT、WHERE、JOIN，包含实践练习'); -- lesson_plan_id: 2

-- --------------------------------------
-- 6. 插入 Question 表数据（依赖 LessonPlan）
-- 描述：为备课计划添加 4 个题目，覆盖不同类型。
-- --------------------------------------
INSERT INTO Question (lesson_plan_id, question_text, question_type, reference_answer, knowledge_point) VALUES
                                                                                                           (1, '编写一个 Python 程序，打印 1 到 10 的偶数', 'programming', 'for i in range(2, 11, 2): print(i)', '循环'), -- question_id: 1
                                                                                                           (1, '以下哪个是 Python 循环关键字？ A) loop B) for C) repeat', 'multiple_choice', 'B) for', '循环'), -- question_id: 2
                                                                                                           (2, '解释 SQL 中 WHERE 语句的作用', 'short_answer', 'WHERE 用于过滤记录，指定查询条件', 'SQL 查询'), -- question_id: 3
                                                                                                           (2, '编写 SQL 查询，从 students 表选择 age > 18 的记录', 'programming', 'SELECT * FROM students WHERE age > 18', 'SQL 查询'); -- question_id: 4

-- --------------------------------------
-- 7. 插入 PracticeRecord 表数据（依赖 User, Question）
-- 描述：记录学生提交的练习答案，关联学生和题目。
-- --------------------------------------
INSERT INTO PracticeRecord (student_id, question_id, submitted_answer, is_correct, error_analysis) VALUES
                                                                                                       (4, 1, 'for i in range(1, 11, 2): print(i)', FALSE, '错误：步长为 2 时打印奇数，应为 range(2, 11, 2)'), -- practice_id: 1
                                                                                                       (4, 2, 'B) for', TRUE, NULL), -- practice_id: 2
                                                                                                       (5, 3, 'WHERE 过滤查询条件', TRUE, NULL), -- practice_id: 3
                                                                                                       (5, 4, 'SELECT * FROM students WHERE age >= 18', FALSE, '错误：使用了 >= 而非 >'); -- practice_id: 4

-- --------------------------------------
-- 8. 插入 Answer 表数据（依赖 User）
-- 描述：记录学生在线问答，关联学生和大模型回答。
-- --------------------------------------
INSERT INTO Answer (student_id, question_text, answer_text) VALUES
                                                                (4, 'Python 循环是什么？', 'Python 循环包括 for 和 while，用于重复执行代码块...'), -- answer_id: 1
                                                                (5, 'SQL JOIN 有哪些类型？', 'SQL JOIN 包括 INNER JOIN, LEFT JOIN, RIGHT JOIN...'); -- answer_id: 2

-- --------------------------------------
-- 9. 插入 Statistics 表数据（依赖 Course, User）
-- 描述：记录学情统计，分析学生表现。
-- --------------------------------------
INSERT INTO Statistics (course_id, student_id, knowledge_point, correct_rate, common_errors, teaching_suggestion) VALUES
                                                                                                                      (1, 4, '循环', 50.00, '混淆奇偶数循环条件', '加强循环步长练习'), -- stat_id: 1
                                                                                                                      (2, 5, 'SQL 查询', 50.00, '误用比较运算符', '讲解比较运算符区别'); -- stat_id: 2

-- --------------------------------------
-- 10. 插入 ActivityLog 表数据（依赖 User）
-- 描述：记录用户活跃数据，跟踪系统使用情况。
-- --------------------------------------
INSERT INTO ActivityLog (user_id, role, module, action_time) VALUES
                                                                 (2, 'teacher', 'lesson_plan', '2025-05-10 09:00:00'), -- log_id: 1, 教师创建备课
                                                                 (4, 'student', 'practice', '2025-05-10 10:00:00'), -- log_id: 2, 学生提交练习
                                                                 (5, 'student', 'answer', '2025-05-10 10:30:00'); -- log_id: 3, 学生提问

-- --------------------------------------
-- 11. 插入 EfficiencyMetrics 表数据（依赖 Course, User）
-- 描述：记录教学效率和学生学习效果。
-- --------------------------------------
INSERT INTO EfficiencyMetrics (course_id, teacher_id, prep_time, correction_time, avg_correct_rate, high_freq_error_point, optimization_suggestion) VALUES
                                                                                                                                                        (1, 2, 3600, 1800, 50.00, '循环', '增加循环示例讲解'), -- metric_id: 1
                                                                                                                                                        (2, 3, 3000, 1500, 50.00, 'SQL 查询', '强化 SQL 条件练习'); -- metric_id: 2

-- --------------------------------------
-- 12. 插入 ChatMemory 表数据（依赖 User）
-- 描述：记录大模型对话历史，支持上下文记忆。
-- --------------------------------------
INSERT INTO ChatMemory (user_id, question_text, answer_text) VALUES
                                                                 (4, 'Python for 循环如何使用？', 'for 循环用于遍历序列，如：for i in range(5): print(i)'), -- memory_id: 1
                                                                 (4, '如何写嵌套循环？', '嵌套循环是循环中包含循环，例如：for i in range(3): for j in range(2): print(i, j)'), -- memory_id: 2
                                                                 (5, 'SQL SELECT 语句是什么？', 'SELECT 用于从数据库查询数据，如：SELECT * FROM table'); -- memory_id: 3

-- --------------------------------------
-- 验证数据（可选查询）
-- 说明：以下查询可用于检查插入数据是否正确。
-- --------------------------------------
/*
-- 验证 User 表
SELECT * FROM User;

-- 验证 LessonPlan 表关联
SELECT lp.*, u.username, c.course_name
FROM LessonPlan lp
JOIN User u ON lp.teacher_id = u.user_id
JOIN Course c ON lp.course_id = c.course_id;

-- 验证 ChatMemory 表
SELECT * FROM ChatMemory WHERE user_id = 4;

-- 验证 PracticeRecord 表
SELECT p.*, q.knowledge_point
FROM PracticeRecord p
JOIN Question q ON p.question_id = q.question_id
WHERE p.student_id = 4;
*/