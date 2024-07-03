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
 * Describes the result of uploading an image resource
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:45.610010900+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ModelApiResponse {
  private Integer code;

  private String _type;

  private String message;

  public ModelApiResponse() {
  }

  public ModelApiResponse code(Integer code) {
    
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
  @jakarta.annotation.Nullable

  public Integer getCode() {
    return code;
  }


  public void setCode(Integer code) {
    this.code = code;
  }


  public ModelApiResponse _type(String _type) {
    
    this._type = _type;
    return this;
  }

   /**
   * Get _type
   * @return _type
  **/
  @jakarta.annotation.Nullable

  public String getType() {
    return _type;
  }


  public void setType(String _type) {
    this._type = _type;
  }


  public ModelApiResponse message(String message) {
    
    this.message = message;
    return this;
  }

   /**
   * Get message
   * @return message
  **/
  @jakarta.annotation.Nullable

  public String getMessage() {
    return message;
  }


  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelApiResponse _apiResponse = (ModelApiResponse) o;
    return Objects.equals(this.code, _apiResponse.code) &&
        Objects.equals(this._type, _apiResponse._type) &&
        Objects.equals(this.message, _apiResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, _type, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelApiResponse {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    _type: ").append(toIndentedString(_type)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

