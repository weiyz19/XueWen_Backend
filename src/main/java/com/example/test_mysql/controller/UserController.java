/**
 * weiyz19
 * MainController.java
 * 2021-08-17
 */
package com.example.test_mysql.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.test_mysql.domain.JsonResult;
import com.example.test_mysql.domain.MyUser;
import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.service.UserService;
import com.example.test_mysql.domain.JsonResult;
@Controller // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /demo (after Application path)
public class UserController {
  @Autowired
  private UserRepo userRepository;
  @Autowired
  private UserService userService;
  
  @PostMapping(path="/register", produces = "application/json;charset=UTF-8") // Map ONLY POST Requests
  public @ResponseBody JsonResult addNewUser (@RequestParam String username,
      @RequestParam String email, @RequestParam String phone, @RequestParam String password) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request
	  MyUser n = new MyUser();
	  n.setUsername(username);
	  n.setEmail(email);
	  n.setPhone(phone);
	  n.setHashedpassword(password);
	  int status = userService.addUser(n);
	  if (status == 1) {
		  return new JsonResult<>("200", "注册成功");  
	  }
	  else if (status == 2) {
		  return new JsonResult<>("205", "用户名已被使用");
	  }
	  else {
		  return new JsonResult<>("201", "用户名和密码不能为空");
	}
  }
  
//  @PostMapping(path="/login") // Map ONLY POST Requests
//  public @ResponseBody String login (@RequestParam String username
//      , @RequestParam String password) {
//	if(userService.login(username, password))
//		return "successfully logged in!";
//	else return "username = " + username  + "  password = " + password + 
//			"\nfailed to log in";
//  }
  

  @GetMapping(path="/showall", produces = "application/json;charset=UTF-8")
  public @ResponseBody JsonResult getAllUsers() {
    return new JsonResult<>(userRepository.findAll(), "用户名和密码不能为空", "200");
  }
}