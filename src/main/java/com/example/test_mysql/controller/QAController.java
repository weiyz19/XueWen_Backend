/**
 * weiyz19
 * QAController.java
 * 2021-09-07
 */
package com.example.test_mysql.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.example.test_mysql.config.HttpUtil;
import com.example.test_mysql.config.JwtTokenUtil;
import com.example.test_mysql.service.AuthService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RequestMapping(path="/ask")
@Controller // This means that this class is a Controller
public class QAController {
	// 用于进行知识问答
	
	@Autowired
	private AuthService authService;
	
	final String[] courses = {"chinese", "math", "english", "physics",
			"chemistry", "biology", "history", "geo", "politics"};
	
	static final String EDUKG_QA = "http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion";
	static final String EDUKG_LINK = "http://open.edukg.cn/opedukg/api/typeOpen/open/linkInstance";
	
	@PostMapping(path="/helper", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject QAwithEDUKG(HttpServletRequest request) {
		MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
		// 课程
		params.put("course", new ArrayList<String>(Arrays.asList(courses[Integer.parseInt(request.getParameter("course"))])));
		// 内容
		params.put("inputQuestion", new ArrayList<String>(Arrays.asList(request.getParameter("question"))));
		JSONArray answerArray = new JSONArray();
		JSONObject resJsonObject = new JSONObject();
		try {
			JSONArray response = JSONObject.fromObject(HttpUtil.sendPostRequest(EDUKG_QA, params)).getJSONArray("data");
			if (response.getJSONObject(0).getString("value").equals("")) {
				answerArray.add("不好意思我太笨了，没办法解答您的疑惑呢");
			}
			else {
				for (int i = 0; i < response.size(); i++) answerArray.add(response.getJSONObject(i).get("value"));
			}
			resJsonObject.put("code", "0");
			resJsonObject.put("msg", "success");
			resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			resJsonObject.put("data", answerArray);
		} catch (Exception e) {
			resJsonObject.put("code", "1");
			resJsonObject.put("msg", "failed");
			resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			resJsonObject.put("data", "");
		}
		return resJsonObject;
	}
	
	@PostMapping(path="/link", produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONObject linkEntity(HttpServletRequest request) {
		MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
		// 课程
		String courseString = request.getParameter("course");
		params.put("course", new ArrayList<>(Arrays.asList(courses[Integer.parseInt(courseString)])));
		// 内容
		params.put("context", new ArrayList<>(Arrays.asList(request.getParameter("context"))));
		JSONObject resJsonObject = new JSONObject();
		try {
			JSONArray response = JSONObject.fromObject(HttpUtil.sendPostRequest(EDUKG_LINK, params)).getJSONObject("data").getJSONArray("results");
			for (int i = 0; i < response.size(); ++i) {
				// 去掉URI
				response.getJSONObject(i).remove("entity_url");
				// 增加科目
				response.getJSONObject(i).put("sbj", courseString);
			}
			resJsonObject.put("code", "0");
			resJsonObject.put("msg", "success");
			resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			resJsonObject.put("data", response);
		} catch (Exception e) {
			resJsonObject.put("code", "1");
			resJsonObject.put("msg", "failed");
			resJsonObject.put("token", authService.refresh(request.getParameter(JwtTokenUtil.TOKENHEDER)));
			resJsonObject.put("data", "");
		}
		return resJsonObject;
	}
	
}