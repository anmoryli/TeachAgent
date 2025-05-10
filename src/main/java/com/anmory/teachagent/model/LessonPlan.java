package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:47
 */

@Data
public class LessonPlan {
    private Integer lessonPlanId;
    private int teacherId;
    private int courseId;
    private String title;
    private String content;
    private Date createTime;
    private Date updatedTime;
}
