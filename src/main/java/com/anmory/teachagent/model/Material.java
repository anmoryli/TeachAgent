package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:41
 */

@Data
public class Material {
    private int materialId;
    private int courseId;
    private String title;
    private String filePath;
    private String materialType;
    private Date createTime;
    private Date updatedTime;
}
