package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 上午11:21
 */

@Data
public class LessonPlanDto {
    private int lessonPlanId;
    private int teacherId;
    private int courseId;
    private String title;
    private String content;
}
