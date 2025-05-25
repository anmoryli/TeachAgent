package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-24 下午11:16
 */

@Data
public class JudgeStudentAnswerDto {
    private int studentId;
    private int questionId;
    private String submittedAnswer;
    private boolean isCorrect;
    private String errorAnalysis;
}
