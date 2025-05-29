package com.anmory.teachagent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-29 下午7:10
 */

@Mapper
public interface CountMapper {
    @Select("select count(*) from Course")
    int getCourseCnt();

    @Select("select count(*) from User where role='student'")
    int getStudentCnt();

    @Select("select count(*) from User where role='teacher'")
    int getTeacherCnt();

    @Select("select count(*) from LessonPlan")
    int getLessonPlanCnt();

    @Select("select count(*) from Question")
    int getQuestionCnt();

    @Select("select count(*) from Material")
    int getMaterialCnt();

    @Select("select count(*) from PracticeRecord")
    int getPracticeRecordCnt();

    @Select("select count(*) from Answer")
    int getAnswerCnt();

    @Select("select count(*) from PracticeRecord where is_correct=1")
    int getCorrectCnt();

    @Select("select count(*) from PracticeRecord where is_correct=0")
    int getIncorrectCnt();
}
