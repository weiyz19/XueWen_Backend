/**
 * weiyz19
 * UserRepo.java
 * 2021-08-17
 */
package com.example.test_mysql.domain;

import org.springframework.data.repository.CrudRepository;

import com.example.test_mysql.domain.User;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepo extends CrudRepository<User, Integer> {

}
