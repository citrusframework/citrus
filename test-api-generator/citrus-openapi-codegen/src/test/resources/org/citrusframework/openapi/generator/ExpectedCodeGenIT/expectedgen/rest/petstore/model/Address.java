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
 * Address
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:42.828969400+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class Address {
  @jakarta.annotation.Nullable
  private String street;

  @jakarta.annotation.Nullable
  private String city;

  @jakarta.annotation.Nullable
  private String state;

  @jakarta.annotation.Nullable
  private String zip;

  public Address() {
  }

  public Address street(@jakarta.annotation.Nullable String street) {
    
    this.street = street;
    return this;
  }

  /**
   * Get street
   * @return street
   */
  @jakarta.annotation.Nullable

  public String getStreet() {
    return street;
  }


  public void setStreet(@jakarta.annotation.Nullable String street) {
    this.street = street;
  }

  public Address city(@jakarta.annotation.Nullable String city) {
    
    this.city = city;
    return this;
  }

  /**
   * Get city
   * @return city
   */
  @jakarta.annotation.Nullable

  public String getCity() {
    return city;
  }


  public void setCity(@jakarta.annotation.Nullable String city) {
    this.city = city;
  }

  public Address state(@jakarta.annotation.Nullable String state) {
    
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
   */
  @jakarta.annotation.Nullable

  public String getState() {
    return state;
  }


  public void setState(@jakarta.annotation.Nullable String state) {
    this.state = state;
  }

  public Address zip(@jakarta.annotation.Nullable String zip) {
    
    this.zip = zip;
    return this;
  }

  /**
   * Get zip
   * @return zip
   */
  @jakarta.annotation.Nullable

  public String getZip() {
    return zip;
  }


  public void setZip(@jakarta.annotation.Nullable String zip) {
    this.zip = zip;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(this.street, address.street) &&
        Objects.equals(this.city, address.city) &&
        Objects.equals(this.state, address.state) &&
        Objects.equals(this.zip, address.zip);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city, state, zip);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");
    sb.append("    street: ").append(toIndentedString(street)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    zip: ").append(toIndentedString(zip)).append("\n");
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

