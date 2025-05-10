package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.ChatMemoryMapper;
import com.anmory.teachagent.model.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午4:05
 */

@Service
public class ChatMemoryService {
    @Autowired
    ChatMemoryMapper chatMemoryMapper;

    public int insert(int userId, String questionText, String answerText) {
        return chatMemoryMapper.insert(userId, questionText, answerText);
    }

    public List<ChatMemory> selectByUserId(int userId) {
        return chatMemoryMapper.selectByUserId(userId);
    }
}
