/**
 * weiyz19
 * JwtAuthenticationEntryPoint.java
 * 2021-08-23
 */
package com.example.test_mysql.domain;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
	private static final long serialVersionUID = 838173925310672174L;
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
    	response.setContentType("application/json;charset=UTF-8");
    	response.getWriter().append("{\"code\":2,\"msg\":\"没有凭证！请先登录！\",\"data\":\"\"}");
    	// 如果认证时出错 则返回没有权限 错误代码为2
    }
}

