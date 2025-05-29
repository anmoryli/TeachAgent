package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-29 下午7:14
 */

@Data
public class CountDto {
    private int courseCnt;
    private int studentCnt;
    private int teacherCnt;
    private int lessonPlanCnt;
    private int questionCnt;
    private int materialCnt;
    private int practiceRecordCnt;
    private int answerCnt;
    private int correctCnt;
    private int incorrectCnt;
}
