/**
 * weiyz19
 * UserService.java
 * 2021-08-18
 */
package com.example.test_mysql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.domain.User;

@Service
public class UserService {
	@Autowired 
	private UserRepo userRepository;
	/** "@Autowire" is used for UserRepository class to handle the database */
   
	/**
     * 基础登录逻辑
     * @param userName 用户名
     * @param password MD5 hashed密码
     * @return
     */
	public boolean login(String username, String password){
        Iterable<User> users = userRepository.findAll();
        for (User u: users) {
            if(u.getUsername().equals(username) && u.getHashedpassword().equals(password)){
                return true;
            }
        }
        return false;
    }
	
}
