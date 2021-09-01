/**
 * weiyz19
 * EntityRepo.java
 * 2021-08-28
 */
package com.example.test_mysql.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import net.sf.json.JSONArray;

public interface EntityRepo extends JpaRepository<MyEntity, String>, JpaSpecificationExecutor<MyEntity>{
	public JSONArray findByNameIn(List<Object> params);
	public JSONArray findAllByNameIn(List<String> params);
	public String findDetailByNameIn(List<Object> params);
}

