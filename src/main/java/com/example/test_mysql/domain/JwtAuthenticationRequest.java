/**
 * weiyz19
 * JwtAuthenticationRequest.java
 * 2021-08-23
 */
package com.example.test_mysql.domain;

import java.io.Serializable;

/**
 * 在请求中获得用户名和密码然后封装起来
 */
 
public class JwtAuthenticationRequest implements Serializable {
 
	private static final long serialVersionUID = 6959349699397197830L;
	private String username;
	private String password;
 
 
	public JwtAuthenticationRequest() {
		super();
	}
 
	public JwtAuthenticationRequest(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	}

	public String getUsername() {
	    return username;
	  }
  
  	public void setUsername(String username) {
	    this.username = username;
	  }

  	public String getPassword() {
  		return password;
  	}
  	
	public void setPassword(String password) {
		this.password = password;
	}
}