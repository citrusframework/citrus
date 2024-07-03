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
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:46.388350800+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class Metadata {
  private Map<String, String> userMetadata = new HashMap<>();

  private Map<String, String> rawMetadata = new HashMap<>();

  private OffsetDateTime httpExpiresDate;

  private OffsetDateTime expirationTime;

  private String expirationTimeRuleId;

  private Boolean ongoingRestore;

  private OffsetDateTime restoreExpirationTime;

  private Boolean bucketKeyEnabled;

  public Metadata() {
  }

  public Metadata userMetadata(Map<String, String> userMetadata) {
    
    this.userMetadata = userMetadata;
    return this;
  }

  public Metadata putUserMetadataItem(String key, String userMetadataItem) {
    if (this.userMetadata == null) {
      this.userMetadata = new HashMap<>();
    }
    this.userMetadata.put(key, userMetadataItem);
    return this;
  }

   /**
   * Get userMetadata
   * @return userMetadata
  **/
  @jakarta.annotation.Nullable

  public Map<String, String> getUserMetadata() {
    return userMetadata;
  }


  public void setUserMetadata(Map<String, String> userMetadata) {
    this.userMetadata = userMetadata;
  }


  public Metadata rawMetadata(Map<String, String> rawMetadata) {
    
    this.rawMetadata = rawMetadata;
    return this;
  }

  public Metadata putRawMetadataItem(String key, String rawMetadataItem) {
    if (this.rawMetadata == null) {
      this.rawMetadata = new HashMap<>();
    }
    this.rawMetadata.put(key, rawMetadataItem);
    return this;
  }

   /**
   * Get rawMetadata
   * @return rawMetadata
  **/
  @jakarta.annotation.Nullable

  public Map<String, String> getRawMetadata() {
    return rawMetadata;
  }


  public void setRawMetadata(Map<String, String> rawMetadata) {
    this.rawMetadata = rawMetadata;
  }


  public Metadata httpExpiresDate(OffsetDateTime httpExpiresDate) {
    
    this.httpExpiresDate = httpExpiresDate;
    return this;
  }

   /**
   * Get httpExpiresDate
   * @return httpExpiresDate
  **/
  @jakarta.annotation.Nullable

  public OffsetDateTime getHttpExpiresDate() {
    return httpExpiresDate;
  }


  public void setHttpExpiresDate(OffsetDateTime httpExpiresDate) {
    this.httpExpiresDate = httpExpiresDate;
  }


  public Metadata expirationTime(OffsetDateTime expirationTime) {
    
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


  public Metadata expirationTimeRuleId(String expirationTimeRuleId) {
    
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


  public Metadata ongoingRestore(Boolean ongoingRestore) {
    
    this.ongoingRestore = ongoingRestore;
    return this;
  }

   /**
   * Get ongoingRestore
   * @return ongoingRestore
  **/
  @jakarta.annotation.Nullable

  public Boolean getOngoingRestore() {
    return ongoingRestore;
  }


  public void setOngoingRestore(Boolean ongoingRestore) {
    this.ongoingRestore = ongoingRestore;
  }


  public Metadata restoreExpirationTime(OffsetDateTime restoreExpirationTime) {
    
    this.restoreExpirationTime = restoreExpirationTime;
    return this;
  }

   /**
   * Get restoreExpirationTime
   * @return restoreExpirationTime
  **/
  @jakarta.annotation.Nullable

  public OffsetDateTime getRestoreExpirationTime() {
    return restoreExpirationTime;
  }


  public void setRestoreExpirationTime(OffsetDateTime restoreExpirationTime) {
    this.restoreExpirationTime = restoreExpirationTime;
  }


  public Metadata bucketKeyEnabled(Boolean bucketKeyEnabled) {
    
    this.bucketKeyEnabled = bucketKeyEnabled;
    return this;
  }

   /**
   * Get bucketKeyEnabled
   * @return bucketKeyEnabled
  **/
  @jakarta.annotation.Nullable

  public Boolean getBucketKeyEnabled() {
    return bucketKeyEnabled;
  }


  public void setBucketKeyEnabled(Boolean bucketKeyEnabled) {
    this.bucketKeyEnabled = bucketKeyEnabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return Objects.equals(this.userMetadata, metadata.userMetadata) &&
        Objects.equals(this.rawMetadata, metadata.rawMetadata) &&
        Objects.equals(this.httpExpiresDate, metadata.httpExpiresDate) &&
        Objects.equals(this.expirationTime, metadata.expirationTime) &&
        Objects.equals(this.expirationTimeRuleId, metadata.expirationTimeRuleId) &&
        Objects.equals(this.ongoingRestore, metadata.ongoingRestore) &&
        Objects.equals(this.restoreExpirationTime, metadata.restoreExpirationTime) &&
        Objects.equals(this.bucketKeyEnabled, metadata.bucketKeyEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userMetadata, rawMetadata, httpExpiresDate, expirationTime, expirationTimeRuleId, ongoingRestore, restoreExpirationTime, bucketKeyEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Metadata {\n");
    sb.append("    userMetadata: ").append(toIndentedString(userMetadata)).append("\n");
    sb.append("    rawMetadata: ").append(toIndentedString(rawMetadata)).append("\n");
    sb.append("    httpExpiresDate: ").append(toIndentedString(httpExpiresDate)).append("\n");
    sb.append("    expirationTime: ").append(toIndentedString(expirationTime)).append("\n");
    sb.append("    expirationTimeRuleId: ").append(toIndentedString(expirationTimeRuleId)).append("\n");
    sb.append("    ongoingRestore: ").append(toIndentedString(ongoingRestore)).append("\n");
    sb.append("    restoreExpirationTime: ").append(toIndentedString(restoreExpirationTime)).append("\n");
    sb.append("    bucketKeyEnabled: ").append(toIndentedString(bucketKeyEnabled)).append("\n");
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

