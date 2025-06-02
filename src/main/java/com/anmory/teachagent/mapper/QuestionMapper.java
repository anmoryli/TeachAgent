package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午5:27
 */

@Mapper
public interface QuestionMapper {
    @Select("select * from Question where lesson_plan_id = #{lessonPlanId}")
    List<Question> selectByLessonPlanId(int lessonPlanId);

    @Insert("insert into Question (lesson_plan_id, question_text, question_type, reference_answer, knowledge_point) values (#{lessonPlanId}, #{questionText}, #{questionType}, #{referenceAnswer}, #{knowledgePoint})")
    int insert(int lessonPlanId, String questionText, String questionType, String referenceAnswer, String knowledgePoint);

    @Select("select question_text from Question where Question.question_id = #{questionId}")
    String getQuestionTextById(int questionId);

    @Select("select * from Question")
    List<Question> selectAll();

    @Update("update Question set question_text = #{questionText}, question_type = #{questionType}, reference_answer = #{referenceAnswer}, " +
            "knowledge_point = #{knowledgePoint} where question_id = #{questionId}")
    int update(int questionId, String questionText, String questionType, String referenceAnswer, String knowledgePoint);

    @Delete("delete from Question where question_id = #{id}")
    int deleteById(int id);
}
