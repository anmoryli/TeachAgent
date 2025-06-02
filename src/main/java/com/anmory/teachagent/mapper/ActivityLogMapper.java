package com.anmory.teachagent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-06-02 下午1:21
 */

@Mapper
public interface ActivityLogMapper {
    @Insert("insert into ActivityLog (user_id, role, module, action_time, cost_time) values (#{userId}, #{role}, #{module}, now(), #{costTime})")
    int insert(int userId, String role, String module, long costTime);
}
