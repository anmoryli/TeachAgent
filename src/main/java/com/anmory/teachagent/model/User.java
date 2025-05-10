package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:46
 */

@Data
public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String email;
    private Date createTime;
    private Date updatedTime;
}
