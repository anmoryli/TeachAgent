package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:52
 */

@Data
public class EfficiencyMetrics {
    private int metricId;
    private int courseId;
    private int teacherId;
    private Integer prepTime;
    private Integer correctionTime;
    private Double avgCorrectRate;
    private String highFreqErrorPoint;
    private String optimizationSuggestion;
    private Date createTime;
    private Date updatedTime;
}
