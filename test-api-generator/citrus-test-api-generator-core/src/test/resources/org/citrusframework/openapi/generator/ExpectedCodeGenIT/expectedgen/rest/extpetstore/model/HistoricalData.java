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
import java.time.LocalDate;

/**
 * Additional historical data for a vaccination report, not contained in internal storage. 
 */
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:46.194751400+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class HistoricalData {
  private LocalDate lastVaccinationDate;

  private Integer vaccinationCount;

  public HistoricalData() {
  }

  public HistoricalData lastVaccinationDate(LocalDate lastVaccinationDate) {
    
    this.lastVaccinationDate = lastVaccinationDate;
    return this;
  }

   /**
   * The date of the last vaccination.
   * @return lastVaccinationDate
  **/
  @jakarta.annotation.Nullable

  public LocalDate getLastVaccinationDate() {
    return lastVaccinationDate;
  }


  public void setLastVaccinationDate(LocalDate lastVaccinationDate) {
    this.lastVaccinationDate = lastVaccinationDate;
  }


  public HistoricalData vaccinationCount(Integer vaccinationCount) {
    
    this.vaccinationCount = vaccinationCount;
    return this;
  }

   /**
   * The number of vaccinations the pet has received.
   * @return vaccinationCount
  **/
  @jakarta.annotation.Nullable

  public Integer getVaccinationCount() {
    return vaccinationCount;
  }


  public void setVaccinationCount(Integer vaccinationCount) {
    this.vaccinationCount = vaccinationCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HistoricalData historicalData = (HistoricalData) o;
    return Objects.equals(this.lastVaccinationDate, historicalData.lastVaccinationDate) &&
        Objects.equals(this.vaccinationCount, historicalData.vaccinationCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastVaccinationDate, vaccinationCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HistoricalData {\n");
    sb.append("    lastVaccinationDate: ").append(toIndentedString(lastVaccinationDate)).append("\n");
    sb.append("    vaccinationCount: ").append(toIndentedString(vaccinationCount)).append("\n");
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

