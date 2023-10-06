/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionTest extends AbstractTestNGUnitTest {

    private ExecutePLSQLAction.Builder executePLSQLActionBuilder;

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BeforeMethod
    public void setUp() {
        executePLSQLActionBuilder = new ExecutePLSQLAction.Builder()
                .jdbcTemplate(jdbcTemplate);
    }

    @Test
    public void testPLSQLExecutionWithInlineScript() {
        String stmt = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;/";

        executePLSQLActionBuilder.sqlScript(stmt);

        String controlStatement = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithTransaction() {
        String stmt = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;/";

        executePLSQLActionBuilder.transactionManager(transactionManager);
        executePLSQLActionBuilder.sqlScript(stmt);

        String controlStatement = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        reset(jdbcTemplate, transactionManager);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithInlineScriptNoEndingCharacter() {
        String stmt = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        executePLSQLActionBuilder.sqlScript(stmt);

        String controlStatement = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithFileResource() {
        executePLSQLActionBuilder.sqlResource("classpath:org/citrusframework/actions/test-plsql.sql");

        String controlStatement = "DECLARE\n" +
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
                "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
                "END;";

        reset(jdbcTemplate);

        executePLSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithInlineScriptVariableSupport() {
        context.setVariable("myText", "Hello World!");
        context.setVariable("tableName", "Greetings");

        String stmt = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := '${myText}';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from ${tableName} where text='${myText}';\"" +
                "END;/";

        executePLSQLActionBuilder.sqlScript(stmt);

        String controlStatement = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithFileResourceVariableSupport() {
        context.setVariable("myText", "Hello World!");
        context.setVariable("tableName", "Greetings");

        executePLSQLActionBuilder.sqlResource("classpath:org/citrusframework/actions/test-plsql-with-variables.sql");

        String controlStatement = "DECLARE\n" +
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
                "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithMultipleInlineStatements() {
        String stmt = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;" +
                "/" +
                "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;" +
                "/";

        executePLSQLActionBuilder.sqlScript(stmt);

        String controlStatement = "DECLARE " +
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
                "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate, times(2)).execute(controlStatement);
    }

    @Test
    public void testPLSQLExecutionWithFileResourceMultipleStmts() {
        executePLSQLActionBuilder.sqlResource("classpath:org/citrusframework/actions/test-plsql-multiple-stmts.sql");

        String controlStatement = "DECLARE\n" +
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
                "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
                "END;";

        reset(jdbcTemplate);
        executePLSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate, times(2)).execute(controlStatement);
    }

    @Test
    public void testNoJdbcTemplateConfigured() {
        // Special ExecuteSQLQueryAction without a JdbcTemplate
        executePLSQLActionBuilder = new ExecutePLSQLAction.Builder().jdbcTemplate(null);
        executePLSQLActionBuilder.statements(Collections.singletonList("statement"));

        CitrusRuntimeException exception = Assert.expectThrows(CitrusRuntimeException.class, () -> executePLSQLActionBuilder.build().execute(context));

        Assert.assertEquals(exception.getMessage(), "No JdbcTemplate configured for sql execution!");
    }
}
