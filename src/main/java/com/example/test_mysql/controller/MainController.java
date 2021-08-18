/**
 * weiyz19
 * MainController.java
 * 2021-08-17
 */
package com.example.test_mysql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.test_mysql.domain.User;
import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.service.UserService;

@Controller // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /demo (after Application path)
public class MainController {
  @Autowired
  private UserRepo userRepository;
  @Autowired
  private UserService userService;
  
  @PostMapping(path="/register") // Map ONLY POST Requests
  public @ResponseBody String addNewUser (@RequestParam String username,
      @RequestParam String email, @RequestParam String phone, @RequestParam String password) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request
    User n = new User();
    n.setUsername(username);
    n.setEmail(email);
    n.setPhone(phone);
    n.setHashedpassword(password);
    userRepository.save(n);
    return "Saved";
  }
  
  @PostMapping(path="/login") // Map ONLY POST Requests
  public @ResponseBody String login (@RequestParam String username
      , @RequestParam String password) {
	if(userService.login(username, password))
		return "successfully logged in!";
	else return "username = " + username  + "  password = " + password + 
			"\nfailed to log in";
  }
  

  @GetMapping(path="/all")
  public @ResponseBody Iterable<User> getAllUsers() {
    // This returns a JSON or XML with the users
    return userRepository.findAll();
  }
}