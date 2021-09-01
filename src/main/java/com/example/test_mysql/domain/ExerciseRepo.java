/**
 * weiyz19
 * ExerciseRepo.java
 * 2021-08-31
 */
package com.example.test_mysql.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import net.sf.json.JSONArray;
@Transactional
public interface ExerciseRepo extends JpaRepository<MyExercise, Integer>, JpaSpecificationExecutor<MyExercise>{
	public JSONArray findByNameIn(List<String> params);
	
	@Modifying
	public void updateExerciseIn(List<Object> params);
}
