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
 * @description 管理员界面接口
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

    @RequestMapping("/register")
    public boolean register(String username, String password, String role, String email, String realName, String code) {
        return userService.insert(username, password, role, email, realName, code) > 0;
    }

    @RequestMapping("/viewUsers")
    public List<User> viewUsers() {
        return userService.selectAll();
    }

    @RequestMapping("/addUser")
    public boolean addUser(String username, String password, String role, String email, String realName, String code) {
        return userService.insert(username, password, role, email, realName, code) > 0;
    }

    @RequestMapping("/deleteUser")
    public boolean deleteUser(String username) {
        return userService.deleteByName(username) > 0;
    }

    @RequestMapping("/updateUser")
    public boolean updateUser(String username, String password, String role, String email) {
        User user = userService.selectByName(username);
        return userService.update(user.getUserId(), username, password, role, email) > 0;
    }

    @RequestMapping("/viewResources")
    public List<Material> viewResources() {
        return materialService.selectAll();
    }

    @RequestMapping("/viewDashboard")
    public String viewDashboard() {
        // TODO
        return "dashboard";
    }

    @RequestMapping("/exportResource")
    public boolean exportResource(String title, String filePath, String materialType) {
        // TODO
        return materialService.insert(title, filePath, materialType) > 0;
    }
}
