/**
 * weiyz19
 * ExerciseRepoImp.java
 * 2021-08-31
 */
package com.example.test_mysql.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.lang.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class ExerciseRepoImpl {

	@PersistenceContext
	private EntityManager entityManager;
	
	private static final String paddingString = "\',\'";

	private static final Logger logger = LoggerFactory.getLogger(ExerciseRepoImpl.class);
	
	// 日期形式：年月日
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	// 需要复习的五个时间节点
	private final TreeMap<Integer, Integer> timeNode = new TreeMap<>(Map.of(1, 0, 2, 1, 4, 2, 7, 3, 15, 4));
	private final long timerange = 604800000;
	private final long oneday = 86400000;
	
	
	public JSONArray findByNameIn(List<String> params) {
		StringBuilder sqlString = new StringBuilder("SELECT id FROM entity_to_exercise WHERE name = \'" 
				+ params.get(0) 
				+ "\'");
		int userID = Integer.parseInt(params.get(1));
		int course = Integer.parseInt(params.get(2));
		JSONArray idList = new JSONArray();
		try {
			idList = JSONArray.fromObject(entityManager.createNativeQuery(sqlString.toString()).getSingleResult());
			// 说明是空的	
			if (idList.isEmpty()) return idList;
		} catch (Exception e) {
			return null;
		}
		JSONArray exerList = new JSONArray();
		// 取出所有的习题
		for (int i = 0; i < idList.size(); ++i) {
			int id = idList.getInt(i);
		    StringBuilder getExerciseString = new StringBuilder("SELECT * FROM exercises WHERE id = " + id);
		    MyExercise sgExercise = null;
		    try {
		    	// 拿出其中一道
		    	sgExercise = (MyExercise) entityManager.createNativeQuery(
		    			getExerciseString.toString(), MyExercise.class).getSingleResult();
		    	JSONArray sbJsonArray = JSONArray.fromObject(sgExercise.getSbj());
		    	// 相关科目不全就更新
		    	if(!sbJsonArray.contains(course)) {
		    		sbJsonArray.add(course);
		    		getExerciseString = new StringBuilder("UPDATE exercises SET sbj=" + sbJsonArray.toString() + " WHERE id = " + id);
		    		entityManager.createNativeQuery(getExerciseString.toString()).executeUpdate();
		    	}
		    } catch (Exception e) {}
		    // 检查每个习题是否被收藏 并加上其关联实体
		    StringBuilder getStarredBuilder = new StringBuilder("SELECT exercises FROM user_favor WHERE id="+ userID);
		    StringBuilder getNameString = new StringBuilder( "SELECT name FROM exercise_to_entity WHERE id = " + id); 
		    JSONObject exerObject = JSONObject.fromObject(sgExercise.toJSON());
		    exerObject.put("entity", JSONArray.fromObject(entityManager.createNativeQuery(getNameString.toString()).getSingleResult().toString()));
		    exerObject.put("idx", -1);
			try {
					String exercises = entityManager.createNativeQuery(getStarredBuilder.toString()).getSingleResult().toString();
					String pattern = "^[0-9]" + Integer.toString(id)+ "^[0-9]";
					// 如果包括
					if(Pattern.matches(Integer.toString(id), exercises))
						exerObject.put("isStarred", "1");
					else exerObject.put("isStarred", "0");
			} catch (Exception e) {
				exerObject.put("isStarred", "0");
			}
			exerList.add(exerObject);
		}
		return exerList;
	}
	
	@Transactional
	@Modifying
	@Async("asyncServiceExecutor")
	public void updateExerciseIn(List<Object> params) {
		// ID  answer  content  options
		String name = (String) params.get(1);
		String course = (String) params.get(2);
		JSONArray related = new JSONArray();
		List<Object> exercisesList = (List<Object>) params.get(0);
		// 遍历所有的习题
		for (Object exercise : exercisesList) {
			List<Object> exList = (List<Object>) exercise;
			int exID = (int) exList.get(0);
			StringBuilder initialBuilder = new StringBuilder("SELECT * FROM exercises WHERE id="+ exID);
			synchronized (entityManager) {
				if (entityManager.createNativeQuery(initialBuilder.toString()).getResultList().isEmpty()) {
					StringBuilder exString = new StringBuilder(
							"INSERT INTO exercises VALUES(" + exID + ",\'" + (String) exList.get(1) + paddingString
									+ (String) exList.get(3) + paddingString + ((String) exList.get(2)) + "\',\'["+ course +"]\')");
					// 更新习题列表
					entityManager.createNativeQuery(exString.toString()).executeUpdate();
				}
			}
			related.add(exID);
			// 这里用来更新关系表
			StringBuilder sqlString = new StringBuilder( "SELECT name FROM exercise_to_entity WHERE id=" + exID);
			Query entityQuery = entityManager.createNativeQuery(sqlString.toString());
			List<Object> nameList = entityQuery.getResultList();
			String nameString;
			synchronized (entityManager) {
				if (nameList.isEmpty()) {
					sqlString = new StringBuilder(
							"INSERT INTO exercise_to_entity VALUES(" + exID + ",\'[\"" + name + "\"]\')");
				} else {
					JSONArray entityJsonArray = JSONArray.fromObject(nameList.get(0));
					entityJsonArray.add(name);
					nameString = entityJsonArray.toString();
					sqlString = new StringBuilder(
							"UPDATE exercise_to_entity SET name = \'" + nameString + "\' WHERE id =" + exID);
				}
				entityManager.createNativeQuery(sqlString.toString()).executeUpdate();
			}
		}
		// 到这里 一个实体所关联的习题已经全部准备就绪了
		// 接下来更新实体-试题关系表
		StringBuilder sqlString = new StringBuilder( "INSERT INTO entity_to_exercise VALUES(\'"
				+ name 
				+ paddingString
				+ related.toString()
				+ "\')");
		synchronized (entityManager) {
			try {
				logger.info("Finally update {} row(s)", entityManager.createNativeQuery(sqlString.toString()).executeUpdate());
			} catch (RuntimeException e) {
				logger.info("Update failed!");
			}
		}
	}
	
	@Transactional
	@Modifying
	public void updateHistoryIn(List<String> params) throws Exception {
		// userID id
		int userID = Integer.parseInt(params.get(0));
		int id = Integer.parseInt(params.get(1));
		int idx = Integer.parseInt(params.get(2));
		// 当前的日期
		String dateString = sdf.format(new Date());
		StringBuilder sqlBuilder = new StringBuilder("SELECT exercise_count FROM user_history WHERE id = " + userID);
		String entList = entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult().toString();
		
		sqlBuilder = new StringBuilder("SELECT exercises FROM user_history" + " WHERE id = " + userID);
		JSONArray exerIdArray = JSONArray.fromObject(entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult());
		StringBuilder favorBuilder = new StringBuilder("SELECT exercises FROM user_favor WHERE id = " + userID);
		JSONArray favorExercise = JSONArray.fromObject(entityManager.createNativeQuery(favorBuilder.toString()).getSingleResult());
		// 是需要复习的题目
		if (idx != -1) {
			for (int j = 0; j < favorExercise.size(); j++) {
				JSONObject exercise = favorExercise.getJSONObject(j);
				if (exercise.getInt("id") == id) {
					JSONArray checks = exercise.getJSONArray("check");
					checks.set(idx, 1);
					exercise.replace("check", checks);
					favorExercise.set(j, exercise);
					favorBuilder = new StringBuilder("UPDATE user_favor set exercises ="+ favorExercise.toString() + " WHERE id = " + userID);
					entityManager.createNativeQuery(favorBuilder.toString()).executeUpdate();
				}
			}
		}
		for (int i = 0; i < exerIdArray.size(); i++) {
			if (exerIdArray.getInt(i) == id) {
				exerIdArray.remove(i);
				break;
			}
		}
		exerIdArray.add(0, id);
		sqlBuilder = new StringBuilder("UPDATE user_history SET exercises =\'" + exerIdArray.toString() + "\' WHERE id = " + userID);
		entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		// 同时只允许一个方法读写
		synchronized (entityManager) {
			JSONArray dateArray = JSONArray.fromObject(entList);
			for (int i = 0; i < dateArray.size(); i++) {
				if (dateArray.getJSONObject(i).getString("date").equals(dateString)) {
					int ccount = (int) dateArray.getJSONObject(i).get("count");
					JSONObject newEntry = new JSONObject();
					newEntry.put("date", dateString);
					newEntry.put("count", ccount + 1);
					dateArray.set(i, newEntry);
					return;
				}
			}
			// 移动到最前端
			JSONObject newEntry = new JSONObject();
			newEntry.put("date", dateString);
			newEntry.put("count", 1);
			dateArray.add(0, newEntry);
			sqlBuilder = new StringBuilder("UPDATE user_history SET exercise_count =\'" + dateArray.toString() + "\' WHERE id = " + userID);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
	}	
	
	/**
	 * 根据艾宾浩斯记忆曲线取出收藏(错题)
	 * 专项复习核心函数
	 * @throws ParseException 
	 * */
	public JSONArray getEbbinghausIn(List<Integer> params) throws ParseException {
		int userID = params.get(0);
		int course = params.get(1);
		JSONArray response = new JSONArray();
		// 首先取出所有需要复习的题目
		StringBuilder reviewString = new StringBuilder("SELECT exercises FROM user_favor WHERE id="+ userID);
		JSONArray review = JSONArray.fromObject(entityManager.createNativeQuery(reviewString.toString()).getSingleResult());
		long time = sdf.parse(sdf.format(new Date())).getTime();
		for (int i = 0; i < review.size(); i++) {
			JSONObject exercise = review.getJSONObject(i);
			StringBuilder sbjBuilder = new StringBuilder("SELECT sbj FROM exercises WHERE id=" + exercise.getInt("id"));
			// 拿出本学科的收藏
			if (JSONArray.fromObject(entityManager.createNativeQuery(sbjBuilder.toString()).getSingleResult())
					.contains(course)) {
				// 算出到今天的时差
				int diff = (int) ((time - sdf.parse(exercise.getString("date")).getTime()) / oneday);
				int idx;
				try {
					idx = timeNode.floorEntry(diff).getValue();
				} catch (Exception e) { continue; }
				
				if (exercise.getJSONArray("check").getInt(idx) == 0) {
					StringBuilder favorBuilder = new StringBuilder(
							"SELECT * FROM exercises WHERE id=" + exercise.getInt("id"));
					JSONObject setExercise = JSONObject.fromObject(((MyExercise) entityManager
							.createNativeQuery(favorBuilder.toString(), MyExercise.class).getSingleResult()).toJSON());
					StringBuilder getNameString = new StringBuilder(
							"SELECT name FROM exercise_to_entity WHERE id=" + exercise.getInt("id"));
					setExercise.put("entity", JSONArray.fromObject(
							entityManager.createNativeQuery(getNameString.toString()).getSingleResult().toString()));
					setExercise.put("idx", idx);
					response.add(setExercise);
				}
			}
		}
		return response;
	}
	
	/**
	 * 按照优先级取出推荐习题
	 * 来源主要是收藏
	 * 优先从收藏中取
	 * @throws ParseException 
	 * */
	public JSONArray getRecommendIn(List<String> params) throws ParseException {
		String userID = params.get(0);
		String course = params.get(1);
		List<Integer> favorList = new LinkedList<>();
		// 首先取出对应科目的收藏实体
		StringBuilder favorString = new StringBuilder("SELECT exercises FROM user_favor WHERE id="+ userID + " AND entities LIKE \'%" + course + "%\'");
		JSONArray favor = new JSONArray();
		try {
			favor = JSONArray.fromObject(entityManager.createNativeQuery(favorString.toString()).getSingleResult());
		} catch (NoResultException e) {
			logger.info("no result");
		}
		List<Integer> idList = new LinkedList<>();
		// 所有关联习题都放进去
		for (int i = 0; i < favor.size(); i++) {
			StringBuilder relateBuilder = new StringBuilder("SELECT id FROM entity_to_exercise WHERE name=\"")
					.append(favor.getJSONObject(i).getString("name")).append("\"");
			idList.addAll(JSONArray.fromObject(entityManager.createNativeQuery(relateBuilder.toString()).getSingleResult()));
		}
		// 减少重复率 再从题库中随机取100道题
		StringBuilder otherString = new StringBuilder("SELECT id FROM exercises WHERE sbj LIKE \'%").append(course).append("%\'");
		List<Integer> otherList = entityManager.createNativeQuery(otherString.toString()).getResultList();
		Collections.shuffle(otherList);
		if (otherList.size()> 100) otherList = otherList.subList(0, 100);
		idList.addAll(otherList);
		Collections.shuffle(idList);
		// 去重
		HashSet<Integer> set = new HashSet<>(idList);
		idList.clear();
		idList.addAll(set);
		JSONArray responseArray = new JSONArray();
		for (int i = 0; i < 20; i++) {
			int exID = idList.get(i);
			StringBuilder exBuilder = new StringBuilder("SELECT * FROM exercises WHERE id=" + exID);
			StringBuilder getNameString = new StringBuilder("SELECT name FROM exercise_to_entity WHERE id=" + exID);
			MyExercise sgExercise = null;
			try {
				// 拿出其中一道
				sgExercise = (MyExercise) entityManager
						.createNativeQuery(exBuilder.toString(), MyExercise.class).getSingleResult();
				JSONObject exerObject = JSONObject.fromObject(sgExercise.toJSON());
				exerObject.put("entity", JSONArray.fromObject(entityManager.createNativeQuery(getNameString.toString()).getSingleResult().toString()));
				exerObject.put("idx", -1);
				responseArray.add(exerObject);
			} catch (Exception e) {}
		}
		return responseArray;
	}
	
}

