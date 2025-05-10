package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:50
 */

@Data
public class Answer {
    private int answerId;
    private int studentId;
    private String questionText;
    private String answerText;
    private Date createTime;
    private Date updatedTime;
}
