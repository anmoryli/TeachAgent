package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午4:03
 */

@Data
public class ChatMemory {
    private int memoryId;
    private int userId;
    private String questionText;
    private String answerText;
    private Date createTime;
    private Date updatedTime;
}
