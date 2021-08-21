/**
 * weiyz19
 * UserService.java
 * 2021-08-18
 */
package com.example.test_mysql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.example.test_mysql.domain.MyUser;
import com.example.test_mysql.domain.UserRepo;

import java.util.*;

@Service("userDetailsService")
public class UserService implements UserDetailsService{
	@Autowired 
	private UserRepo userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		/** query by username */
		MyUser user = userRepository.findByUserName(username);
		/** TODO: catch exception */
		if (user == null) {
			throw new UsernameNotFoundException("用户名不存在!");
		}
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		auths.add(new SimpleGrantedAuthority("user"));
		return new User(user.getUsername(), user.getHashedpassword(), auths);
	}
	
    public int addUser(MyUser user) {
    	Map<String, Object> map;
		if(userRepository.findByUserName(user.getUsername()) != null)
			return 2;
        if (user.getUsername() != null && user.getHashedpassword() != null){
        	/** 不保存明文密码 */
        	user.setHashedpassword(passwordEncoder.encode(user.getHashedpassword()));
            userRepository.save(user);
            return 1;
        }
        else
            return 3;
    }
	
//	/**
//     * 基础登录逻辑
//     * @param userName 用户名
//     * @param password MD5 hashed密码
//     * @return
//     */
//	public boolean login(String username, String password){
//        Iterable<MyUser> users = userRepository.findAll();
//        for (MyUser u: users) {
//            if(u.getUsername().equals(username) && u.getHashedpassword().equals(password)){
//                return true;
//            }
//        }
//        return false;
//    }
	
}
