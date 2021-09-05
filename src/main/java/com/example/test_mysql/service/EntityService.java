/**
 * weiyz19
 * EntityService.java
 * 2021-08-28
 */
package com.example.test_mysql.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.test_mysql.domain.EntityRepoImp;
import com.example.test_mysql.domain.MyEntity;

import net.sf.json.JSONArray;


/** courses: 0. 语文 1. 数学 2. 英语 3. 物理 4. 化学 5. 生物 6. 历史 7. 地理 8. 政治 */
@Service("EntityService")
@Async("asyncServiceExecutor")
public class EntityService {
	
	@Autowired
	private EntityRepoImp entityRepoImp;
	
	public JSONArray findEntities(int course, String name) {
		if (course == 9) {
			try {
				List<String> params = new LinkedList<>();
				params.add(name);
				return entityRepoImp.findAllByNameIn(params);
			} catch (Exception e) {
				return null;
			}
		}
		try {
			List<Object> params = new LinkedList<>();
			params.add(course);
			params.add(name);
			return entityRepoImp.findByNameIn(params);
		} catch (Exception e) {
			return null;
		}
	}

	public String getEntity(int course, String name, int userID) {
		try {
			List<Object> params = new LinkedList<>();
			params.add(course);
			params.add(name);
			params.add(userID);
			return entityRepoImp.findDetailByNameIn(params);
		} catch (Exception e) {
			return null;
		}
	}
	

	public void updateHistory(List<Object> params) {
		entityRepoImp.updateHistoryIn(params);
	}
	
	public JSONArray getHistory(String userID) {
		List<Integer> params = new LinkedList<>();
		params.add(Integer.parseInt(userID));
		return entityRepoImp.findHistoryIn(params);
	}
	
}
