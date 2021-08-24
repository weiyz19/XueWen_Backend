/**
 * weiyz19
 * MainController.java
 * 2021-08-17
 */
package com.example.test_mysql.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.test_mysql.domain.AuthenticationException;
import com.example.test_mysql.domain.JsonResult;
import com.example.test_mysql.domain.JwtAuthenticationRequest;
import com.example.test_mysql.domain.JwtAuthenticationResponse;
import com.example.test_mysql.domain.MyUser;
import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.service.AuthService;

@Controller // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /demo (after Application path)
public class UserController {
	@Autowired
	private UserRepo userRepository;
	@Autowired
	private AuthService authService;
  
	private String tokenHeader = "Authorization";
  
	@PostMapping(path="/register", produces = "application/json;charset=UTF-8")
	public @ResponseBody JsonResult addNewUser (@RequestParam String username,
			@RequestParam String email, @RequestParam String phone, @RequestParam String password) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request
		if( username.equals("") || password.equals("")) 
			return new JsonResult<>("", "用户名和密码不能为空", "2");
		MyUser n = new MyUser();
		n.setUsername(username);
		n.setEmail(email);
		n.setPhone(phone);
		n.setHashedpassword(password);
		int status = authService.register(n);
		if (status == 0) {
			return new JsonResult<>("", "注册成功", "0");  
		}
		else {
			return new JsonResult<>("", "用户名已被使用", "1");
		}
	}

	@PostMapping(path="/login", produces = "application/json;charset=UTF-8")
	public @ResponseBody JwtAuthenticationResponse userlogin(
			@RequestParam String username, @RequestParam String password) throws AuthenticationException{
		final String token = authService.login(username, password);
		if (token.equals("")) {
			return new JwtAuthenticationResponse<>("", "登录失败!", "1", token);
		}
		return new JwtAuthenticationResponse<>("", "登录成功!", "0", token);
	}

	@GetMapping(path="/refresh", produces = "application/json;charset=UTF-8")
	public JwtAuthenticationResponse refreshAndGetToken(
          HttpServletRequest request) throws AuthenticationException{
		String refreshedToken = authService.refresh(request.getParameter(tokenHeader));
		if(refreshedToken == null) {
			return new JwtAuthenticationResponse<>("", "token刷新失败!", "1", "");
		} else {
			return new JwtAuthenticationResponse<>("", "token刷新成功!", "0", refreshedToken);
		}
	}
  
	@GetMapping(path="/showall", produces = "application/json;charset=UTF-8")
	public @ResponseBody JwtAuthenticationResponse getAllUsers(HttpServletRequest request) {
		return new JwtAuthenticationResponse<>(userRepository.findAll(), "success", "0", authService.refresh(request.getParameter(tokenHeader)));
	}
}