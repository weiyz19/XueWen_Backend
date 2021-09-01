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
import org.springframework.stereotype.Component;

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
			return res.toJSON();
		} catch (Exception e) {
			return null;
		}
	}	
}

