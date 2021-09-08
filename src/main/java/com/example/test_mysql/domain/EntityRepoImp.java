/**
 * weiyz19
 * EntityRepoImp.java
 * 2021-08-28
 */
package com.example.test_mysql.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	final String[] usertable = {"user_favor", "user_history"};
	
	
	@SuppressWarnings("unchecked")
	public JSONArray findAllByNameIn(List<String> params) {
		JSONArray entityList = new JSONArray();
		for (int i = 0; i < 9; i++) {
			JSONObject entity = new JSONObject();
			StringBuilder sqlString = new StringBuilder("SELECT name, content FROM " 
					+ courses[i] 
					+ " WHERE name LIKE \'%" 
					+ params.get(0) 
					+ "%\'");
			Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
			try {
				for (Object myEntity : dataQuery.getResultList()) {
					Object[] row = (Object[]) myEntity;
					entity.put("name", (String) row[0]);
					entity.put("content", (String) row[1]);
					if (entity.getString("content").equals("[]"))
						entity.replace("content", "[\"请点击以查看详情\"]");
					entity.put("sbj", Integer.toString(i));
					entityList.add(entity);
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
			JSONObject entity = new JSONObject();
			resList = dataQuery.getResultList();
			if (resList.isEmpty()) entityList = null;
			else {
				for (Object myEntity : resList) {
					Object[] row = (Object[]) myEntity;
					Map<String, String> entityMap = new HashedMap();
					entity.put("name", (String) row[0]);
					entity.put("content", (String) row[1]);
					if (entity.getString("content").equals("[]"))
						entity.replace("content", "[\"请点击以查看详情\"]");
					entity.put("sbj", Integer.toString(course));
					entityList.add(entity);
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
	public JSONObject findDetailByNameIn(List<Object> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM " 
				+ courses[(int)params.get(0)] 
				+ " WHERE name = \'" 
				+ params.get(1) 
				+ "\'");
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), MyEntity.class);
		JSONObject entity;
		try {
			entity = JSONObject.fromObject(((MyEntity) dataQuery.getSingleResult()).toJSON());
		} catch (Exception e) {
			return null;
		}
		StringBuilder getStarredBuilder = new StringBuilder("SELECT entities FROM user_favor WHERE id="+ params.get(2));
		try {
				JSONObject entry = new JSONObject();
				entry.put("sbj", params.get(0));
				entry.put("name", params.get(1));
				if (JSONArray.fromObject(entityManager.createNativeQuery(
					getStarredBuilder.toString()).getSingleResult()).contains(entry)) 
					entity.put("isStarred", "1");
				else entity.put("isStarred", "0");
		} catch (Exception e) { // 收藏里找不到，
			entity.put("isStarred", "0");
		}
		return entity;
	}
	
	@Transactional
	@Modifying
	/** 查找用户的历史记录信息 */
	public JSONArray findHistoryIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT entities FROM user_history"
				+ " WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
		String res = dataQuery.getSingleResult().toString();
		return JSONArray.fromObject(res);
	}
	
	@Transactional
	@Modifying
	/** 增加用户的历史记录 */
	public void updateHistoryIn(List<Object> params) {
		//  String 课程  String 实体名  int userid
		String course = (String) params.get(0);
		int id = (int) params.get(2);
		// 实体
		String newEntry = new StringBuilder(
				"{ name: \"" + (String) params.get(1) + "\", sbj: \"" + course + "\"}").toString();
		StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_history WHERE id = " + id);
		List<String> entList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
		// 同时只允许一个方法读写
		synchronized (entityManager) {
			if (entList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_history VALUES(" + id + ",\'[" + newEntry + "]\', \'[0,0,0,0,0,0,0,0,0]\', \'[]\', \'[]\')");
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
			String getString = new StringBuilder("SELECT entity_count FROM user_history WHERE id = ").append(id).toString();
			JSONArray countArray = JSONArray.fromObject(entityManager.createNativeQuery(getString).getSingleResult());
			int courseidx = Integer.parseInt(course);
			int oldnum = (int) countArray.remove(courseidx);
			countArray.add(courseidx, oldnum + 1);
			sqlBuilder = new StringBuilder("UPDATE user_history SET entity_count =\'" + countArray.toString() + "\' WHERE id = " + id);
			entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
		}
	}
	
	/**
	 * 按照优先级取出推荐实体的ID
	 * 来源可以是历史记录或者收藏
	 * 优先从收藏中取
	 * */
	private List<String> getRelatedIn(List<String> params) {
		int userID = Integer.parseInt(params.get(0));
		String course = params.get(1);
		int tablename = Integer.parseInt(params.get(2));
		StringBuilder favorString = new StringBuilder("SELECT * FROM " + usertable[tablename] + " WHERE id = "
				+ userID);
		List<UserFavor> favor = entityManager.createNativeQuery(favorString.toString(), UserFavor.class).getResultList();
		List<String> entyArray = new LinkedList<>();
		// 优先取出收藏
		if (!favor.isEmpty()) {
			UserFavor userFavor = favor.get(0);
			JSONArray en = JSONArray.fromObject(userFavor.getEntities());
			JSONArray ex = JSONArray.fromObject(userFavor.getExercises());
			// 拿到它的所有关联实体
			for (int i = 0; i < en.size(); ++i) {
				// 拿到它的关联知识点
				if (((String)en.getJSONObject(i).get("sbj")).equals(course)) {
					favorString = new StringBuilder("SELECT relations FROM " + courses[Integer.parseInt(course)]
						+ " WHERE name = \'"
						+ en.getJSONObject(i).get("name") 
						+ "\'");
					JSONObject related = JSONObject.fromObject(entityManager.createNativeQuery(favorString.toString()).getSingleResult());
					for (Object object : related.values()) {
						JSONArray namesArray = (JSONArray) object;
						for(int j = 0; j < namesArray.size(); ++j)
							entyArray.add(namesArray.getString(j));
					}
				}
			}
			for (int k = 0; k < ex.size(); ++k) {
				// 拿到它的习题的关联知识点
				int exID = (int) ex.getJSONObject(k).get("id");
				favorString = new StringBuilder("SELECT name FROM exercise_to_entity WHERE id=" + exID);
				JSONArray relatedEnt = JSONArray.fromObject(entityManager.createNativeQuery(favorString.toString()).getSingleResult());
				// 检查每个关联的知识点
				for (int j = 0; j < relatedEnt.size(); j++) {
					String name = relatedEnt.getString(j);
					StringBuilder checkString = new StringBuilder("SELECT * FROM " + courses[Integer.parseInt(course)] + " WHERE name=\'" + name + "\'");
					if(!entityManager.createNativeQuery(checkString.toString()).getResultList().isEmpty()) {
						entyArray.add(name);
					}
				}
			}
		}
		return entyArray;
	}
	
	/** 为用户生成个性化实体推荐 */
	public JSONArray getUniqueIn(List<String> params) {
		params.add("0");
		List<String> entyArray = getRelatedIn(params);
		if (entyArray.size() < 200) {
			params.remove(2);
			params.add("1");
			entyArray.addAll(getRelatedIn(params));
		}
		String course = params.get(1);
		StringBuilder randomString = new StringBuilder("SELECT name FROM " + courses[Integer.parseInt(course)]
				+ " WHERE attributes != \'{}\' OR content != \'[]\'");
		List<String> nameList = entityManager.createNativeQuery(randomString.toString()).getResultList();
		Collections.shuffle(nameList);
		entyArray.addAll(nameList.subList(0, 200));
		// 去重
		HashSet<String> set = new HashSet<>(entyArray);
		entyArray.clear();
		entyArray.addAll(set);
		JSONArray entyJsonArray = new JSONArray();
		Collections.shuffle(entyArray);
		for (int i = 0; i < 50; i++) {
			String entityName = entyArray.get(i);
			StringBuilder contentString = new StringBuilder("SELECT content FROM " + courses[Integer.parseInt(course)]
					+ " WHERE name=\'" + entityName+ "\'");
			JSONObject entryJsonObject = new JSONObject();
			entryJsonObject.put("name", entityName);
			entryJsonObject.put("sbj", course);
			entryJsonObject.put("content", JSONArray.fromObject(entityManager.createNativeQuery(contentString.toString()).getSingleResult()));
			if (entryJsonObject.getString("content").equals("[]")) 
				entryJsonObject.replace("content", "[\"请点击以查看详情\"]");
			entyJsonArray.add(entryJsonObject);
		}
		return entyJsonArray;
	}
}

