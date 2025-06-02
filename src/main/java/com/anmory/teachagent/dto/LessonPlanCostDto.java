package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-06-02 下午1:51
 */

@Data
public class LessonPlanCostDto {
    private String module;
    private long avgCostTime;
    private long minCostTime;
    private long maxCostTime;
    private int activityCount;
}
