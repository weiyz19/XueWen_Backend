/**
 * weiyz19
 * ExerciseController.java
 * 2021-08-30
 */
package com.example.test_mysql.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.test_mysql.config.HttpUtil;
import com.example.test_mysql.config.JwtTokenUtil;
import com.example.test_mysql.service.AuthService;
import com.example.test_mysql.service.EntityService;
import com.example.test_mysql.service.ExerciseService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller // This means that this class is a Controller
@RequestMapping(path="/exercise")
public class ExerciseController {
	@Autowired
	private ExerciseService exerciseService;
	@Autowired
	private AuthService authService;
	
	static final String EDUKG_EXERCISE = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName";
	
	/** 拿到一个实体的相关习题 */
	@PostMapping(path="/get", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject findEntity (
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		String name = request.getParameter("name");
		String course = request.getParameter("course");
		String userID = HttpUtil.getuserID(request);
		JSONArray exerciseList = exerciseService.findExercises(name, userID, course);
		if (exerciseList == null) {
			Map<String,String> params = new HashMap<>();
			params.put("uriName", name);
			JSONObject response = JSONObject.fromObject(HttpUtil.sendGetRequest(EDUKG_EXERCISE, params));
			exerciseList = exerciseService.updateExercise(name, course, response.getJSONArray("data"));
		}
		JSONObject resJsonObject = new JSONObject();
		resJsonObject.put("msg", "找到结果");
		resJsonObject.put("code", "0");
		resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		resJsonObject.put("data", exerciseList);
		return resJsonObject;
	}
	
	/** 
	 * 进行指定学科的专项复习
	 * 采用了艾宾浩斯记忆法
	 */
	@PostMapping(path="/review", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getbyEbbinghaus(
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		String userID = HttpUtil.getuserID(request);
		JSONArray exerciseList = exerciseService.getEbbinghaus(userID, request.getParameter("course"));
		JSONObject responseObject = new JSONObject();
		if (exerciseList == null) {
			responseObject.put("msg", "failed");
			responseObject.put("code", "1");
			responseObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			responseObject.put("data", "");
		}
		responseObject.put("msg", "succeed");
		responseObject.put("code", "0");
		responseObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		responseObject.put("data", exerciseList);
		return responseObject;
	}
	
	/** 
	 * 拿到用户指定学科的推荐习题
	 */
	@PostMapping(path="/recommend", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getRecommend(
			HttpServletRequest request) {
		String userID = HttpUtil.getuserID(request);
		JSONArray exerciseList = exerciseService.getRecommend(userID, request.getParameter("course"));
		JSONObject responseObject = new JSONObject();
		if (exerciseList == null) {
			responseObject.put("msg", "failed");
			responseObject.put("code", "1");
			responseObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			responseObject.put("data", "");
		}
		responseObject.put("msg", "succeed");
		responseObject.put("code", "0");
		responseObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		responseObject.put("data", exerciseList);
		return responseObject;
	}
	
	
	/** 接收历史记录的内容包括：习题ID，是否正确，选择的选项 */
	@PostMapping(path="/log", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getEntityDetail (
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		List<String> params = new LinkedList<>();
		params.add(HttpUtil.getuserID(request));
		params.add(request.getParameter("id"));
		params.add(request.getParameter("idx"));
		exerciseService.updateHistory(params);
		JSONObject datajson = new JSONObject();
		datajson.put("msg", "更新成功");
		datajson.put("code", "0");
		datajson.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		datajson.put("data", "");
		return datajson;
	}
}