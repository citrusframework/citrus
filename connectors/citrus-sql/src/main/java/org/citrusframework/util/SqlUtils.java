/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class SqlUtils {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    /** Constant representing SQL comment */
    public static final String SQL_COMMENT = "--";

    /** Default sql statement ending */
    public static final String STMT_ENDING = ";";

    /**
     * Prevent instantiation.
     */
    private SqlUtils() {
        super();
    }

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     *
     * @param sqlResource the sql file resource.
     * @return list of SQL statements.
     */
    public static List<String> createStatementsFromFileResource(Resource sqlResource) {
        return createStatementsFromFileResource(sqlResource, null);
    }

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     *
     * @param sqlResource the sql file resource.
     * @param lineDecorator optional line decorator for last script lines.
     * @return list of SQL statements.
     */
    public static List<String> createStatementsFromFileResource(Resource sqlResource, LastScriptLineDecorator lineDecorator) {
        BufferedReader reader = null;
        StringBuffer buffer;

        List<String> stmts = new ArrayList<>();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Create statements from SQL file: " + sqlResource.getFile().getAbsolutePath());
            }

            reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
            buffer = new StringBuffer();

            String line;
            while (reader.ready()) {
                line = reader.readLine();

                if (line != null && !line.trim().startsWith(SQL_COMMENT) && line.trim().length() > 0) {
                    if (line.trim().endsWith(getStatementEndingCharacter(lineDecorator))) {
                        if (lineDecorator != null) {
                            buffer.append(lineDecorator.decorate(line));
                        } else {
                            buffer.append(line);
                        }

                        String stmt = buffer.toString().trim();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Found statement: " + stmt);
                        }

                        stmts.add(stmt);
                        buffer.setLength(0);
                        buffer = new StringBuffer();
                    } else {
                        buffer.append(line);

                        //more lines to come for this statement add line break
                        buffer.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Resource could not be found - filename: " + sqlResource, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Warning: Error while closing reader instance", e);
                }
            }
        }

        return stmts;
    }

    /**
     * Gets the SQL statement ending character sequence.
     *
     * @param lineDecorator
     * @return
     */
    public static String getStatementEndingCharacter(LastScriptLineDecorator lineDecorator) {
        if (lineDecorator != null) {
            return lineDecorator.getStatementEndingCharacter();
        }

        return STMT_ENDING;
    }

    /**
     * Line decorator decorates last script lines with custom logic.
     */
    public interface LastScriptLineDecorator {
        /**
         * Implementing classes may want to decorate last script line.
         * @param line the last script line finishing a SQL statement.
         * @return
         */
        String decorate(String line);

        /**
         * Provides statement ending character sequence.
         * @return
         */
        String getStatementEndingCharacter();
    }
}
