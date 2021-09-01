/**
 * weiyz19
 * HttpUtil.java
 * 2021-08-30
 */
package com.example.test_mysql.config;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpUtil {
    /**
     * 向目的URL发送post请求
     * @param url      目的url
     * @param params   发送的参数
     * @return  JsonData
     */
	private static final String ID = "f0c7ba8c-e740-4342-8111-0a4f7c0666e9";
	
    public static String sendPostRequest(String url, Map<String, String> params){
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        params.put("id", ID);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用String 类格式化（可设置为对应返回值格式的类）
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }

    /**
     * 向目的URL发送get请求
     * @param url       目的url
     * @param params    发送的参数
     * @param headers   发送的http头，可在外部设置好参数后传入
     * @return  String
     */
    public static String sendGetRequest(String url, Map<String, String> params){
        RestTemplate client = new RestTemplate();
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();
        // 以表单的方式提交
        StringBuilder newurl = new StringBuilder(url);
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
			if (isFirst) {
				newurl.append("?").append(entry.getKey()).append("=").append(entry.getValue());
				isFirst = false;
			}
			else newurl.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}
        newurl.append("&").append("id").append("=").append(ID);
        headers.setContentType(MediaType.APPLICATION_JSON);
        //将请求头部和参数合成一个请求
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用String 类格式化
        ResponseEntity<String> response = client.exchange(newurl.toString(), method, requestEntity, String.class);
        return response.getBody();
    }
}

