package com.anmory.teachagent.controller;

import com.anmory.teachagent.model.Material;
import com.anmory.teachagent.model.User;
import com.anmory.teachagent.service.MaterialService;
import com.anmory.teachagent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @Autowired
    MaterialService materialService;
    @RequestMapping("/login")
    public boolean login(String username, String password, String role, HttpSession session){
        User user = userService.selectByName(username);
        if (user == null || !user.getPassword().equals(password) || !user.getRole().equals(role)) {
            return false;
        }
        session.setAttribute("session_user_key", user);
        return true;
    }

    @RequestMapping("/viewUsers")
    public List<User> viewUsers() {
        return userService.selectAll();
    }

    @RequestMapping("/addUser")
    public boolean addUser(String username, String password, String role, String email) {
        return userService.insert(username, password, role, email) > 0;
    }

    @RequestMapping("/viewResources")
    public List<Material> viewResources() {
        return materialService.selectAll();
    }

    @RequestMapping("/viewDashboard")
    public String viewDashboard() {
        return "dashboard";
    }
}
