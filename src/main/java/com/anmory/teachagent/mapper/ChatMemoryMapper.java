package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.ChatMemory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-10 下午4:04
 */

@Mapper
public interface ChatMemoryMapper {
    @Insert("insert into ChatMemory (user_id, question_text, answer_text) values (#{userId}, #{questionText}, #{answerText})")
    int insert(int userId, String questionText, String answerText);

    @Select("select * from ChatMemory where user_id = #{userId}")
    List<ChatMemory> selectByUserId(int userId);
}
