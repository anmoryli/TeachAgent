package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.User;
import com.anmory.teachagent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-10 下午9:48
 */

@RestController
@RequestMapping("/admin")
public class UserController {
    @Autowired
    UserService userService;
    @RequestMapping("/login")
    public boolean login(String username, String password, String role, HttpSession session){
        User user = userService.selectByName(username);
        if (user == null || !user.getPassword().equals(password) || !user.getRole().equals(role)) {
            return false;
        }
        session.setAttribute("session_user_key", user);
        return true;
    }
}
