package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:49
 */

@Data
public class PracticeRecord {
    private int practiceId;
    private int studentId;
    private int questionId;
    private String submittedAnswer;
    private Boolean isCorrect;
    private String errorAnalysis;
    private Date submittedAt;
}
