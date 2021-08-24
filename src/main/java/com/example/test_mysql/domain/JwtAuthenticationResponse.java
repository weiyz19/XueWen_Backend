/**
 * weiyz19
 * JwtAuthenticationResponse.java
 * 2021-08-23
 */
package com.example.test_mysql.domain;
import java.io.Serializable;

/**
 * 响应令牌类
 */
 
public class JwtAuthenticationResponse<T> extends JsonResult<T> implements Serializable {

	private static final long serialVersionUID = -913246171166034308L;
	private final String token;   //要发送回客户端的令牌
 
	public JwtAuthenticationResponse(T data, String msg, String code, String token) {
		super(data, msg, code);
		this.token = token;
	}
 
	public String getToken() {
		return this.token;
	}
}