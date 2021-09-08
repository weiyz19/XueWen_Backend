/**
 * weiyz19
 * MyExercise.java
 * 2021-08-30
 */
package com.example.test_mysql.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;

@Entity
@Table(name = "exercises")
public class MyExercise {
	@Id		/** key in table */
	private int id;
  
	private String answer;
  
	private String content;
  
	private String options;
	
	private String sbj;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAnswer() {
	    return answer;
	}
	public void setAnswer(String answer) {
	    this.answer = answer;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getOptions() {
		return options;
	}
	public void setOptions(String options) {
		this.options = options;
	}
	public String getSbj() {
		return sbj;
	}
	
	public void setSbj(String sbj) {
		this.sbj = sbj;
	}
	public String toJSON() {
		return new StringBuilder("{" +
				"id:" + id +
				", answer:\'" + StringEscapeUtils.unescapeJava(answer) +
	            "\',content:\'" + StringEscapeUtils.unescapeJava(content) +
	            "\',options:" + StringEscapeUtils.unescapeJava(options) +
	            "}").toString();
	}
}