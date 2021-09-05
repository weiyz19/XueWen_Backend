/**
 * weiyz19
 * ExerciseService.java
 * 2021-08-30
 */
package com.example.test_mysql.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.test_mysql.domain.ExerciseRepoImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("ExerciseService")
public class ExerciseService {
	
	@Autowired
	private ExerciseRepoImpl exerciseRepoImp;
	
	/** 如果有就不需要 */
	public JSONArray findExercises(String name, String userID) {
		List<String> nameList = new LinkedList<>();
		nameList.add(name);
		nameList.add(userID);
		return exerciseRepoImp.findByNameIn(nameList);
	}
	
	public JSONArray updateExercise(String name, JSONArray exercises) {
		JSONArray exerArray = new JSONArray();
		// 用于存放用于更新的参数
		List<Object> params = new LinkedList<>();
		List<Object> exerciseList = new LinkedList<>();
		for (Object object : exercises) {
			JSONObject jsonObject = (JSONObject) object;
			String exercise_bodyString = (String) jsonObject.get("qBody");
			String[] body_optionStrings = exercise_bodyString.split("[A-Z][.．]");
			int opnum = body_optionStrings.length;
			JSONArray options = JSONArray.fromObject(Arrays.copyOfRange(body_optionStrings, 1, opnum));
			String exercise_answer = (String) jsonObject.get("qAnswer");
			if (Character.isUpperCase(exercise_answer.charAt(0))) {
				StringBuilder answer = new StringBuilder();
				answer.append(exercise_answer.charAt(0));
				for (int i = 1; i < Math.min(opnum, exercise_answer.length()); i++) {
					if (exercise_answer.charAt(i) >= 'A' || exercise_answer.charAt(i) < 'A' + opnum) {
						answer.append("," + exercise_answer.charAt(i));
					}
					else break;
				}
				// 如果答案可以被解析 那就继续下去 否则就跳过
				int ID = (int) jsonObject.get("id");
				JSONObject exerObject = new JSONObject();
				exerObject.put("id", ID);
				exerObject.put("answer", answer.toString());
				exerObject.put("content", body_optionStrings[0]);
				exerObject.put("options", options);
				exerObject.put("entity", "[\""+ name + "\"]");
				exerArray.add(exerObject);
				List<Object> exercise = new LinkedList<>();
				exercise.add(ID);
				exercise.add(answer.toString());
				exercise.add(body_optionStrings[0]);
				exercise.add(options.toString());
				exerciseList.add(exercise);
			}
		}
		params.add(exerciseList);
		params.add(name);
		// 尝试异步操作
		exerciseRepoImp.updateExerciseIn(params);
		return exerArray;
	}

	@Async("asyncServiceExecutor")
	public void updateHistory(List<String> params) {
		exerciseRepoImp.updateHistoryIn(params);
	}
}
