/*
* Copyright the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.citrusframework.openapi.generator.rest.petstore.model;

import java.util.Objects;
import java.util.Arrays;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * User
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-07-25T13:14:37.509644+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class User {
  @jakarta.annotation.Nullable
  private Long id;

  @jakarta.annotation.Nullable
  private String username;

  @jakarta.annotation.Nullable
  private String firstName;

  @jakarta.annotation.Nullable
  private String lastName;

  @jakarta.annotation.Nullable
  private String email;

  @jakarta.annotation.Nullable
  private String password;

  @jakarta.annotation.Nullable
  private String phone;

  @jakarta.annotation.Nullable
  private Integer userStatus;

  public User() {
  }

  public User id(@jakarta.annotation.Nullable Long id) {
    
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @jakarta.annotation.Nullable

  public Long getId() {
    return id;
  }


  public void setId(@jakarta.annotation.Nullable Long id) {
    this.id = id;
  }

  public User username(@jakarta.annotation.Nullable String username) {
    
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
   */
  @jakarta.annotation.Nullable

  public String getUsername() {
    return username;
  }


  public void setUsername(@jakarta.annotation.Nullable String username) {
    this.username = username;
  }

  public User firstName(@jakarta.annotation.Nullable String firstName) {
    
    this.firstName = firstName;
    return this;
  }

  /**
   * Get firstName
   * @return firstName
   */
  @jakarta.annotation.Nullable

  public String getFirstName() {
    return firstName;
  }


  public void setFirstName(@jakarta.annotation.Nullable String firstName) {
    this.firstName = firstName;
  }

  public User lastName(@jakarta.annotation.Nullable String lastName) {
    
    this.lastName = lastName;
    return this;
  }

  /**
   * Get lastName
   * @return lastName
   */
  @jakarta.annotation.Nullable

  public String getLastName() {
    return lastName;
  }


  public void setLastName(@jakarta.annotation.Nullable String lastName) {
    this.lastName = lastName;
  }

  public User email(@jakarta.annotation.Nullable String email) {
    
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @jakarta.annotation.Nullable

  public String getEmail() {
    return email;
  }


  public void setEmail(@jakarta.annotation.Nullable String email) {
    this.email = email;
  }

  public User password(@jakarta.annotation.Nullable String password) {
    
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
   */
  @jakarta.annotation.Nullable

  public String getPassword() {
    return password;
  }


  public void setPassword(@jakarta.annotation.Nullable String password) {
    this.password = password;
  }

  public User phone(@jakarta.annotation.Nullable String phone) {
    
    this.phone = phone;
    return this;
  }

  /**
   * Get phone
   * @return phone
   */
  @jakarta.annotation.Nullable

  public String getPhone() {
    return phone;
  }


  public void setPhone(@jakarta.annotation.Nullable String phone) {
    this.phone = phone;
  }

  public User userStatus(@jakarta.annotation.Nullable Integer userStatus) {
    
    this.userStatus = userStatus;
    return this;
  }

  /**
   * User Status
   * @return userStatus
   */
  @jakarta.annotation.Nullable

  public Integer getUserStatus() {
    return userStatus;
  }


  public void setUserStatus(@jakarta.annotation.Nullable Integer userStatus) {
    this.userStatus = userStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(this.id, user.id) &&
        Objects.equals(this.username, user.username) &&
        Objects.equals(this.firstName, user.firstName) &&
        Objects.equals(this.lastName, user.lastName) &&
        Objects.equals(this.email, user.email) &&
        Objects.equals(this.password, user.password) &&
        Objects.equals(this.phone, user.phone) &&
        Objects.equals(this.userStatus, user.userStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, firstName, lastName, email, password, phone, userStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    userStatus: ").append(toIndentedString(userStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

