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

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "statement"
})
@XmlRootElement(name = "execute")
public class Execute {

    @XmlElement(required = true)
    protected Statement statement;

    public Execute() {
    }

    public Execute(Statement statement) {
        this.statement = statement;
    }

    /**
     * Gets the statement.
     *
     * @return
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * Sets the statement.
     *
     * @param statement
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "delete",
        "update",
        "insert",
        "select",
        "dropTable",
        "createTable",
        "sql"
    })
    public static class Statement {

        protected Delete delete;
        protected Update update;
        protected Insert insert;
        protected Select select;
        @XmlElement(name = "drop-table")
        protected DropTable dropTable;
        @XmlElement(name = "create-table")
        protected CreateTable createTable;
        protected String sql;

        public Statement() {
        }

        public Statement(Delete delete) {
            this.delete = delete;
        }

        public Statement(Update update) {
            this.update = update;
        }

        public Statement(Insert insert) {
            this.insert = insert;
        }

        public Statement(Select select) {
            this.select = select;
        }

        public Statement(DropTable dropTable) {
            this.dropTable = dropTable;
        }

        public Statement(CreateTable createTable) {
            this.createTable = createTable;
        }

        public Statement(String sql) {
            this.sql = sql;
        }

        /**
         * Gets the delete.
         *
         * @return
         */
        public Delete getDelete() {
            return delete;
        }

        /**
         * Sets the delete.
         *
         * @param delete
         */
        public void setDelete(Delete delete) {
            this.delete = delete;
        }

        /**
         * Gets the update.
         *
         * @return
         */
        public Update getUpdate() {
            return update;
        }

        /**
         * Sets the update.
         *
         * @param update
         */
        public void setUpdate(Update update) {
            this.update = update;
        }

        /**
         * Gets the insert.
         *
         * @return
         */
        public Insert getInsert() {
            return insert;
        }

        /**
         * Sets the insert.
         *
         * @param insert
         */
        public void setInsert(Insert insert) {
            this.insert = insert;
        }

        /**
         * Gets the select.
         *
         * @return
         */
        public Select getSelect() {
            return select;
        }

        /**
         * Sets the select.
         *
         * @param select
         */
        public void setSelect(Select select) {
            this.select = select;
        }

        /**
         * Gets the dropTable.
         *
         * @return
         */
        public DropTable getDropTable() {
            return dropTable;
        }

        /**
         * Sets the dropTable.
         *
         * @param dropTable
         */
        public void setDropTable(DropTable dropTable) {
            this.dropTable = dropTable;
        }

        /**
         * Gets the createTable.
         *
         * @return
         */
        public CreateTable getCreateTable() {
            return createTable;
        }

        /**
         * Sets the createTable.
         *
         * @param createTable
         */
        public void setCreateTable(CreateTable createTable) {
            this.createTable = createTable;
        }

        /**
         * Gets the sql.
         *
         * @return
         */
        public String getSql() {
            return sql;
        }

        /**
         * Sets the sql.
         *
         * @param sql
         */
        public void setSql(String sql) {
            this.sql = sql;
        }
    }

}
