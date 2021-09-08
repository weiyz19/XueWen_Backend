/**
 * weiyz19
 * UserFavorRepoImpl.java
 * 2021-09-02
 */
package com.example.test_mysql.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class UserFavorRepoImpl {

	@PersistenceContext
	private EntityManager entityManager;

	// 日期形式：年月日
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private final long timerange = 604800000;
	private final long oneday = 86400000;
	
	@Async("asyncServiceExecutor")
	public void createEntry(List<Integer> params) {
		StringBuilder favorString = new StringBuilder("INSERT INTO user_favor VALUES("
				+ params.get(0)
				+ ",\'[]\', \'[]\')");
		entityManager.createNativeQuery(favorString.toString()).executeUpdate();
		StringBuilder historyString = new StringBuilder("INSERT INTO user_history VALUES("
				+ params.get(0)
				+ ",\'[]\' ,\'[0,0,0,0,0,0,0,0,0]\' ,\'[]\', \'[]\')");
		entityManager.createNativeQuery(historyString.toString()).executeUpdate();
	}
	
	@Transactional
	@Modifying
	/** 查找用户的收藏实体 */
	public JSONArray findEntityByIdIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM user_favor WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), UserFavor.class);
		UserFavor res = (UserFavor) dataQuery.getSingleResult();
		JSONArray entyArray = new JSONArray();
		entyArray = JSONArray.fromObject(res.getEntities());
		return entyArray;
	}
	
	@Transactional
	@Modifying
	/** 查找用户的收藏习题 */
	public JSONArray findExerciseByIdIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM user_favor WHERE id = "+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), UserFavor.class);
		UserFavor res = (UserFavor) dataQuery.getSingleResult();
		JSONArray exerArray = new JSONArray();
		JSONArray idArray = JSONArray.fromObject(res.getExercises());
		for (int i = 0; i < idArray.size(); ++i) {
			int exID = idArray.getJSONObject(i).getInt("id");
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
				exerArray.add(exerObject);
			} catch (Exception e) {}
		}
		return exerArray;
	}
	
	@Transactional
	@Modifying
	/** 增加用户的收藏信息 */
	public void updateFavorIn(List<Object> params) {
		// 	0:实体  实体名称 userid   sbj
		//  1:习题  习题id   userid
		int id = (int) params.get(2);
		int type = (int) params.get(0);
		// 实体
		if (type == 0) {
			String newEntry = new StringBuilder("{ name: \'" 
				+ (String) params.get(1) 
				+ "\', sbj: \'"				
				+ (String) params.get(3)
				+ "\'}").toString();
			StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_favor WHERE id=" + id);
			String entList = entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult().toString();
			JSONArray entitiesArray = JSONArray.fromObject(entList);
			JSONObject sg = JSONObject.fromObject(newEntry);
			if(entitiesArray.contains(sg)) return;
			entitiesArray.add(0, sg);
			sqlBuilder = new StringBuilder("UPDATE user_favor SET entities=\'"
					+ entitiesArray.toString()
					+ "\' WHERE id = " 
					+ id);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
		// 习题
		else {
			int exID = Integer.parseInt((String) params.get(1));
			StringBuilder sqlBuilder = new StringBuilder("SELECT exercises FROM user_favor WHERE id = "
				+ id);
			String resList = entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult().toString();
			JSONArray entitiesArray = JSONArray.fromObject(resList);
			for (int i = 0; i < entitiesArray.size(); ++i) {
				if ((int) entitiesArray.getJSONObject(i).get("id") == exID) return;
			}
			String newEntry = new StringBuilder("{ id:" 
					+ exID 
					+ ", date: \'"				
					+ sdf.format(new Date())
					// used for EbbingHause check
					+ "\', check:[0, 0, 0, 0, 0]}").toString();
			entitiesArray.add(0, newEntry);
			sqlBuilder = new StringBuilder("UPDATE user_favor SET exercises"
					+ "=\'"
					+ entitiesArray.toString()
					+ "\' WHERE id=" 
					+ id);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
	}
	
	@Transactional
	@Modifying
	/** 删除用户的收藏信息 */
	public void cancelFavorIn(List<Object> params) {
		// 	0:实体  实体名称  userid   sbj
		//  1:习题  习题id   userid
		int id = (int) params.get(2);
		int type = (int) params.get(0);
		// 实体
		if (type == 0) {
			StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_favor WHERE id = "+id);
			JSONArray resList = JSONArray.fromObject(entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult());
			String newEntry = new StringBuilder(
					"{ name: \'" + (String) params.get(1) + "\', sbj: \'" + (String) params.get(3) + "\'}").toString();
			JSONObject sg = JSONObject.fromObject(newEntry);
			if (!resList.contains(sg)) return;
			resList.remove(sg);
			sqlBuilder = new StringBuilder("UPDATE user_favor SET entities" + "=\'" 
					+ resList.toString() + "\' WHERE id = " + id);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
		else {
			int exID = Integer.parseInt((String) params.get(1));
			StringBuilder sqlBuilder = new StringBuilder("SELECT exercises FROM user_favor WHERE id = "+id);
			JSONArray resList = JSONArray.fromObject(entityManager.createNativeQuery(sqlBuilder.toString()).getSingleResult());
			String pattern = "[^0-9]" + exID + "[^0-9]";
		    // 创建 Pattern 对象
		    Pattern r = Pattern.compile(pattern);
			for(int i = 0; i < resList.size(); ++i) {
				Matcher m = r.matcher(resList.getString(i));
				if (m.find()) {
					resList.remove(i); 
					break;
				}
			}
			sqlBuilder = new StringBuilder("UPDATE user_favor SET exercises=\'"
						+ resList.toString()
						+ "\' WHERE id=" 
						+ id);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
	}
	
	@Transactional
	@Modifying
	/** 查找用户的历史总览信息 */
	public JSONObject findHistoryByIdIn(List<Integer> params) throws ParseException {
		StringBuilder sqlString = new StringBuilder("SELECT exercise_count, entity_count FROM user_history WHERE id = "
				+ params.get(0));
		List<Object> res = entityManager.createNativeQuery(sqlString.toString()).getResultList();
		JSONObject userInfo = new JSONObject();
		long time = sdf.parse(sdf.format(new Date())).getTime();
		JSONArray logJsonArray = JSONArray.fromObject(res.get(0));
		userInfo.put("entity", logJsonArray.getJSONArray(1));
		JSONArray exList = logJsonArray.getJSONArray(0);
		JSONArray dateList = JSONArray.fromObject(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
		int minNum = 7;
		if (exList.size() < 7)
			minNum = exList.size();
		for (int i = 0; (i < exList.size() && i < 7); i++) {
			JSONObject ex = JSONObject.fromObject(exList.get(i));
			long diff = time - sdf.parse(ex.getString("date")).getTime();
			if (diff < timerange)
				dateList.set((int) (diff / oneday), ex.get("count"));
		}
		userInfo.put("exercise", dateList);
		return userInfo;
	}
	
}

