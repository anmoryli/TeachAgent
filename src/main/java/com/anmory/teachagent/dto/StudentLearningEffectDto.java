package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-24 下午11:23
 */

@Data
public class StudentLearningEffectDto {
    private int studentId;
    private String knowledgePoint;
    private double avgCorrectRate;
    private String commonErrors;
    private String teachingSuggestions;
}
