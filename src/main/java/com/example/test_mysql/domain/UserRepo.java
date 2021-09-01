/**
 * weiyz19
 * UserRepo.java
 * 2021-08-17
 */
package com.example.test_mysql.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepo extends CrudRepository<MyUser, Integer> {
	
	@Query("select t from MyUser t where t.username = :username")
    MyUser findByUsername(@Param("username") String username);
}
