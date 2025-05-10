package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.PromptMapper;
import com.anmory.teachagent.model.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午5:15
 */

@Service
public class PromptService {
    @Autowired
    PromptMapper promptMapper;

    public Prompt selectByPromptId(int promptId) {
        return promptMapper.selectByPromptId(promptId);
    }
}
