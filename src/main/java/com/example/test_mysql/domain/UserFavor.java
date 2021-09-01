/**
 * weiyz19
 * UserFavor.java
 * 2021-09-02
 */
package com.example.test_mysql.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "user_favor")
public class UserFavor {
	@Id		/** key in table */
	private int id;
  
	private String exercises;
  
	private String entities;
	
	public int getId() {
	    return id;
	}
	public void setId(int id) {
	    this.id = id;
	}
	public String getExercises() {
		return exercises;
	}
	public void setExercises(String exercises) {
		this.exercises = exercises;
	}
	public String getEntities() {
		return entities;
	}
	public void setEntities(String entities) {
		this.entities = entities;
	}
}
