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
    "execute",
    "createStatement",
    "closeStatement",
    "createPreparedStatement",
    "openConnection",
    "closeConnection"
})
@XmlRootElement(name = "operation")
public class Operation {

    protected Execute execute;
    @XmlElement(name = "create-statement")
    protected CreateStatement createStatement;
    @XmlElement(name = "create-prepared-statement")
    protected CreatePreparedStatement createPreparedStatement;
    @XmlElement(name = "close-connection")
    protected CloseConnection closeConnection;
    @XmlElement(name = "close-statement")
    protected CloseStatement closeStatement;
    @XmlElement(name = "open-connection")
    protected OpenConnection openConnection;

    /**
     * Default constructor.
     */
    public Operation() {
        super();
    }

    public Operation(OpenConnection operation) {
        this.openConnection = operation;
    }

    public Operation(CloseConnection operation) {
        this.closeConnection = operation;
    }

    public Operation(CloseStatement operation) {
        this.closeStatement = operation;
    }

    public Operation(CreateStatement operation) {
        this.createStatement = operation;
    }

    public Operation(CreatePreparedStatement operation) {
        this.createPreparedStatement = operation;
    }

    public Operation(Execute operation) {
        this.execute = operation;
    }

    /**
     * Gets the execute.
     *
     * @return
     */
    public Execute getExecute() {
        return execute;
    }

    /**
     * Sets the execute.
     *
     * @param execute
     */
    public void setExecute(Execute execute) {
        this.execute = execute;
    }

    /**
     * Gets the createStatement.
     *
     * @return
     */
    public CreateStatement getCreateStatement() {
        return createStatement;
    }

    /**
     * Sets the createStatement.
     *
     * @param createStatement
     */
    public void setCreateStatement(CreateStatement createStatement) {
        this.createStatement = createStatement;
    }

    /**
     * Gets the createPreparedStatement.
     *
     * @return
     */
    public CreatePreparedStatement getCreatePreparedStatement() {
        return createPreparedStatement;
    }

    /**
     * Sets the createPreparedStatement.
     *
     * @param createPreparedStatement
     */
    public void setCreatePreparedStatement(CreatePreparedStatement createPreparedStatement) {
        this.createPreparedStatement = createPreparedStatement;
    }

    /**
     * Gets the closeConnection.
     *
     * @return
     */
    public CloseConnection getCloseConnection() {
        return closeConnection;
    }

    /**
     * Sets the closeConnection.
     *
     * @param closeConnection
     */
    public void setCloseConnection(CloseConnection closeConnection) {
        this.closeConnection = closeConnection;
    }

    /**
     * Gets the closeStatement.
     *
     * @return
     */
    public CloseStatement getCloseStatement() {
        return closeStatement;
    }

    /**
     * Sets the closeStatement.
     *
     * @param closeStatement
     */
    public void setCloseStatement(CloseStatement closeStatement) {
        this.closeStatement = closeStatement;
    }

    /**
     * Gets the openConnection.
     *
     * @return
     */
    public OpenConnection getOpenConnection() {
        return openConnection;
    }

    /**
     * Sets the openConnection.
     *
     * @param openConnection
     */
    public void setOpenConnection(OpenConnection openConnection) {
        this.openConnection = openConnection;
    }
}
