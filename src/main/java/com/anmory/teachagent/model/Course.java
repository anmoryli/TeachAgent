package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:46
 */

@Data
public class Course {
    private int courseId;
    private String courseName;
    private String discipline;
    private String description;
    private Date createTime;
    private Date updatedTime;
}
