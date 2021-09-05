/**
 * weiyz19
 * EntityRepoImp.java
 * 2021-08-28
 */
package com.example.test_mysql.domain;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class EntityRepoImp{

	@PersistenceContext
	private EntityManager entityManager;
	
	final String[] courses = {"chinese_detail_list", "math_detail_list", "english_detail_list", "physics_detail_list",
			"chemistry_detail_list", "biology_detail_list", "history_detail_list", "geo_detail_list", "politics_detail_list"};
	
	
	@SuppressWarnings("unchecked")
	public JSONArray findAllByNameIn(List<String> params) {
		Map<String, String> entityMap = new HashedMap();
		JSONArray entityList = new JSONArray();
		for (int i = 0; i < 9; i++) {
			StringBuilder sqlString = new StringBuilder("SELECT name, content FROM " 
					+ courses[i] 
					+ " WHERE name LIKE \'%" 
					+ params.get(0) 
					+ "%\'");
			Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
			try {
				for (Object myEntity : dataQuery.getResultList()) {
					Object[] row = (Object[]) myEntity;
					entityMap.put("name", (String) row[0]);
					entityMap.put("content", (String) row[1]);
					entityMap.put("sbj", Integer.toString(i));
					entityList.add(JSONObject.fromObject(entityMap));
				}
			} catch (Exception e) {}
		}
		if (entityList.isEmpty()) return null;
		return entityList;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray findByNameIn(List<Object> params) {
		int course = (int) params.get(0);
		StringBuilder sqlString = new StringBuilder("SELECT name, content FROM " 
				+ courses[course] 
				+ " WHERE name LIKE \'%" 
				+ params.get(1) 
				+ "%\'");
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
		List<Object> resList; 
		JSONArray entityList = new JSONArray();
		try {
			resList = dataQuery.getResultList();
			if (resList.isEmpty()) entityList = null;
			else {
				for (Object myEntity : resList) {
					Object[] row = (Object[]) myEntity;
					Map<String, String> entityMap = new HashedMap();
					entityMap.put("name", (String) row[0]);
					entityMap.put("content", (String) row[1]);
					entityMap.put("sbj", Integer.toString(course));
					entityList.add(JSONObject.fromObject(entityMap));
				}
			}
		} catch (Exception e) {
			entityList = null;
		}
		return entityList;
	}
	/**
	 * 得到一个实体的详情以及收藏情况
	 * params 学科标号/实体名称
	 * return 查找到的习题详情信息
	 * */
	public String findDetailByNameIn(List<Object> params) {
		StringBuilder sqlString = new StringBuilder( "SELECT * FROM " 
				+ courses[(int)params.get(0)] 
				+ " WHERE name = \'" 
				+ params.get(1) 
				+ "\'");
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), MyEntity.class);
		MyEntity res; 
		try {
			res = (MyEntity) dataQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		}
		StringBuilder getStarredBuilder = new StringBuilder("SELECT entities FROM user_favor WHERE id="+ params.get(2));
		try {
				JSONObject entry = new JSONObject();
				entry.put("sbj", params.get(0));
				entry.put("name", params.get(1));
				if (JSONArray.fromObject(entityManager.createNativeQuery(getStarredBuilder.toString()).getSingleResult())
						.contains(entry)) 
					return res.toJSON("1");
				else return res.toJSON("0");
		} catch (Exception e) {
				// 收藏里找不到，
			return res.toJSON("0");
		}
	}
	
	@Transactional
	@Modifying
	/** 查找用户的历史记录信息 */
	public JSONArray findHistoryIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT entities FROM user_history"
				+ " WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
		List<String> res = dataQuery.getResultList();
		JSONArray entyArray = new JSONArray();
		if (res.isEmpty()) {
			StringBuilder insertBuilder = new StringBuilder("INSERT INTO user_history VALUES("
					+ params.get(0)
					+ ",\'[]\', \'[]\')");
			entityManager.createNativeQuery(insertBuilder.toString()).executeUpdate();
		}
		else entyArray = JSONArray.fromObject(res.get(0));
		return entyArray;
	}
	
	@Transactional
	@Modifying
	/** 增加用户的历史记录 */
	public void updateHistoryIn(List<Object> params) {
		//  String 课程  String 实体名  int userid
		int id = (int) params.get(2);
		// 实体
		String newEntry = new StringBuilder(
				"{ name: \"" + (String) params.get(1) + "\", sbj: \"" + (String) params.get(0) + "\"}").toString();
		StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_history" + " WHERE id = " + id);
		List<String> entList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
		// 同时只允许一个方法读写
		synchronized (entityManager) {
			if (entList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_history VALUES(" + id + ",\'[" + newEntry + "]\', \'[]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			} else {
				JSONArray entitiesArray = JSONArray.fromObject(entList.get(0));
				JSONObject sg = JSONObject.fromObject(newEntry);
				if (entitiesArray.contains(sg)) entitiesArray.remove(sg);
				// 移动到最前端
				entitiesArray.add(0, sg);
				sqlBuilder = new StringBuilder("UPDATE user_history SET entities =\'" + entitiesArray.toString() + "\' WHERE id = " + id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
	}
}

