/**
 * weiyz19
 * ExerciseRepoImp.java
 * 2021-08-31
 */
package com.example.test_mysql.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
	
	// 日期形式：年月日 时分秒
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public JSONArray findByNameIn(List<String> params) {
		StringBuilder sqlString = new StringBuilder("SELECT id FROM " 
				+ "entity_to_exercise"
				+ " WHERE name = \'" 
				+ params.get(0) 
				+ "\'");
		int userID = Integer.parseInt(params.get(1));
		String[] idList;
		try {
			idList = entityManager.createNativeQuery(sqlString.toString()).getSingleResult().toString().split(",");
			// 说明是空的
			if (idList[0].isEmpty()) return new JSONArray();
		} catch (Exception e) {
			return null;
		}
		StringBuilder exerList = new StringBuilder("[");
		for (String id : idList) {
		    StringBuilder getExerciseString = new StringBuilder("SELECT * FROM" 
					+ " exercises"
					+ " WHERE id = " 
					+ id);
		    MyExercise sgExercise = null;
		    if (!(exerList.toString().equals("["))) exerList.append(",");
		    try {
		    	sgExercise = (MyExercise) entityManager.createNativeQuery(
		    			getExerciseString.toString(), MyExercise.class).getSingleResult();
		    } catch (Exception e) {}
		    StringBuilder getStarredBuilder = new StringBuilder("SELECT exercises FROM user_favor WHERE id="+ userID);
		    StringBuilder getNameString = new StringBuilder( "SELECT name FROM exercise_to_entity WHERE id = " + Integer.parseInt(id)); 
			try {
					String exercises = (String) entityManager.createNativeQuery(getStarredBuilder.toString()).getSingleResult();
					// 如果包括	
					if (JSONArray.fromObject(exercises).contains(id)) exerList.append(sgExercise.toJSON("1",
							(String) entityManager.createNativeQuery(getNameString.toString()).getSingleResult()));
					else exerList.append(sgExercise.toJSON("0", 
							(String) entityManager.createNativeQuery(getNameString.toString()).getSingleResult()));
			} catch (Exception e) {
					// 收藏里找不到，
				exerList.append(sgExercise.toJSON("0", (String) entityManager.createNativeQuery(getNameString.toString()).getSingleResult()));
			}
		}
		exerList.append("]");
		return JSONArray.fromObject(exerList.toString());
	}
	
	@Transactional
	@Modifying
	@Async("asyncServiceExecutor")
	public void updateExerciseIn(List<Object> params) {
		// ID  answer  content  options
		String name = (String) params.get(1);
		StringBuilder related = new StringBuilder();
		List<Object> exercisesList = (List<Object>) params.get(0);
		for (Object exercise : exercisesList) {
			List<Object> exList = (List<Object>) exercise;
			int exID = (int) exList.get(0);
			StringBuilder initialBuilder = new StringBuilder("SELECT * FROM exercises WHERE id = "
					+ exID);
			synchronized (entityManager) {
				if (entityManager.createNativeQuery(initialBuilder.toString()).getResultList().isEmpty()) {
					StringBuilder exString = new StringBuilder(
							"INSERT INTO exercises VALUES(" + exID + ",\'" + (String) exList.get(1) + paddingString
									+ (String) exList.get(3) + paddingString + ((String) exList.get(2)) + "\')");
					// 更新习题列表
					entityManager.createNativeQuery(exString.toString()).executeUpdate();
				}
			}
			if (related.toString().equals("")) related.append(exID);
			else related.append(",").append(exID);
			// 这里用来更新关系表
			StringBuilder sqlString = new StringBuilder( "SELECT name FROM exercise_to_entity"
					+ " WHERE id =" 
					+ exID);
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
							"UPDATE exercise_to_entity SET name = \'" + nameString + "\', WHERE id =" + exID);
				}
				entityManager.createNativeQuery(sqlString.toString()).executeUpdate();
			}
		}
		// 到这里 一个实体所关联的习题已经全部准备就绪了
		// 接下来更新实体-试题关系表
		StringBuilder sqlString = new StringBuilder( "INSERT INTO entity_to_exercise VALUES(\'"
				+ name 
				+ paddingString
				+ related
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
	public void updateHistoryIn(List<String> params) {
		// userID
		int userID = Integer.parseInt(params.get(0));
		// 当前的日期
		String dateString = sdf.format(new Date());
		StringBuilder sqlBuilder = new StringBuilder("SELECT exercise_count FROM user_history" + " WHERE id = " + userID);
		List<String> entList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
		// 同时只允许一个方法读写
		synchronized (entityManager) {
			if (entList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_history VALUES(" + userID + ",\'[]\', \'[]\', \'[{date:\'" + dateString + "\', count:1}]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			} else {
				JSONArray dateArray = JSONArray.fromObject(entList.get(0));
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
	}	
}

