/**
 * weiyz19
 * MainController.java
 * 2021-08-17
 */
package com.example.test_mysql.controller;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.test_mysql.config.JwtTokenUtil;
import com.example.test_mysql.domain.AuthenticationException;
import com.example.test_mysql.domain.JsonResult;
import com.example.test_mysql.domain.JwtAuthenticationResponse;
import com.example.test_mysql.domain.MyUser;
import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.service.AuthService;
import com.example.test_mysql.service.UserService;

import net.sf.json.JSONObject;

@Controller // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /demo (after Application path)
public class UserController {
	@Autowired
	private UserRepo userRepository;
	@Autowired
	private AuthService authService;
	@Autowired
	private UserService userService;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
  
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

	@PostMapping(path="/refresh", produces = "application/json;charset=UTF-8")
	public @ResponseBody JwtAuthenticationResponse refreshAndGetToken(HttpServletRequest request) {
		String refreshedToken = authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER));
		if(refreshedToken == null) {
			return new JwtAuthenticationResponse<>("", "token刷新失败!", "1", "");
		} else {
			return new JwtAuthenticationResponse<>("", "token刷新成功!", "0", refreshedToken);
		}
	}
  
	@GetMapping(path="/favor/get", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getUserFavor(HttpServletRequest request) {
		JSONObject resJsonObject = new JSONObject();
		resJsonObject.put("code", "0");
		resJsonObject.put("msg", "success");
		resJsonObject.put("data", userService.getUserFavor(request.getParameterValues("userID")[0], request.getParameter("type")));
		resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		return resJsonObject;
	}
	
	@PostMapping(path="/favor/update", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject addUserFavor(HttpServletRequest request) {
		// 0 表示实体操作   1 表示习题操作
		int type = Integer.parseInt(request.getParameter("type"));
		// 0 表示添加   1 表示删除 (可理解为该条目的当前状态)
		int optype = Integer.parseInt(request.getParameter("op"));
		// 对于实体来讲是name  对于习题来讲是id
		String index = request.getParameter("index");
		List<Object> params = new LinkedList<>();
		params.add(type);
		params.add(index);
		params.add(Integer.parseInt(request.getParameterValues("userID")[0]));
		if (type == 0) {
			// 实体需要再加入科目 直接以字符串形式存储即可
			params.add(request.getParameter("sbj"));
		}
		JSONObject resJsonObject = new JSONObject();
		if (userService.updateUserFavor(params, optype)){
			resJsonObject.put("code", "0");
			resJsonObject.put("msg", "success");
		} 
		else {
			resJsonObject.put("code", "1");
			resJsonObject.put("msg", "failed");
		}
		resJsonObject.put("data", "");
		resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		return resJsonObject;
	}
}