/**
 * weiyz19
 * ExerciseRepoImp.java
 * 2021-08-31
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

import net.sf.json.JSONArray;

@Component
public class ExerciseRepoImpl {

	@PersistenceContext
	private EntityManager entityManager;
	
	private final String paddingString = "\',\'";

	public JSONArray findByNameIn(List<String> params) {
		StringBuilder sqlString = new StringBuilder("SELECT id FROM " 
				+ "entity_to_exercise"
				+ " WHERE name = \'" 
				+ params.get(0) 
				+ "\'");
		Query dataQuery = entityManager.createNativeQuery(sqlString.toString());
		String[] idList;
		try {
			idList = dataQuery.getSingleResult().toString().split(",");
		} catch (Exception e) {
			return null;
		}
		StringBuilder exerList = new StringBuilder("[");
		Query exerciseQuery;
		for (String id : idList) {
		    StringBuilder getExerciseString = new StringBuilder("SELECT * FROM" 
					+ " exercises"
					+ " WHERE id = " 
					+ id);
		    exerciseQuery = entityManager.createNativeQuery(getExerciseString.toString(), MyExercise.class);
		    try {
		    	if (!(exerList.toString().equals("["))) exerList.append(",");
		    	exerList.append(((MyExercise) exerciseQuery.getSingleResult()).toJSON());
		    } catch (Exception e) {
		    	System.out.println(">????");
		    }
		}
		exerList.append("]");
		return JSONArray.fromObject(exerList.toString());
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = RuntimeException.class)
	@Modifying
	public void updateExerciseIn(List<Object> params) {
		System.out.println("status:" + TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());
		// ID  answer  content  options
		String name = (String) params.get(1);
		StringBuilder related = new StringBuilder();
		List<Object> exercisesList = (List<Object>) params.get(0);
		for (Object exercise : exercisesList) {
			List<Object> exList = (List<Object>) exercise;
			int exID = (int) exList.get(0);
			StringBuilder initialBuilder = new StringBuilder("SELECT * FROM exercises WHERE id = "
					+ exID);
			if (entityManager.createNativeQuery(initialBuilder.toString()).getResultList().isEmpty()) {
				StringBuilder exString = new StringBuilder( "INSERT INTO exercises VALUES(" 
						+ exID 
						+ ",\'" 
						+ (String) exList.get(1)
						+ paddingString 
						+ (String) exList.get(3)
						+ paddingString 
						+ ((String) exList.get(2)) 
						+ "\')");
				// 更新习题列表
				entityManager.createNativeQuery(exString.toString()).executeUpdate();
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
			if (nameList.isEmpty()) {
				sqlString = new StringBuilder( "INSERT INTO exercise_to_entity VALUES("
						+ exID
						+ ",\'"
						+ "[" + name + "]"
						+ "\')");
			}
			else {
				JSONArray entityJsonArray = JSONArray.fromObject(nameList.get(0));
				entityJsonArray.add(name);
				nameString = entityJsonArray.toString();
				sqlString = new StringBuilder( "UPDATE exercise_to_entity SET name = "
						+ "\'"
						+ nameString
						+ "\', WHERE id ="
						+ exID );
			}
			entityManager.createNativeQuery(sqlString.toString()).executeUpdate();
		}
		// 到这里 一个实体所关联的习题已经全部准备就绪了
		// 接下来更新实体-试题关系表
		StringBuilder sqlString = new StringBuilder( "INSERT INTO entity_to_exercise VALUES(\'"
				+ name 
				+ paddingString
				+ related
				+ "\')");
		System.out.println("final:" + TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());
		try {
			System.out.println("Finally update " + entityManager.createNativeQuery(sqlString.toString()).executeUpdate()+ " row(s)");
		} catch (RuntimeException e) {
			System.out.println("Update failed!");
		}
	}	
}

