package com.anmory.teachagent.interceptor;

import com.anmory.teachagent.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-25 下午8:14
 */

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("进入拦截器");
        HttpSession session = request.getSession(false);
        if(!checkSession(session)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("用户未登录");
            return false;
        }
        return true;
    }

    private boolean checkSession(HttpSession session) {
        if(session == null) {
            return false;
        }
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return false;
        }
        return true;
    }
}
