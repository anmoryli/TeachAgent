package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 上午11:28
 */

@Mapper
public interface UserMapper {
    @Insert("insert into User (username, password, role, email, real_name, code) values " +
            "(#{username}, #{password}, #{role}, #{email}, #{realName}, #{code})")
    int insert(String username, String password, String role, String email,String realName, String code);

    @Select("select * from User where username = #{username}")
    User selectByName(String username);

    @Update("update User set username = #{username}, password = #{password}, role = #{role}, email = #{email} where user_id = #{userId}")
    int update(int userId, String username, String password, String role, String email);

    @Delete("delete from User where username = #{username}")
    int deleteByName(String username);

    @Select("select * from User")
    List<User> selectAll();
}
