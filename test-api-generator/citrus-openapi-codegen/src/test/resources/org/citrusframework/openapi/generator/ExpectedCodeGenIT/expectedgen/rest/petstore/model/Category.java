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
 * Category
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:42.828969400+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class Category {
  @jakarta.annotation.Nullable
  private Long id;

  @jakarta.annotation.Nullable
  private String _name;

  public Category() {
  }

  public Category id(@jakarta.annotation.Nullable Long id) {
    
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

  public Category _name(@jakarta.annotation.Nullable String _name) {
    
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Category category = (Category) o;
    return Objects.equals(this.id, category.id) &&
        Objects.equals(this._name, category._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, _name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Category {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    _name: ").append(toIndentedString(_name)).append("\n");
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

