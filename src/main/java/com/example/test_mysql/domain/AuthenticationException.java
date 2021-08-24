/**
 * weiyz19
 * AuthenticationException.java
 * 2021-08-23
 */
package com.example.test_mysql.domain;

/**
 * 授权异常类
 */
public class AuthenticationException extends RuntimeException {
	private static final long serialVersionUID = -4899846863450862536L;

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}