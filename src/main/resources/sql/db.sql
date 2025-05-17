-- 删除表顺序：从最深层依赖表到无依赖表

-- 1. 删除最深层依赖表
DROP TABLE IF EXISTS PracticeRecord;
DROP TABLE IF EXISTS Answer;
DROP TABLE IF EXISTS ActivityLog;
DROP TABLE IF EXISTS Statistics;
DROP TABLE IF EXISTS EfficiencyMetrics;
DROP TABLE IF EXISTS ChatMemory;

-- 2. 删除次层依赖表
DROP TABLE IF EXISTS Question;

-- 3. 删除更浅层依赖表
DROP TABLE IF EXISTS LessonPlan;
DROP TABLE IF EXISTS Material;

-- 4. 删除无依赖表
DROP TABLE IF EXISTS Course;
DROP TABLE IF EXISTS User;

-- 用户表：存储管理员、教师、学生信息
CREATE TABLE User (
                      user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      role ENUM('admin', 'teacher', 'student') NOT NULL,
                      email VARCHAR(100),
                      create_time datetime DEFAULT now(),
                      updated_time datetime DEFAULT now() ON UPDATE now()
);

-- 课程表：存储课程信息（知识库来源）
CREATE TABLE Course (
                        course_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        course_name VARCHAR(100) NOT NULL,
                        discipline VARCHAR(50) NOT NULL, -- 学科，如计算机
                        description TEXT,
                        create_time datetime DEFAULT now(),
                        updated_time datetime DEFAULT now() ON UPDATE now()
);

-- 课件表：存储课件资源（知识库内容）
CREATE TABLE Material (
                          material_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          title VARCHAR(100) NOT NULL,
                          file_path VARCHAR(255) NOT NULL, -- 存储文件路径，避免大文件直接存入数据库
                          material_type ENUM('document', 'exercise', 'other') NOT NULL default 'document',
                          create_time datetime DEFAULT now(),
                          updated_time datetime DEFAULT now() ON UPDATE now()
);

-- 备课表：存储教师备课设计
CREATE TABLE LessonPlan (
                            lesson_plan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            teacher_id BIGINT NOT NULL,
                            course_id BIGINT NOT NULL,
                            title text NOT NULL,
                            content TEXT NOT NULL, -- 教学内容（知识讲解、实训练习、时间分布等）
                            create_time datetime DEFAULT now(),
                            updated_time datetime DEFAULT now() ON UPDATE now(),
                            FOREIGN KEY (teacher_id) REFERENCES User(user_id) ON DELETE CASCADE,
                            FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);

-- 题目表：存储考核题目及参考答案
CREATE TABLE Question (
                          question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          lesson_plan_id BIGINT NOT NULL,
                          question_text TEXT NOT NULL,
                          question_type ENUM('multiple_choice', 'programming', 'short_answer') NOT NULL,
                          reference_answer TEXT NOT NULL, -- 参考答案
                          knowledge_point VARCHAR(100) NOT NULL, -- 关联知识点说明
                          create_time datetime DEFAULT now(),
                          updated_time datetime DEFAULT now() ON UPDATE now(),
                          FOREIGN KEY (lesson_plan_id) REFERENCES LessonPlan(lesson_plan_id) ON DELETE CASCADE
);

-- 练习记录表：存储学生练习记录
CREATE TABLE PracticeRecord (
                                practice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                student_id BIGINT NOT NULL,
                                question_id BIGINT NOT NULL,
                                submitted_answer TEXT NOT NULL,
                                is_correct BOOLEAN NOT NULL,
                                error_analysis TEXT, -- 错误定位与修正建议
                                submitted_at datetime DEFAULT now(),
                                FOREIGN KEY (student_id) REFERENCES User(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (question_id) REFERENCES Question(question_id) ON DELETE CASCADE
);

-- 答案表：存储学生在线问答记录
CREATE TABLE Answer (
                        answer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        student_id BIGINT NOT NULL,
                        question_text TEXT NOT NULL, -- 学生提问
                        answer_text TEXT NOT NULL, -- 大模型生成的回答
                        create_time datetime DEFAULT now(),
                        updated_time datetime DEFAULT now() ON UPDATE now(),
                        FOREIGN KEY (student_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 统计表：存储学情数据分析
CREATE TABLE Statistics (
                            stat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            course_id BIGINT NOT NULL,
                            student_id BIGINT NOT NULL,
                            knowledge_point VARCHAR(100) NOT NULL,
                            correct_rate DECIMAL(5,2) NOT NULL, -- 正确率
                            common_errors TEXT, -- 常见错误
                            teaching_suggestion TEXT, -- 教学建议
                            create_time datetime DEFAULT now(),
                            updated_time datetime DEFAULT now() ON UPDATE now(),
                            FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
                            FOREIGN KEY (student_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 活跃日志表：存储教师和学生使用活跃数据
CREATE TABLE ActivityLog (
                             log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT NOT NULL,
                             role ENUM('teacher', 'student') NOT NULL,
                             module VARCHAR(50) NOT NULL, -- 活跃板块（如备课、练习）
                             action_time datetime DEFAULT now(),
                             FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 效率指标表：存储教学效率和学生学习效果统计
CREATE TABLE EfficiencyMetrics (
                                   metric_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   course_id BIGINT NOT NULL,
                                   teacher_id BIGINT NOT NULL,
                                   prep_time INT, -- 备课耗时（秒）
                                   correction_time INT, -- 批改耗时（秒）
                                   avg_correct_rate DECIMAL(5,2), -- 学生平均正确率
                                   high_freq_error_point VARCHAR(100), -- 高频错误知识点
                                   optimization_suggestion TEXT, -- 课程优化建议
                                   create_time datetime DEFAULT now(),
                                   updated_time datetime DEFAULT now() ON UPDATE now(),
                                   FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
                                   FOREIGN KEY (teacher_id) REFERENCES User(user_id) ON DELETE CASCADE
);

# 大模型对话记忆存储，与教学项目关系不大
create table ChatMemory(
    memory_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    answer_text TEXT NOT NULL,
    create_time datetime DEFAULT now(),
    updated_time datetime DEFAULT now() ON UPDATE now(),
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

# 大模型提示词存储
create table Prompt(
    prompt_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prompt_text TEXT NOT NULL,
    create_time datetime DEFAULT now(),
    updated_time datetime DEFAULT now() ON UPDATE now()
);

CREATE TABLE KnowledgeDocument (
    document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    chapter_section VARCHAR(50),
    keywords VARCHAR(255),
    course_id BIGINT NOT NULL,
    vector_embedding BLOB, -- 向量嵌入存储为二进制
    create_time datetime DEFAULT now(),
    updated_time datetime DEFAULT now() ON UPDATE now(),
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);

-- 索引优化
CREATE INDEX idx_user_role ON User(role);
CREATE INDEX idx_lesson_plan_teacher ON LessonPlan(teacher_id);
CREATE INDEX idx_practice_student ON PracticeRecord(student_id);
CREATE INDEX idx_activity_log_action_time ON ActivityLog(action_time);
CREATE INDEX idx_knowledge_document_course ON KnowledgeDocument(course_id);
