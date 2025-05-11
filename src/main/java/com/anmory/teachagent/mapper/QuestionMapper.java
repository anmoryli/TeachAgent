package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Question;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
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
}
