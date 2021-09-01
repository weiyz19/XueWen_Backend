/**
 * weiyz19
 * UserFavorRepoImpl.java
 * 2021-09-02
 */
package com.example.test_mysql.domain;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import antlr.ASdebug.ASDebugStream;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class UserFavorRepoImpl {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	@Modifying
	/** 查找用户的收藏信息 */
	public JSONObject findByIdIn(List<Integer> params) {
		StringBuilder sqlString = new StringBuilder("SELECT * FROM user_favor"
				+ " WHERE id = "
				+ params.get(0));
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString(), UserFavor.class);
		List<UserFavor> res = dataQuery.getResultList();
		JSONArray exerArray = new JSONArray();
		JSONArray entyArray = new JSONArray();
		if (res.isEmpty()) {
			StringBuilder insertBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ params.get(0)
					+ ",\'[]\', \'[]\')");
			entityManager.createNativeQuery(insertBuilder.toString()).executeUpdate();
		}
		else {
			exerArray = JSONArray.fromObject(res.get(0).getExercises());
			entyArray = JSONArray.fromObject(res.get(0).getEntities());
		}
		JSONObject resJsonObject = new JSONObject();
		resJsonObject.put("exercises", exerArray);
		resJsonObject.put("entities", entyArray);
		return resJsonObject;
	}
	
	@Transactional
	@Modifying
	/** 增加用户的收藏信息 */
	public void updateFavorIn(List<Object> params) {
		// userid	0:实体  实体名称 
		// userid	1:习题  习题id
		int id = (int) params.get(0);
		int type = (int) params.get(1);
		// 实体
		if (type == 0) {
			String nameString = (String) params.get(2);
			StringBuilder sqlBuilder = new StringBuilder("SELECT entities FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> resList = entityManager.createNativeQuery(sqlBuilder.toString(), String.class).getResultList();
			if (resList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ id
					+ ",\'["
					+ nameString
					+ "]\', \'[]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
			else {
				JSONArray entitiesArray = JSONArray.fromObject(resList.get(0));
				entitiesArray.add(nameString);
				sqlBuilder = new StringBuilder("UPDATE user_favor SET entities"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\', WHERE id=" 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
		else {
			int exID = (int) params.get(2);
			StringBuilder sqlBuilder = new StringBuilder("SELECT exercises FROM user_favor"
				+ " WHERE id = "
				+ id);
			List<String> resList = entityManager.createNativeQuery(sqlBuilder.toString()).getResultList();
			if (resList.isEmpty()) {
				sqlBuilder = new StringBuilder("INSERT INTO user_favor VALUES("
					+ id
					+ ",\'[]\', \'["
					+ exID
					+ "]\')");
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
			else {
				JSONArray entitiesArray = JSONArray.fromObject(resList.get(0));
				entitiesArray.add(exID);
				sqlBuilder = new StringBuilder("UPDATE user_favor SET exercises"
						+ "=\'"
						+ entitiesArray.toString()
						+ "\', WHERE id=" 
						+ id);
				entityManager.createNativeQuery(sqlBuilder.toString()).executeUpdate();
			}
		}
	}
}

