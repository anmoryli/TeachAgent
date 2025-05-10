package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午5:13
 */

@Data
public class Prompt {
    private int promptId;
    private String promptText;
    private Date createTime;
    private Date updatedTime;
}
