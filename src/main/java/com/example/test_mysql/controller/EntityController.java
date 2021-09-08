/**
 * weiyz19
 * EntityController.java
 * 2021-08-28
 */
package com.example.test_mysql.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.test_mysql.config.JwtTokenUtil;
import com.example.test_mysql.service.AuthService;
import com.example.test_mysql.service.EntityService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller // This means that this class is a Controller
@RequestMapping(path="/entity") // This means URL's start with /demo (after Application path)
public class EntityController {
	@Autowired
	private EntityService entityService;
	@Autowired
	private AuthService authService;
	
	/** 实体搜索 course为9则为学科模糊查找 */
	@PostMapping(path="/search", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject findEntity (
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		int course = Integer.parseInt(request.getParameter("course"));
		String name = request.getParameter("name");
		JSONArray entities = entityService.findEntities(course, name);
		Map<String, String> resMap = new HashMap<>();
		if (entities == null) {
			resMap.put("msg", "没有找到结果");
			resMap.put("code", "1");
			resMap.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		}
		else {
			resMap.put("msg", "找到结果");
			resMap.put("code", "0");
			resMap.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		}
		JSONObject resjson = JSONObject.fromObject(resMap);
		resjson.put("data", entities);
		return resjson;
	}
	
	@PostMapping(path="/detail", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getEntityDetail (
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		int course = Integer.parseInt(request.getParameter("course"));
		int userID = Integer.parseInt(request.getParameterValues("userID")[0]);
		String name = request.getParameter("name");
		JSONObject myEntity = entityService.getEntity(course, name, userID);
		JSONObject resjson = new JSONObject();
		if (myEntity == null) {
			resjson.put("data", "");
			resjson.put("msg", "没有找到结果");
			resjson.put("code", "1");
			resjson.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		}
		else {
			resjson.put("data", myEntity);
			resjson.put("msg", "找到结果");
			resjson.put("code", "0");
			resjson.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			List<Object> params = new LinkedList<>();
			params.add(request.getParameter("course"));
			params.add(name);
			params.add(Integer.parseInt(request.getParameterValues("userID")[0]));
			entityService.updateHistory(params);
		}
		return resjson;
	}
	
	/** Entities only */
	@PostMapping(path="/history", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getUserHistory(HttpServletRequest request) {
		JSONObject resJsonObject = new JSONObject();
		resJsonObject.put("code", "0");
		resJsonObject.put("msg", "success");
		resJsonObject.put("data", entityService.getHistory(request.getParameterValues("userID")[0]));
		resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		return resJsonObject;
	}
	
	/** Entities only  指定学科 */
	@PostMapping(path="/recommend", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getUserUnique(HttpServletRequest request) {
		JSONObject resJsonObject = new JSONObject();
		resJsonObject.put("code", "0");
		resJsonObject.put("msg", "success");
		resJsonObject.put("data", entityService.getUnique(request.getParameterValues("userID")[0], request.getParameter("course")));
		resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		return resJsonObject;
	}
	
}