/**
 * weiyz19
 * UserFavorRepo.java
 * 2021-09-02
 */
package com.example.test_mysql.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.sf.json.JSONObject;

public interface UserFavorRepo extends JpaRepository<MyExercise, Integer>, JpaSpecificationExecutor<MyExercise>{
	public JSONObject findByIdIn(List<Integer> params);
	public void updateFavorIn(List<Object> params);
}
