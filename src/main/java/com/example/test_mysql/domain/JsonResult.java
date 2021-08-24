/**
 * weiyz19
 * JsonResult.java
 * 2021-08-20
 */
package com.example.test_mysql.domain;

/** 统一的返回体封装格式 */
public class JsonResult<T> {

    private T data;
    private String code;
    private String msg;

    /**
     * 有数据返回时，设置状态码并指定提示信息”
     * @param data
     */
    public JsonResult(T data, String msg, String code) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }
    
    public String getCode() {
    	return code;
    }

    public String getMsg() {
    	return msg;
    }

    public T getData() {
    	return data;
    }
}