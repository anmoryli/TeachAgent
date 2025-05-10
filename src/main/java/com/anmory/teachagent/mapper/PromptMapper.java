package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Prompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-10 下午5:14
 */

@Mapper
public interface PromptMapper {
    @Select("select * from Prompt where prompt_id = #{promptId}")
    Prompt selectByPromptId(int promptId);
}
