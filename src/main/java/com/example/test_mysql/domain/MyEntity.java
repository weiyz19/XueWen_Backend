/**
 * weiyz19
 * MyEntity.java
 * 2021-08-28
 */
package com.example.test_mysql.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringEscapeUtils;

@Entity
public class MyEntity {
	@Id		/** key in table */
	private String name;
  
	private String uri;
  
	private String relations;
  
	private String attributes;
  
	private String content;
  
	private String type;
  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
	    return uri;
	}
	public void setUri(String uri) {
	    this.uri = uri;
	}
	public String getRelations() {
		return relations;
	}
	public void setRelations(String relations) {
		this.relations = relations;
	}
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String toJSON() {
		return new StringBuilder("{" +
			"name:\""+ name + "\"" +
            ",uri:" + StringEscapeUtils.unescapeJava(uri) +
            ",relations:" + StringEscapeUtils.unescapeJava(relations) +
            ",attributes:" + StringEscapeUtils.unescapeJava(attributes) +
            ",content:" + StringEscapeUtils.unescapeJava(content) +
            ",type:" + StringEscapeUtils.unescapeJava(type) +
            "}").toString();
	}
}