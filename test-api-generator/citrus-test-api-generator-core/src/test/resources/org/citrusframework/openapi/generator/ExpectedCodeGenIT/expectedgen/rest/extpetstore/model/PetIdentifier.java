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

/**
 * PetIdentifier
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-28T13:20:44.158583300+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetIdentifier {
  private String _name;

  private String alias;

  public PetIdentifier() {
  }

  public PetIdentifier _name(String _name) {
    
    this._name = _name;
    return this;
  }

   /**
   * Get _name
   * @return _name
  **/
  @jakarta.annotation.Nullable

  public String getName() {
    return _name;
  }


  public void setName(String _name) {
    this._name = _name;
  }


  public PetIdentifier alias(String alias) {
    
    this.alias = alias;
    return this;
  }

   /**
   * Get alias
   * @return alias
  **/
  @jakarta.annotation.Nullable

  public String getAlias() {
    return alias;
  }


  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PetIdentifier petIdentifier = (PetIdentifier) o;
    return Objects.equals(this._name, petIdentifier._name) &&
        Objects.equals(this.alias, petIdentifier.alias);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, alias);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PetIdentifier {\n");
    sb.append("    _name: ").append(toIndentedString(_name)).append("\n");
    sb.append("    alias: ").append(toIndentedString(alias)).append("\n");
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

