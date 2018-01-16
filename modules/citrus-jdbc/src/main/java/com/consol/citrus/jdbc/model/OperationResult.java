/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "success",
    "exception",
    "resultSet"
})
@XmlRootElement(name = "operation-result")
public class OperationResult {

    protected boolean success;
    protected String exception;
    @XmlElement(name = "result-set")
    protected ResultSet resultSet;

    public OperationResult() {
    }

    public OperationResult(boolean success) {
        this.success = success;
    }

    /**
     * Adds result set in fluent api.
     * @param resultSet
     * @return
     */
    public OperationResult withResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        return this;
    }

    /**
     * Gets the success.
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success.
     *
     * @param success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the exception.
     *
     * @return
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets the exception.
     *
     * @param exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Gets the resultSet.
     *
     * @return
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * Sets the resultSet.
     *
     * @param resultSet
     */
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
}
