/**
 * weiyz19
 * AuthService.java
 * 2021-08-23
 */
package com.example.test_mysql.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.test_mysql.config.JwtTokenUtil;
import com.example.test_mysql.domain.MyUser;
import com.example.test_mysql.domain.UserRepo;
import com.example.test_mysql.domain.AuthenticationException;

@Service
public class AuthService {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	@Autowired
	private UserRepo userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public int register(MyUser user) {
		if(userRepository.findByUsername(user.getUsername()) != null)
			return 1;
		/** 不保存明文密码 */
		user.setHashedpassword(passwordEncoder.encode(user.getHashedpassword()));
        userRepository.save(user);
        return 0;
    }

    public String login(String username, String password) throws AuthenticationException {
    	try {
    		authenticate(username, password);
    		final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtTokenUtil.generateToken(userDetails);
		} catch (Exception e) {		// 如果有问题 就返回空的token
			return "";
		}
    }

    public String refresh(String oldToken) {
        final String token = oldToken.substring(JwtTokenUtil.TOKENHEAD.length());
        if (jwtTokenUtil.canRefresh(token)){
            return jwtTokenUtil.refreshToken(token);
        }
        return null;
    }
	
    /**
     * Authenticates the user by username & password. an {@link AuthenticationException} will be thrown
     */
    private void authenticate(String username, String password) {
    	Objects.requireNonNull(username);
    	Objects.requireNonNull(password);
    	try {
    		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    	} catch (DisabledException e) {
    		System.err.println("User is disabled!");
    		throw new AuthenticationException("User is disabled!", e);
    	} catch (BadCredentialsException e) {
    		System.err.println("Bad credentials!");
    		throw new AuthenticationException("Bad credentials!", e);
    	}
    }
}
