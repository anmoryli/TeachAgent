package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:48
 */

@Data
public class Question {
    private int questionId;
    private int lessonPlanId;
    private String questionText;
    private String questionType;
    private String referenceAnswer;
    private String knowledgePoint;
    private Date createTime;
    private Date updatedTime;

    public Question(Integer lessonPlanId, String question, String questionType, String referenceAnswer, String knowledgePoint) {
    }

    public Question() {

    }
}
