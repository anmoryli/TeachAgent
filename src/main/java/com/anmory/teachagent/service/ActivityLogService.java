package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.ActivityLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-06-02 下午1:23
 */

@Service
public class ActivityLogService {
    @Autowired
    ActivityLogMapper activityLogMapper;

    public  int insert(int userId, String role, String module, long costTime) {
        return activityLogMapper.insert(userId, role, module, costTime);
    }
}
