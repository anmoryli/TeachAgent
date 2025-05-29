package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.LessonPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-10 上午11:10
 */

@Mapper
public interface LessonPlanMapper {
    @Insert("insert into LessonPlan (teacher_id, course_id, title, content) values (#{teacherId}, #{courseId}, #{title}, #{content})")
    int insert(int teacherId, int courseId, String title, String content);

    @Select("select * from LessonPlan")
    List<LessonPlan> selectAll();

    @Delete("delete from LessonPlan where course_id = #{courseId}")
    int deleteByCourseId(int courseId);

    @Select("select * from LessonPlan where lesson_plan_id = #{lessonPlanId}")
    LessonPlan selectOne(Integer lessonPlanId);

    @Update("update LessonPlan set teacher_id = #{teacherId}, course_id = #{courseId}, title = #{title}, content = #{content} where lesson_plan_id = #{lessonPlanId}")
    int update(Integer lessonPlanId, int teacherId, int courseId, String title, String content);

    @Select("select lesson_plan_id from LessonPlan where course_id = #{courseId} limit 1")
    int getLessonPlanIdByCourseId(int courseId);

    @Select("select * from LessonPlan where course_id = (select course_id from Course where course_name = #{courseName})")
    List<LessonPlan> selectByCourseName(String courseName);

    @Select("select * from LessonPlan where course_id = " +
            "(select course_id from Course where course_name = #{courseName}) limit 1")
    LessonPlan selectOneByCourseName(String courseName);

    @Select("select * from LessonPlan where title = #{lessonPlanName}")
    List<LessonPlan> selectByLessonPlanName(String lessonPlanName);

    @Select("select * from LessonPlan where course_id = #{courseId}")
    List<LessonPlan> selectByCourseId(int courseId);

    @Delete("delete from LessonPlan where lesson_plan_id = #{lessonPlanId}")
    int deleteByLessonPlanId(int lessonPlanId);
}
