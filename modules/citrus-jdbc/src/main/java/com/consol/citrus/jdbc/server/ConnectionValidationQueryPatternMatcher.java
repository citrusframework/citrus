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

package com.consol.citrus.jdbc.server;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Georgi Todorov
 * @since 2.7.5
 */
public class ConnectionValidationQueryPatternMatcher {

    protected static final String CONNECTION_VALIDATION_QUERIES_PROPERTY = "citrus.jdbc.connection.validaiton.queries";

    /**
     * ';' separated list of validation queries for different databases
     */
    private final String validationQueryPatterns =
                    "SELECT \\w*;" + //e.g. H2, MySQL, PostgreSQL, SQLite, Microsoft SQL Server
                    "SELECT.*FROM DUAL;" + // Oracle
                    "SELECT.*FROM SYSIBM.SYSDUMMY1;"; // DB2

    private Pattern queryValidationPattern;

    public ConnectionValidationQueryPatternMatcher() {
        String connectionValidationQueries = System.getProperty(CONNECTION_VALIDATION_QUERIES_PROPERTY, validationQueryPatterns);
        List<String> validationQueryPatterns = Arrays.stream(connectionValidationQueries.split(";"))
                .map(String::trim)
                .filter(validationQuery -> !StringUtils.isEmpty(validationQuery))
                .map(validationQueryPattern -> "(?i)\\A" + validationQueryPattern + "\\Z")
                .collect(Collectors.toList());
        queryValidationPattern = Pattern.compile(String.join("|", validationQueryPatterns));
    }

    public boolean match(String sqlQuery) {
        return queryValidationPattern.matcher(sqlQuery).find();
    }

}
