/**
 * weiyz19
 * User.java
 * 2021-08-17
 */
package com.example.test_mysql.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;


@Entity // This tells Hibernate to make a table out of this class
@Table(name = "userlist")
public class User {
  @Id		/** key in table */
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Integer id;
  
  @NotEmpty(message = "用户名不能为空")
  private String username;
  
  @NotEmpty(message = "邮箱不能为空")
  private String email;
  
  @NotEmpty(message = "手机号不能为空")
  private String phone;
  
  @NotEmpty(message = "密码不能为空")
  private String hashedpassword;

  
  
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getPhone() {
	    return phone;
	  }

  public void setPhone(String phone) {
	    this.phone = phone;
  }
  
  public String getHashedpassword() {
	    return hashedpassword;
  }
  public void setHashedpassword(String hashedpassword) {
	    this.hashedpassword = hashedpassword;
  }
  
  /** override for subsequent use*/
  @Override
  public String toString() {
      return "User{" +
              "id=" + id +
              ", username='" + username + '\'' +
              ", email='" + email + '\'' +
              ", phone='" + phone + '\'' +
              ", hashedpassword='" + hashedpassword + '\'' +
              '}';
  }
}
