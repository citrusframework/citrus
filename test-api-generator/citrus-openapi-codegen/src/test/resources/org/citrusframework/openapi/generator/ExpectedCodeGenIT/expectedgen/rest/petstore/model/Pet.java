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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.citrusframework.openapi.generator.rest.petstore.model.Category;
import org.citrusframework.openapi.generator.rest.petstore.model.Tag;

/**
 * Pet
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:42.828969400+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class Pet {
  @jakarta.annotation.Nullable
  private Long id;

  @jakarta.annotation.Nonnull
  private String _name;

  @jakarta.annotation.Nullable
  private Category category;

  @jakarta.annotation.Nonnull
  private List<String> photoUrls = new ArrayList<>();

  @jakarta.annotation.Nullable
  private List<Tag> tags = new ArrayList<>();

  /**
   * pet status in the store
   */
  public enum StatusEnum {
    AVAILABLE(String.valueOf("available")),
    
    PENDING(String.valueOf("pending")),
    
    SOLD(String.valueOf("sold"));

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @jakarta.annotation.Nullable
  private StatusEnum status;

  public Pet() {
  }

  public Pet id(@jakarta.annotation.Nullable Long id) {
    
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

  public Pet _name(@jakarta.annotation.Nonnull String _name) {
    
    this._name = _name;
    return this;
  }

  /**
   * Get _name
   * @return _name
   */
  @jakarta.annotation.Nonnull

  public String getName() {
    return _name;
  }


  public void setName(@jakarta.annotation.Nonnull String _name) {
    this._name = _name;
  }

  public Pet category(@jakarta.annotation.Nullable Category category) {
    
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
   */
  @jakarta.annotation.Nullable

  public Category getCategory() {
    return category;
  }


  public void setCategory(@jakarta.annotation.Nullable Category category) {
    this.category = category;
  }

  public Pet photoUrls(@jakarta.annotation.Nonnull List<String> photoUrls) {
    
    this.photoUrls = photoUrls;
    return this;
  }

  public Pet addPhotoUrlsItem(String photoUrlsItem) {
    if (this.photoUrls == null) {
      this.photoUrls = new ArrayList<>();
    }
    this.photoUrls.add(photoUrlsItem);
    return this;
  }

  /**
   * Get photoUrls
   * @return photoUrls
   */
  @jakarta.annotation.Nonnull

  public List<String> getPhotoUrls() {
    return photoUrls;
  }


  public void setPhotoUrls(@jakarta.annotation.Nonnull List<String> photoUrls) {
    this.photoUrls = photoUrls;
  }

  public Pet tags(@jakarta.annotation.Nullable List<Tag> tags) {
    
    this.tags = tags;
    return this;
  }

  public Pet addTagsItem(Tag tagsItem) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }
    this.tags.add(tagsItem);
    return this;
  }

  /**
   * Get tags
   * @return tags
   */
  @jakarta.annotation.Nullable

  public List<Tag> getTags() {
    return tags;
  }


  public void setTags(@jakarta.annotation.Nullable List<Tag> tags) {
    this.tags = tags;
  }

  public Pet status(@jakarta.annotation.Nullable StatusEnum status) {
    
    this.status = status;
    return this;
  }

  /**
   * pet status in the store
   * @return status
   */
  @jakarta.annotation.Nullable

  public StatusEnum getStatus() {
    return status;
  }


  public void setStatus(@jakarta.annotation.Nullable StatusEnum status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pet pet = (Pet) o;
    return Objects.equals(this.id, pet.id) &&
        Objects.equals(this._name, pet._name) &&
        Objects.equals(this.category, pet.category) &&
        Objects.equals(this.photoUrls, pet.photoUrls) &&
        Objects.equals(this.tags, pet.tags) &&
        Objects.equals(this.status, pet.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, _name, category, photoUrls, tags, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Pet {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    _name: ").append(toIndentedString(_name)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    photoUrls: ").append(toIndentedString(photoUrls)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

