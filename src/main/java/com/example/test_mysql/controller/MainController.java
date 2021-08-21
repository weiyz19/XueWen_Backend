/**
 * weiyz19
 * MainController.java
 * 2021-08-17
 */
package com.example.test_mysql.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller // This means that this class is a Controller
public class MainController {

	@RequestMapping(path="/") // This means URL's start with / (after Application path)
	public String showHome () {
		return "home";
  }
}