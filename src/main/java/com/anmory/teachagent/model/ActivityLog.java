package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:52
 */

@Data
public class ActivityLog {
    private int logId;
    private int userId;
    private String role;
    private String module;
    private Date actionTime;
    private long costTime;
}
