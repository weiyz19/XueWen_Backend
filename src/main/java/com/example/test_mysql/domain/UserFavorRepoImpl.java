/**
 * weiyz19
 * UserFavorRepoImpl.java
 * 2021-09-02
 */
package com.example.test_mysql.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class UserFavorRepoImpl {

	@PersistenceContext
	private EntityManager entityManager;

	// 日期形式：年月日 时分秒
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Transactional
	@Modifying
	/** 查找用户的收藏信息 */
	public JSONArray findEntityByIdIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM user_favor WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), UserFavor.class);
		List<UserFavor> res = dataQuery.getResultList();
		JSONArray entyArray = new JSONArray();
		if (res.isEmpty()) {
			StringBuilder insertBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ params.get(0)
					+ ",\'[]\', \'[]\')");
			entityManager.createNativeQuery(insertBuilder.toString()).executeUpdate();
		}
		else entyArray = JSONArray.fromObject(res.get(0).getEntities());
		return entyArray;
	}
	
	@Transactional
	@Modifying
	/** 查找用户的收藏信息 */
	public JSONArray findExerciseByIdIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM user_favor WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), UserFavor.class);
		List<UserFavor> res = dataQuery.getResultList();
		JSONArray exerArray = new JSONArray();
		if (res.isEmpty()) {
			StringBuilder insertBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ params.get(0)
					+ ",\'[]\', \'[]\')");
			entityManager.createNativeQuery(insertBuilder.toString()).executeUpdate();
		}
		else { 
			JSONArray idArray = JSONArray.fromObject(res.get(0).getExercises());
			for (int i = 0; i < idArray.size(); ++i) {
				// 遍历所有习题
				sqlString = new StringBuilder("SELECT name FROM exercise_to_entity WHERE id = " + idArray.get(i));
				JSONObject exerObject = new JSONObject();
				exerObject.put("id", idArray.get(i));
				exerObject.put("entities", JSONArray.fromObject(entityManager.createNativeQuery(sqlString.toString()).getSingleResult()));
				exerArray.add(exerObject);
			}
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
			StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> entList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
			if (entList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ id
					+ ",\'["
					+ newEntry
					+ "]\', \'[]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
			else {
				JSONArray entitiesArray = JSONArray.fromObject(entList.get(0));
				JSONObject sg = JSONObject.fromObject(newEntry);
				if(entitiesArray.contains(sg)) return;
				entitiesArray.add(0, sg);
				sqlBuilder = new StringBuilder("UPDATE user_favor SET entities"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\' WHERE id = " 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
		// 习题
		else {
			int exID = Integer.parseInt((String) params.get(1));
			StringBuilder sqlBuilder = new StringBuilder("SELECT exercises FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> resList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
			String newEntry = new StringBuilder("{ id:" 
					+ exID 
					+ ", date: \'"				
					+ sdf.format(new Date())
					+ "\'}").toString();
			if (resList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ id
					+ ",\'[]\', \'["
					+ newEntry
					+ "]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
			else {
				JSONArray entitiesArray = JSONArray.fromObject(resList.get(0));
				for (int i = 0; i < entitiesArray.size(); ++i) {
					if ((int) entitiesArray.getJSONObject(i).get("id") == exID) return;
				}
				entitiesArray.add(0, exID);
				sqlBuilder = new StringBuilder("UPDATE user_favor SET exercises"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\' WHERE id=" 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
	}
	
	@Transactional
	@Modifying
	/** 删除用户的收藏信息 */
	public void cancelFavorIn(List<Object> params) {
		// 	0:实体  实体名称 userid
		//  1:习题  习题id   userid
		int id = (int) params.get(2);
		int type = (int) params.get(0);
		// 实体
		if (type == 0) {
			StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> resList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
			if (resList.isEmpty()) return;
			else {
				String newEntry = new StringBuilder("{ name: \'" 
						+ (String) params.get(1) 
						+ "\', sbj: \'"				
						+ (String) params.get(3)
						+ "\'}").toString();
				JSONObject sg = JSONObject.fromObject(newEntry);
				JSONArray entitiesArray = JSONArray.fromObject(resList.get(0));
				if(!entitiesArray.contains(sg)) return;
				entitiesArray.remove(sg);
				sqlBuilder = new StringBuilder("UPDATE user_favor SET entities"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\' WHERE id = " 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
		else {
			int exID = Integer.parseInt((String) params.get(1));
			StringBuilder sqlBuilder = new StringBuilder("SELECT exercises FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> resList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
			if (resList.isEmpty()) return;
			else {
				JSONArray entitiesArray = JSONArray.fromObject(resList.get(0));
				// TODO: 测试这玩意能用不
				if(!entitiesArray.contains(exID)) return;
				for(int i = 0; i < entitiesArray.size(); ++i) {
					if (entitiesArray.get(i).equals(exID)) {
						entitiesArray.remove(i); break;
					}
				}
				sqlBuilder = new StringBuilder("UPDATE user_favor SET exercises"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\' WHERE id=" 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
	}
	
}

