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

package org.citrusframework.openapi.generator.rest.extpetstore.model;

import java.util.Objects;
import java.util.Arrays;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.citrusframework.openapi.generator.rest.extpetstore.model.Address;

/**
 * Object containing Owner data
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-07-25T13:14:38.461661900+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class Owner {
  @jakarta.annotation.Nullable
  private String _name;

  @jakarta.annotation.Nullable
  private String email;

  @jakarta.annotation.Nullable
  private String phone;

  @jakarta.annotation.Nullable
  private Address address;

  public Owner() {
  }

  public Owner _name(@jakarta.annotation.Nullable String _name) {
    
    this._name = _name;
    return this;
  }

  /**
   * Get _name
   * @return _name
   */
  @jakarta.annotation.Nullable

  public String getName() {
    return _name;
  }


  public void setName(@jakarta.annotation.Nullable String _name) {
    this._name = _name;
  }

  public Owner email(@jakarta.annotation.Nullable String email) {
    
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

  public Owner phone(@jakarta.annotation.Nullable String phone) {
    
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

  public Owner address(@jakarta.annotation.Nullable Address address) {
    
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
   */
  @jakarta.annotation.Nullable

  public Address getAddress() {
    return address;
  }


  public void setAddress(@jakarta.annotation.Nullable Address address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Owner owner = (Owner) o;
    return Objects.equals(this._name, owner._name) &&
        Objects.equals(this.email, owner.email) &&
        Objects.equals(this.phone, owner.phone) &&
        Objects.equals(this.address, owner.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, email, phone, address);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Owner {\n");
    sb.append("    _name: ").append(toIndentedString(_name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

