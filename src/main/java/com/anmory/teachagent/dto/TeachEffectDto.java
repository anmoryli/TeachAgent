package com.anmory.teachagent.dto;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-24 下午11:22
 */

@Data
public class TeachEffectDto {
    private int teacherId;
    private double avgPrepTime;
    private double avgCorrectionTime;
    private double avgCorrectRate;
    private String highFreqErrorPoint;
    private String optimizationSuggestion;
}
