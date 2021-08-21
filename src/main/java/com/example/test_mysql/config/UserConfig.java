/**
 * weiyz19
 * UserConfig.java
 * 2021-08-19
 */
package com.example.test_mysql.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
public class UserConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.formLogin()
			//.loginPage("/user/login")			/** 自定义登陆页面 */
			.loginProcessingUrl("/user/login")	/** 自定义登录路径 */
			.defaultSuccessUrl("/user/showall")				/** 自定义登陆成功路径 */
			.and().authorizeRequests()
				.antMatchers("/user/login", "/user/register").permitAll()	/** 设置不需要验证的url */
			.anyRequest().authenticated()
			.and().csrf().disable();		/** 关闭csrf防护 */
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

@Component
class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
    		Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = null;
        try{
        	writer = response.getWriter();
        	writer.write("abaaba");
        }catch (IOException e) {
        	response.sendError(0, "IOException Struck!");
		}
    }

}
//
//@Component
//class LoginFailureHandler implements AuthenticationFailureHandler {
//
//	@Override
//	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
//			AuthenticationException exception) throws IOException, ServletException {
//	}
//
//}
