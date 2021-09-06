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
@RequestMapping(path="/exercise") // This means URL's start with /demo (after Application path)
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
		String userID = request.getParameterValues("userID")[0];
		JSONArray exerciseList = exerciseService.findExercises(name, userID);
		if (exerciseList == null) {
			Map<String,String> params = new HashMap<>();
			params.put("uriName", name);
			JSONObject response = JSONObject.fromObject(HttpUtil.sendGetRequest(EDUKG_EXERCISE, params));
			exerciseList = exerciseService.updateExercise(name, response.getJSONArray("data"));
		}
		Map<String, String> resMap = new HashMap<>();
		resMap.put("msg", "找到结果");
		resMap.put("code", "0");
		resMap.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		JSONObject resjson = JSONObject.fromObject(resMap);
		resjson.put("data", exerciseList);
		return resjson;
	}
	
	
	/** 接收历史记录的内容包括：习题ID，是否正确，选择的选项 */
	@GetMapping(path="/log", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject getEntityDetail (
			HttpServletRequest request) {
    // @ResponseBody means the returned String is the response, not a view name
		List<String> params = new LinkedList<>();
		params.add(request.getParameterValues("userID")[0]);
		exerciseService.updateHistory(params);
		JSONObject datajson = new JSONObject();
		datajson.put("msg", "更新成功");
		datajson.put("code", "0");
		datajson.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
		datajson.put("data", "");
		return datajson;
	}
}