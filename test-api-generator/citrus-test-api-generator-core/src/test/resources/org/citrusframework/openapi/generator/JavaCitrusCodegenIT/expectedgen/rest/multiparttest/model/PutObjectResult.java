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

package org.citrusframework.openapi.generator.rest.multiparttest.model;

import java.util.Objects;
import java.util.Arrays;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import org.citrusframework.openapi.generator.rest.multiparttest.model.Metadata;

/**
 * PutObjectResult
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:46.388350800+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PutObjectResult {
  private String versionId;

  private String eTag;

  private OffsetDateTime expirationTime;

  private String expirationTimeRuleId;

  private String contentMd5;

  private Metadata metadata;

  private Boolean isRequesterCharged;

  public PutObjectResult() {
  }

  public PutObjectResult versionId(String versionId) {
    
    this.versionId = versionId;
    return this;
  }

   /**
   * Get versionId
   * @return versionId
  **/
  @jakarta.annotation.Nullable

  public String getVersionId() {
    return versionId;
  }


  public void setVersionId(String versionId) {
    this.versionId = versionId;
  }


  public PutObjectResult eTag(String eTag) {
    
    this.eTag = eTag;
    return this;
  }

   /**
   * Get eTag
   * @return eTag
  **/
  @jakarta.annotation.Nullable

  public String geteTag() {
    return eTag;
  }


  public void seteTag(String eTag) {
    this.eTag = eTag;
  }


  public PutObjectResult expirationTime(OffsetDateTime expirationTime) {
    
    this.expirationTime = expirationTime;
    return this;
  }

   /**
   * Get expirationTime
   * @return expirationTime
  **/
  @jakarta.annotation.Nullable

  public OffsetDateTime getExpirationTime() {
    return expirationTime;
  }


  public void setExpirationTime(OffsetDateTime expirationTime) {
    this.expirationTime = expirationTime;
  }


  public PutObjectResult expirationTimeRuleId(String expirationTimeRuleId) {
    
    this.expirationTimeRuleId = expirationTimeRuleId;
    return this;
  }

   /**
   * Get expirationTimeRuleId
   * @return expirationTimeRuleId
  **/
  @jakarta.annotation.Nullable

  public String getExpirationTimeRuleId() {
    return expirationTimeRuleId;
  }


  public void setExpirationTimeRuleId(String expirationTimeRuleId) {
    this.expirationTimeRuleId = expirationTimeRuleId;
  }


  public PutObjectResult contentMd5(String contentMd5) {
    
    this.contentMd5 = contentMd5;
    return this;
  }

   /**
   * Get contentMd5
   * @return contentMd5
  **/
  @jakarta.annotation.Nullable

  public String getContentMd5() {
    return contentMd5;
  }


  public void setContentMd5(String contentMd5) {
    this.contentMd5 = contentMd5;
  }


  public PutObjectResult metadata(Metadata metadata) {
    
    this.metadata = metadata;
    return this;
  }

   /**
   * Get metadata
   * @return metadata
  **/
  @jakarta.annotation.Nullable

  public Metadata getMetadata() {
    return metadata;
  }


  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }


  public PutObjectResult isRequesterCharged(Boolean isRequesterCharged) {
    
    this.isRequesterCharged = isRequesterCharged;
    return this;
  }

   /**
   * Get isRequesterCharged
   * @return isRequesterCharged
  **/
  @jakarta.annotation.Nullable

  public Boolean getIsRequesterCharged() {
    return isRequesterCharged;
  }


  public void setIsRequesterCharged(Boolean isRequesterCharged) {
    this.isRequesterCharged = isRequesterCharged;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PutObjectResult putObjectResult = (PutObjectResult) o;
    return Objects.equals(this.versionId, putObjectResult.versionId) &&
        Objects.equals(this.eTag, putObjectResult.eTag) &&
        Objects.equals(this.expirationTime, putObjectResult.expirationTime) &&
        Objects.equals(this.expirationTimeRuleId, putObjectResult.expirationTimeRuleId) &&
        Objects.equals(this.contentMd5, putObjectResult.contentMd5) &&
        Objects.equals(this.metadata, putObjectResult.metadata) &&
        Objects.equals(this.isRequesterCharged, putObjectResult.isRequesterCharged);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionId, eTag, expirationTime, expirationTimeRuleId, contentMd5, metadata, isRequesterCharged);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PutObjectResult {\n");
    sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
    sb.append("    eTag: ").append(toIndentedString(eTag)).append("\n");
    sb.append("    expirationTime: ").append(toIndentedString(expirationTime)).append("\n");
    sb.append("    expirationTimeRuleId: ").append(toIndentedString(expirationTimeRuleId)).append("\n");
    sb.append("    contentMd5: ").append(toIndentedString(contentMd5)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    isRequesterCharged: ").append(toIndentedString(isRequesterCharged)).append("\n");
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

