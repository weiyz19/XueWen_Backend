/**
 * weiyz19
 * UserTokenRepo.java
 * 2021-08-24
 */
package com.example.test_mysql.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserTokenRepo extends CrudRepository<UserToken, Integer> {
	
	@Query("select t from UserToken t where t.username = :username")
    MyUser findByUserName(@Param("username") String username);
}
