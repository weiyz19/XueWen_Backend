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
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		/** query by username */
		MyUser user = userRepository.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("用户名不存在!");
		}
		List<GrantedAuthority> auths = new ArrayList<>();
		auths.add(new SimpleGrantedAuthority("user"));
		return new User(user.getUsername(), user.getHashedpassword(), auths);
	}
}
