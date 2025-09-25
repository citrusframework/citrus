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
import java.time.OffsetDateTime;

/**
 * Order
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-09-24T14:15:33.859186700+02:00[Europe/Zurich]", comments = "Generator version: 7.15.0")
public class Order {
  @jakarta.annotation.Nullable
  private Long id;

  @jakarta.annotation.Nullable
  private Long petId;

  @jakarta.annotation.Nullable
  private Integer quantity;

  @jakarta.annotation.Nullable
  private OffsetDateTime shipDate;

  /**
   * Order Status
   */
  public enum StatusEnum {
    PLACED(String.valueOf("placed")),
    
    APPROVED(String.valueOf("approved")),
    
    DELIVERED(String.valueOf("delivered"));

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

  @jakarta.annotation.Nullable
  private Boolean complete;

  public Order() {
  }

  public Order id(@jakarta.annotation.Nullable Long id) {
    
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

  public Order petId(@jakarta.annotation.Nullable Long petId) {
    
    this.petId = petId;
    return this;
  }

  /**
   * Get petId
   * @return petId
   */
  @jakarta.annotation.Nullable

  public Long getPetId() {
    return petId;
  }


  public void setPetId(@jakarta.annotation.Nullable Long petId) {
    this.petId = petId;
  }

  public Order quantity(@jakarta.annotation.Nullable Integer quantity) {
    
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
   */
  @jakarta.annotation.Nullable

  public Integer getQuantity() {
    return quantity;
  }


  public void setQuantity(@jakarta.annotation.Nullable Integer quantity) {
    this.quantity = quantity;
  }

  public Order shipDate(@jakarta.annotation.Nullable OffsetDateTime shipDate) {
    
    this.shipDate = shipDate;
    return this;
  }

  /**
   * Get shipDate
   * @return shipDate
   */
  @jakarta.annotation.Nullable

  public OffsetDateTime getShipDate() {
    return shipDate;
  }


  public void setShipDate(@jakarta.annotation.Nullable OffsetDateTime shipDate) {
    this.shipDate = shipDate;
  }

  public Order status(@jakarta.annotation.Nullable StatusEnum status) {
    
    this.status = status;
    return this;
  }

  /**
   * Order Status
   * @return status
   */
  @jakarta.annotation.Nullable

  public StatusEnum getStatus() {
    return status;
  }


  public void setStatus(@jakarta.annotation.Nullable StatusEnum status) {
    this.status = status;
  }

  public Order complete(@jakarta.annotation.Nullable Boolean complete) {
    
    this.complete = complete;
    return this;
  }

  /**
   * Get complete
   * @return complete
   */
  @jakarta.annotation.Nullable

  public Boolean getComplete() {
    return complete;
  }


  public void setComplete(@jakarta.annotation.Nullable Boolean complete) {
    this.complete = complete;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return Objects.equals(this.id, order.id) &&
        Objects.equals(this.petId, order.petId) &&
        Objects.equals(this.quantity, order.quantity) &&
        Objects.equals(this.shipDate, order.shipDate) &&
        Objects.equals(this.status, order.status) &&
        Objects.equals(this.complete, order.complete);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, petId, quantity, shipDate, status, complete);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Order {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    petId: ").append(toIndentedString(petId)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    shipDate: ").append(toIndentedString(shipDate)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    complete: ").append(toIndentedString(complete)).append("\n");
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

