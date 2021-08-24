/**
 * weiyz19
 * UserToken.java
 * 2021-08-22
 */
package com.example.test_mysql.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "usertoken")
public class UserToken {
  @Id		/** key in table */
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Integer id;
  
  @NotEmpty(message = "用户名不能为空")
  private String username;
  
  @NotEmpty(message = "token不能为空")
  private String token;
  
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
  
  /** override for subsequent use*/
  @Override
  public String toString() {
      return "User {" +
              "id=" + id +
              ", username='" + username + '\'' +
              ", token='" + token + '\'' +
              '}';
  }
}
