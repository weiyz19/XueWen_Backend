/**
 * weiyz19
 * JwtAuthentication.java
 * 2021-08-23
 */
package com.example.test_mysql.domain;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.test_mysql.config.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private String tokenHeader = "Authorization";

    private String tokenHead = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
    	String authHeader = request.getParameter(this.tokenHeader);
    	String username = null;
        String authToken = null;
        if (authHeader != null && authHeader.startsWith(tokenHead)) {
            authToken = authHeader.substring(tokenHead.length()); // The part after "Bearer "
           try {
        	   username = jwtTokenUtil.getUserNameFromToken(authToken);
           } catch (IllegalArgumentException e) {
               logger.error("an error occured during getting username from token", e);
           } catch (ExpiredJwtException e) {
               logger.warn("the token is expired and not valid anymore", e);
           }
        } else {
        	logger.warn("couldn't find bearer string, will ignore the header");
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        	UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                logger.info("authenticated user " + username + ", setting security context");
                // 传入上下文 完成认证
                SecurityContextHolder.getContext().setAuthentication(authentication);
                }
        }
        chain.doFilter(request, response);
    }
}