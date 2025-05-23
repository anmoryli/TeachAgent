package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.UserMapper;
import com.anmory.teachagent.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 上午11:29
 */

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public int insert(String username, String password, String role, String email, String realName, String code) {
        return userMapper.insert(username, password, role, email, realName, code);
    }

    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    public int update(int userId, String username, String password, String role, String email) {
        return userMapper.update(userId, username, password, role, email);
    }

    public int deleteByName(String username) {
        return userMapper.deleteByName(username);
    }

    public List<User> selectAll() {
        return userMapper.selectAll();
    }
}
