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
     * 若没有数据返回，默认状态码为 0，提示信息为“操作成功！”
     */
    public JsonResult() {
        this.code = "0";
        this.msg = "操作成功！";
    }

    /**
     * 若没有数据返回，可以人为指定状态码和提示信息
     * @param code
     * @param msg
     */
    public JsonResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 有数据返回时，设置状态码并指定提示信息”
     * @param data
     */
    public JsonResult(T data, String msg, String code) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    /**
     * 有数据返回，状态码默认为200，人为指定提示信息
     * @param data
     * @param msg
     */
    public JsonResult(T data, String msg) {
        this.data = data;
        this.code = "200";
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