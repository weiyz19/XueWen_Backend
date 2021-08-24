/**
 * weiyz19
 * AuthConfig.java
 * 2021-08-24
 */
package com.example.test_mysql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.test_mysql.domain.JwtAuthenticationEntryPoint;
import com.example.test_mysql.domain.JwtAuthenticationTokenFilter;

@Configuration
public class AuthConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter; 
	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	
	//不需要拦截的白名单
    private static final String[] URL_WHITELIST = {
            "/user/login",
            "/user/register"
    };
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(URL_WHITELIST);
    }
    
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()		/** 允许跨域 关闭csrf防护 */
			.formLogin()
				.loginPage("/user/login")						/** 自定义登陆页面 */
				.loginProcessingUrl("/user/login")				/** 自定义登录路径 */
			.and()
				.authorizeRequests()
				.antMatchers(URL_WHITELIST).permitAll()		/** 设置不需要验证的url */
				.anyRequest().authenticated()
			.and()
				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
			.and()		
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		// 禁用缓存
		http.headers().cacheControl();
		http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	@Override
	//授权管理器
	public AuthenticationManager authenticationManagerBean() throws Exception {
	    return super.authenticationManagerBean();
	}
	
}