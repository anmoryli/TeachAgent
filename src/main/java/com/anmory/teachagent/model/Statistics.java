package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:50
 */

@Data
public class Statistics {
    private int statId;
    private int courseId;
    private int studentId;
    private String knowledgePoint;
    private Double correctRate;
    private String commonErrors;
    private String teachingSuggestion;
    private Date createTime;
    private Date updatedTime;
}
