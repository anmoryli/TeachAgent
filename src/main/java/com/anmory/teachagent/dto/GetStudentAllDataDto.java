package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-24 下午11:18
 */

@Data
public class GetStudentAllDataDto {
    private int studentId;
    private String knowledgePoint;
    private double avgCorrectRate;
    private String commonErrors;
    private String teachingSuggestions;
}
