package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Answer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-11 上午10:34
 */

@Mapper
public interface AnswerMapper {
    @Select("select * from Answer where question_text = #{questionText}")
    Answer selectByQuestionText(String questionText);

    @Insert("insert into Answer (student_id, question_text, answer_text) values (#{studentId}, #{questionText}, #{answerText})")
    int insert(int studentId, String questionText, String answerText);

    @Select("select * from Answer where student_id = #{studentId}")
    List<Answer> selectByStudentId(int studentId);
}
